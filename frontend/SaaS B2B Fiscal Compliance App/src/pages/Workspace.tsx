import { useEffect, useRef, useState } from 'react'
import { Card, Field, SectionTitle, StatutBadge, inputCls } from '../components/ui'
import { IconAlert, IconCheck, IconFile, IconSparkle, IconUpload } from '../components/icons'
import { num } from '../data'
import { invoiceService, type InvoiceUploadResponse } from '../services/invoiceService'

type Phase = 'idle' | 'analyse' | 'resultat'

const ANOMALIES = [
  {
    code: 'ICE-001',
    titre: 'ICE Client manquant',
    detail: "Champ obligatoire — Art. 145-VIII du CGI. Aucun identifiant à 15 chiffres détecté.",
  },
  {
    code: 'TVA-014',
    titre: 'Écart de calcul TVA',
    detail: '12 480,00 déclaré contre 12 480,00 attendu à 20 % — base HT incohérente de 1 200,00.',
  },
  {
    code: 'TMB-002',
    titre: 'Droit de timbre absent',
    detail: 'Règlement en espèces : timbre de 0,25 % non appliqué sur le TTC.',
  },
]

const EXTRACTION = [
  { champ: 'Numéro de facture', source: 'Facture n° 183/2026', extrait: 'FAC-2026-0183', ok: true },
  { champ: 'Date d’émission', source: 'Casablanca, le 26/07/2026', extrait: '2026-07-26', ok: true },
  { champ: 'Client', source: 'ATLAS TEXTILE INDUSTRIES', extrait: 'Atlas Textile Industries SARL', ok: true },
  { champ: 'ICE client', source: '— absent du document —', extrait: null, ok: false },
  { champ: 'Total HT', source: '62.400,00 DH', extrait: '62 400,00', ok: true },
  { champ: 'TVA 20 %', source: '12.480,00 DH', extrait: '12 480,00', ok: false },
  { champ: 'Mode de règlement', source: 'Réglé en espèces', extrait: 'Espèces', ok: true },
  { champ: 'Total TTC', source: '74.880,00 DH', extrait: '74 880,00', ok: false },
]

