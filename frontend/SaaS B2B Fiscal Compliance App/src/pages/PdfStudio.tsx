import { useState } from 'react'
import { Card, Field, SectionTitle, inputCls } from '../components/ui'
import { num } from '../data'

const ACCENTS = [
  { nom: 'Indigo', hex: '#4f46e5' },
  { nom: 'Ardoise', hex: '#0f172a' },
  { nom: 'Émeraude', hex: '#059669' },
  { nom: 'Terracotta', hex: '#b45309' },
]

export default function PdfStudio() {
  const [accent, setAccent] = useState('#4f46e5')
  const [societe, setSociete] = useState('Zenith Conseil & Audit SARL')
  const [mentions, setMentions] = useState(
    "Facture émise conformément à l'article 145 du Code Général des Impôts. Pénalité de retard : 3 % par mois entamé.",
  )
  const [afficherTimbre, setAfficherTimbre] = useState(true)
  const [langue, setLangue] = useState<'fr' | 'ar' | 'bi'>('bi')

  const ht = 62_400
  const tva = ht * 0.2
  const timbre = afficherTimbre ? (ht + tva) * 0.0025 : 0

  return (
    <div className="grid grid-cols-1 gap-6 lg:grid-cols-[340px_1fr]">
      <div className="space-y-4">
        <Card>
          <SectionTitle title="Identité du modèle" />
          <div className="space-y-4">
            <Field label="Raison sociale">
              <input
                value={societe}
                onChange={(e) => setSociete(e.target.value)}
                className={`${inputCls} border-slate-300`}
              />
            </Field>
            <Field label="Couleur d'accent">
              <div className="flex gap-2">
                {ACCENTS.map((a) => (
                  <button
                    key={a.hex}
                    type="button"
                    title={a.nom}
                    aria-label={a.nom}
                    onClick={() => setAccent(a.hex)}
                    style={{ background: a.hex }}
                    className={`h-8 w-8 rounded-md ring-offset-2 transition-all ${
                      accent === a.hex ? 'ring-2 ring-slate-900' : 'hover:scale-105'
                    }`}
                  />
                ))}
              </div>
            </Field>
            <Field label="Langue du document">
              <div className="flex rounded-md border border-slate-300 p-0.5">
                {(
                  [
                    ['fr', 'Français'],
                    ['ar', 'العربية'],
                    ['bi', 'Bilingue'],
                  ] as const
                ).map(([k, l]) => (
                  <button
                    key={k}
                    type="button"
                    onClick={() => setLangue(k)}
                    className={`flex-1 rounded px-2 py-1.5 text-xs font-semibold transition-colors ${
                      langue === k ? 'bg-slate-900 text-white' : 'text-slate-600 hover:bg-slate-100'
                    }`}
                  >
                    {l}
                  </button>
                ))}
              </div>
            </Field>
          </div>
        </Card>

        <Card>
          <SectionTitle title="Mentions légales" />
          <textarea
            value={mentions}
            onChange={(e) => setMentions(e.target.value)}
            rows={4}
            className={`${inputCls} border-slate-300 resize-none leading-relaxed`}
          />
          <label className="mt-4 flex items-center gap-2.5 text-sm text-slate-700">
            <input
              type="checkbox"
              checked={afficherTimbre}
              onChange={(e) => setAfficherTimbre(e.target.checked)}
              className="h-4 w-4 rounded border-slate-300 accent-indigo-600"
            />
            Afficher la ligne « droit de timbre 0,25 % »
          </label>
        </Card>

        <button
          type="button"
          className="w-full rounded-md bg-indigo-600 px-4 py-2.5 text-sm font-semibold text-white shadow-sm transition-colors hover:bg-indigo-700"
        >
          Enregistrer le modèle
        </button>
      </div>

      <Card className="flex justify-center bg-slate-100/70 p-8">
        <div className="w-full max-w-[600px] bg-white p-10 shadow-lg" style={{ aspectRatio: '1/1.414' }}>
          <div className="flex items-start justify-between border-b-2 pb-5" style={{ borderColor: accent }}>
            <div>
              <p className="font-display text-lg font-extrabold text-slate-900">{societe}</p>
              <p className="mt-1 font-mono text-[9px] leading-relaxed text-slate-500">
                ICE 002784510000037 · IF 40218855 · RC 412885 Casablanca
                <br />
                14, boulevard Zerktouni, 20100 Casablanca — Maroc
              </p>
            </div>
            <div className="text-right">
              <p
                className="font-display text-sm font-extrabold tracking-wide uppercase"
                style={{ color: accent }}
              >
                Facture
              </p>
              {langue !== 'fr' && (
                <p className="text-[11px] text-slate-500" dir="rtl">
                  فاتورة
                </p>
              )}
              <p className="mt-1 font-mono text-[10px] text-slate-600">FAC-2026-0183</p>
              <p className="font-mono text-[10px] text-slate-500">26 / 07 / 2026</p>
            </div>
          </div>

          <div className="mt-5 rounded bg-slate-50 p-3">
            <p className="text-[8px] font-semibold tracking-widest text-slate-500 uppercase">
              Client
            </p>
            <p className="mt-1 text-[11px] font-semibold text-slate-900">
              Atlas Textile Industries SARL
            </p>
            <p className="font-mono text-[9px] text-slate-500">ICE 001456782000041</p>
          </div>

          <table className="mt-5 w-full text-left text-[10px]">
            <thead>
              <tr className="text-[8px] tracking-widest text-slate-500 uppercase">
                <th className="border-b border-slate-200 pb-1.5">Désignation</th>
                <th className="border-b border-slate-200 pb-1.5 text-right">Qté</th>
                <th className="border-b border-slate-200 pb-1.5 text-right">P.U. HT</th>
                <th className="border-b border-slate-200 pb-1.5 text-right">Total HT</th>
              </tr>
            </thead>
            <tbody className="text-slate-700">
              {[
                ['Mission d’audit fiscal — exercice 2025', 1, 42_000],
                ['Assistance contrôle DGI (12 h)', 12, 1_200],
                ['Rapport de conformité Art. 145', 1, 6_000],
              ].map(([d, q, pu]) => (
                <tr key={d as string}>
                  <td className="py-1.5">{d}</td>
                  <td className="tabular py-1.5 text-right">{q}</td>
                  <td className="tabular py-1.5 text-right">{num(pu as number, 0)}</td>
                  <td className="tabular py-1.5 text-right font-medium">
                    {num((q as number) * (pu as number), 0)}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>

          <div className="mt-4 ml-auto w-56 space-y-1 text-[10px]">
            <div className="flex justify-between text-slate-600">
              <span>Total HT</span>
              <span className="tabular">{num(ht)}</span>
            </div>
            <div className="flex justify-between text-slate-600">
              <span>TVA 20 %</span>
              <span className="tabular">{num(tva)}</span>
            </div>
            {afficherTimbre && (
              <div className="flex justify-between text-slate-600">
                <span>Droit de timbre 0,25 %</span>
                <span className="tabular">{num(timbre)}</span>
              </div>
            )}
            <div
              className="flex justify-between border-t pt-1.5 text-[12px] font-extrabold"
              style={{ borderColor: accent, color: accent }}
            >
              <span>Total TTC</span>
              <span className="tabular">{num(ht + tva + timbre)}</span>
            </div>
          </div>

          <p className="mt-6 border-t border-slate-200 pt-3 text-[8px] leading-relaxed text-slate-500">
            {mentions}
          </p>
        </div>
      </Card>
    </div>
  )
}