export default function Workspace() {
  const fileInputRef = useRef<HTMLInputElement>(null)
  const [phase, setPhase] = useState<Phase>('idle')
  const [progress, setProgress] = useState(0)
  const [dragging, setDragging] = useState(false)
  const [selectedFile, setSelectedFile] = useState<File | null>(null)
  const [ice, setIce] = useState('')
  const [reglement, setReglement] = useState<'Virement' | 'Chèque' | 'Espèces' | 'Effet'>('Espèces')
  const [ht, setHt] = useState(62_400)
  const [genere, setGenere] = useState(false)
  const [isUploading, setIsUploading] = useState(false)
  const [uploadResult, setUploadResult] = useState<InvoiceUploadResponse | null>(null)
  const [uploadError, setUploadError] = useState<string | null>(null)

  useEffect(() => {
    if (phase !== 'analyse') return
    setProgress(0)
    const id = setInterval(() => {
      setProgress((p) => {
        if (p >= 100) {
          clearInterval(id)
          setPhase('resultat')
          return 100
        }
        return p + 4
      })
    }, 45)
    return () => clearInterval(id)
  }, [phase])

  async function uploadFile(file: File) {
    setSelectedFile(file)
    setUploadError(null)
    setIsUploading(true)
    setPhase('analyse')
    try {
      const result = await invoiceService.upload(file)
      setUploadResult(result)
      setPhase('resultat')
    } catch (error) {
      setUploadError(error instanceof Error ? error.message : 'Impossible de joindre le backend.')
      setUploadResult(null)
      setPhase('resultat')
    } finally {
      setIsUploading(false)
    }
  }

  const tva = ht * 0.2
  const timbre = reglement === 'Espèces' ? (ht + tva) * 0.0025 : 0
  const ttc = ht + tva + timbre
  const iceValide = /^\d{15}$/.test(ice)
  const restantes = (iceValide ? 0 : 1) + (timbre > 0 ? 0 : 0)

  if (phase !== 'resultat') {
    return (
      <div className="mx-auto max-w-3xl pt-6">
        <div className="mb-6 text-center">
          <h2 className="font-display text-2xl font-extrabold tracking-tight text-slate-900">
            Audit flash de conformité DGI
          </h2>
          <p className="mt-2 text-sm text-slate-500">
            Déposez une facture Word ou Excel : 27 contrôles de l&apos;article 145 du CGI sont
            exécutés en quelques secondes.
          </p>
        </div>

        <div
          onDragOver={(e) => {
            e.preventDefault()
            setDragging(true)
          }}
          onDragLeave={() => setDragging(false)}
          onDrop={(e) => {
            e.preventDefault()
            setDragging(false)
            const file = e.dataTransfer.files?.[0]
            if (file) {
              void uploadFile(file)
            }
          }}
          className={`rounded-lg border-2 border-dashed bg-white px-8 py-14 text-center shadow-sm transition-colors ${
            dragging ? 'border-indigo-600 bg-indigo-50/60' : 'border-slate-300'
          }`}
        >
          <span className="mx-auto mb-4 flex h-14 w-14 items-center justify-center rounded-full bg-indigo-50 text-indigo-600">
            <IconUpload className="h-6 w-6" />
          </span>
          <p className="font-display text-base font-bold text-slate-900">
            Glissez-déposez votre facture ici
          </p>
          <p className="mt-1 text-xs text-slate-500">
            Formats acceptés : .docx, .xlsx, .pdf — 10 Mo maximum
          </p>
          <button
            type="button"
            onClick={() => fileInputRef.current?.click()}
            disabled={isUploading}
            className="mt-5 rounded-md bg-indigo-600 px-4 py-2 text-sm font-semibold text-white shadow-sm transition-colors hover:bg-indigo-700 focus:outline-none focus-visible:ring-2 focus-visible:ring-indigo-600 focus-visible:ring-offset-2 disabled:opacity-60"
          >
            {isUploading ? 'Analyse en cours…' : 'Parcourir mes fichiers'}
          </button>

          <input
            ref={fileInputRef}
            type="file"
            accept=".docx,.xlsx,.pdf"
            hidden
            onChange={(e) => {
              const file = e.target.files?.[0]
              if (file) {
                void uploadFile(file)
              }
            }}
          />

          {phase === 'analyse' && (
            <div className="mx-auto mt-8 max-w-md text-left">
              <div className="mb-1.5 flex justify-between text-xs font-medium text-slate-600">
                <span className="flex items-center gap-1.5">
                  <IconFile className="h-4 w-4 text-indigo-600" />
                  {selectedFile?.name || 'facture-atlas-textile-072026.docx'}
                </span>
                <span className="tabular">{progress} %</span>
              </div>
              <div className="h-1.5 overflow-hidden rounded-full bg-slate-200">
                <div
                  className="h-full rounded-full bg-indigo-600 transition-[width] duration-75"
                  style={{ width: `${progress}%` }}
                />
              </div>
              <p className="mt-2 text-[11px] text-slate-500">
                {progress < 40
                  ? 'Extraction OCR des champs…'
                  : progress < 80
                    ? 'Vérification des mentions obligatoires Art. 145…'
                    : 'Recalcul TVA et droits de timbre…'}
              </p>
            </div>
          )}
        </div>

        <ul className="mt-6 grid grid-cols-1 gap-3 sm:grid-cols-3">
          {['Mentions Art. 145 du CGI', 'Cohérence TVA 20 / 10 / 7 %', 'Timbre 0,25 % espèces'].map(
            (c) => (
              <li
                key={c}
                className="flex items-center gap-2 rounded-lg border border-slate-200 bg-white px-3 py-2.5 text-xs text-slate-600 shadow-sm"
              >
                <IconCheck className="h-4 w-4 shrink-0 text-emerald-600" />
                {c}
              </li>
            ),
          )}
        </ul>
      </div>
    )
  }

  return (
    <div className="space-y-6">
      <Card className="border-l-4 border-l-rose-600">
        <div className="flex flex-wrap items-center justify-between gap-4">
          <div>
            <div className="flex items-center gap-3">
              <h2 className="font-display text-xl font-extrabold tracking-tight text-slate-900">
                FAC-2026-0183
              </h2>
              <StatutBadge statut="non-conforme" suffix="3 erreurs" />
            </div>
            <p className="mt-1 text-xs text-slate-500">
              Atlas Textile Industries SARL · émise le 26 juillet 2026 ·{' '}
              <span className="font-mono">facture-atlas-textile-072026.docx</span>
            </p>
          </div>
          <button
            type="button"
            onClick={() => {
              setPhase('idle')
              setGenere(false)
              setIce('')
            }}
            className="rounded-md border border-slate-300 px-3 py-1.5 text-xs font-semibold text-slate-600 transition-colors hover:bg-slate-50"
          >
            Analyser une autre facture
          </button>
        </div>
      </Card>

      {uploadResult && (
        <Card className={uploadResult.isCompliant ? 'border-l-4 border-l-emerald-600' : 'border-l-4 border-l-rose-600'}>
          <div className="flex flex-wrap items-center justify-between gap-3">
            <div>
              <div className="flex items-center gap-3">
                <h2 className="font-display text-lg font-extrabold tracking-tight text-slate-900">
                  Audit backend #{uploadResult.id}
                </h2>
                <StatutBadge
                  statut={uploadResult.isCompliant ? 'conforme' : 'non-conforme'}
                  suffix={`${uploadResult.auditReport.failedRules} anomalie(s)`}
                />
              </div>
              <p className="mt-1 text-xs text-slate-500">
                Facture {uploadResult.invoiceNumber} · règles exécutées {uploadResult.auditReport.totalRules}
              </p>
            </div>
            <p className="text-xs font-semibold text-slate-500">
              {uploadResult.auditReport.passedRules} règles validées
            </p>
          </div>
          {uploadResult.auditReport.errors.length > 0 && (
            <ul className="mt-4 space-y-2">
              {uploadResult.auditReport.errors.map((err) => (
                <li key={`${err.ruleCode}-${err.fieldName}`} className="rounded-lg bg-slate-50 p-3 text-sm">
                  <div className="flex items-center justify-between gap-2">
                    <span className="font-semibold text-slate-900">{err.message}</span>
                    <span className="font-mono text-[11px] text-slate-500">{err.ruleCode}</span>
                  </div>
                  <p className="mt-1 text-xs text-slate-500">
                    Champ: {err.fieldName}
                    {err.expectedValue ? ` · attendu ${err.expectedValue}` : ''}
                    {err.actualValue ? ` · actuel ${err.actualValue}` : ''}
                  </p>
                </li>
              ))}
            </ul>
          )}
        </Card>
      )}

      {uploadError && (
        <Card className="border-l-4 border-l-amber-500">
          <p className="text-sm text-amber-800">
            Backend indisponible ou réponse invalide : {uploadError}. Le mode démo reste actif.
          </p>
        </Card>
      )}

      <div className="grid grid-cols-1 gap-6 lg:grid-cols-2">
        <Card>
          <SectionTitle title="Alerte anomalies DGI" hint="3 blocages à la validation" />
          <ul className="space-y-3">
            {ANOMALIES.map((a) => (
              <li key={a.code} className="rounded-lg bg-rose-50 p-3 ring-1 ring-rose-600/15">
                <div className="flex items-center gap-2">
                  <IconAlert className="h-4 w-4 shrink-0 text-rose-600" />
                  <span className="text-[13px] font-semibold text-rose-700">{a.titre}</span>
                  <span className="ml-auto font-mono text-[10px] text-rose-600/70">{a.code}</span>
                </div>
                <p className="mt-1.5 pl-6 text-xs leading-relaxed text-slate-600">{a.detail}</p>
              </li>
            ))}
          </ul>
        </Card>

        <Card padded={false}>
          <div className="p-5 pb-0">
            <SectionTitle title="Facture source vs valeurs extraites" hint="OCR · 8 champs" />
          </div>
          <table className="w-full text-left text-[12px]">
            <thead>
              <tr className="border-y border-slate-200 bg-slate-50 text-[10px] tracking-wide text-slate-500 uppercase">
                <th className="px-5 py-2 font-semibold">Champ</th>
                <th className="px-3 py-2 font-semibold">Source</th>
                <th className="px-5 py-2 font-semibold">Extrait</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100">
              {EXTRACTION.map((r) => (
                <tr key={r.champ} className={r.ok ? '' : 'bg-rose-50/50'}>
                  <td className="px-5 py-2.5 font-medium text-slate-700">{r.champ}</td>
                  <td className="px-3 py-2.5 font-mono text-[11px] text-slate-500">{r.source}</td>
                  <td className="tabular px-5 py-2.5 font-semibold text-slate-900">
                    {r.extrait ?? <span className="text-rose-600">Non détecté</span>}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </Card>
      </div>

      <Card>
        <SectionTitle
          title="Formulaire de correction rapide"
          hint="Les totaux se recalculent automatiquement"
        />
        <div className="grid grid-cols-1 gap-5 lg:grid-cols-[1.4fr_1fr]">
          <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
            <Field
              label="ICE Client"
              error={!iceValide}
              hint="15 chiffres — Identifiant Commun de l'Entreprise"
            >
              <input
                value={ice}
                onChange={(e) => setIce(e.target.value.replace(/\D/g, '').slice(0, 15))}
                placeholder="000000000000000"
                inputMode="numeric"
                className={`${inputCls} font-mono ${iceValide ? 'border-emerald-500' : 'border-rose-400'}`}
              />
            </Field>
            <Field label="Mode de règlement" hint="Les espèces déclenchent le timbre de 0,25 %">
              <select
                value={reglement}
                onChange={(e) => setReglement(e.target.value as typeof reglement)}
                className={`${inputCls} border-slate-300`}
              >
                {['Virement', 'Chèque', 'Espèces', 'Effet'].map((m) => (
                  <option key={m}>{m}</option>
                ))}
              </select>
            </Field>
            <Field label="Total HT (MAD)">
              <input
                type="number"
                value={ht}
                onChange={(e) => setHt(Number(e.target.value) || 0)}
                className={`${inputCls} border-slate-300`}
              />
            </Field>
            <Field label="Taux de TVA applicable">
              <select className={`${inputCls} border-slate-300`} defaultValue="20 % — taux normal">
                <option>20 % — taux normal</option>
                <option>14 % — taux réduit</option>
                <option>10 % — restauration / banque</option>
                <option>7 % — produits de première nécessité</option>
              </select>
            </Field>
          </div>

          <div className="rounded-lg bg-slate-900 p-5 text-white">
            <p className="text-[10px] font-semibold tracking-widest text-slate-400 uppercase">
              Totaux recalculés
            </p>
            <dl className="mt-4 space-y-2.5 text-sm">
              {[
                ['Total HT', num(ht)],
                ['TVA 20 %', num(tva)],
                ['Droit de timbre 0,25 %', num(timbre)],
              ].map(([k, v]) => (
                <div key={k} className="flex justify-between">
                  <dt className="text-slate-400">{k}</dt>
                  <dd className="tabular font-medium">{v}</dd>
                </div>
              ))}
              <div className="flex justify-between border-t border-white/15 pt-3">
                <dt className="font-semibold">Total TTC</dt>
                <dd className="font-display tabular text-lg font-extrabold text-emerald-400">
                  {num(ttc)}
                </dd>
              </div>
            </dl>
            <p className="mt-4 flex items-start gap-1.5 text-[11px] leading-relaxed text-slate-400">
              <IconSparkle className="mt-px h-3.5 w-3.5 shrink-0 text-indigo-400" />
              {timbre > 0
                ? 'Timbre appliqué automatiquement (règlement en espèces, Art. 252 du CGI).'
                : 'Aucun timbre dû pour ce mode de règlement.'}
            </p>
          </div>
        </div>

        <div className="mt-6 flex flex-wrap items-center gap-3 border-t border-slate-200 pt-5">
          <button
            type="button"
            disabled={!iceValide}
            onClick={() => setGenere(true)}
            className="rounded-md bg-indigo-600 px-4 py-2.5 text-sm font-semibold text-white shadow-sm transition-colors hover:bg-indigo-700 focus:outline-none focus-visible:ring-2 focus-visible:ring-indigo-600 focus-visible:ring-offset-2 disabled:cursor-not-allowed disabled:bg-slate-300"
          >
            Valider et générer le PDF conforme
          </button>
          <button
            type="button"
            className="rounded-md border border-slate-300 px-4 py-2.5 text-sm font-semibold text-slate-600 transition-colors hover:border-rose-300 hover:bg-rose-50 hover:text-rose-700"
          >
            Rejeter la facture
          </button>
          {genere ? (
            <span className="flex items-center gap-1.5 text-xs font-semibold text-emerald-700">
              <IconCheck className="h-4 w-4" />
              PDF conforme généré et archivé au registre.
            </span>
          ) : (
            <span className="text-xs text-slate-500">
              {restantes > 0
                ? 'Renseignez un ICE valide à 15 chiffres pour débloquer la génération.'
                : 'Tous les contrôles DGI sont satisfaits.'}
            </span>
          )}
        </div>
      </Card>
    </div>
  )
}
