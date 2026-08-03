import { useEffect, useState } from 'react'
import { Card, Field, SectionTitle, inputCls } from '../components/ui'
import { IconCheck } from '../components/icons'
import { analyticsService, type AnalyticsAdminResponse } from '../services/analyticsService'
import { authService } from '../services/authService'

export function ProfilFiscal() {
  const currentUser = authService.getCurrentUser()

  return (
    <div className="grid max-w-4xl grid-cols-1 gap-6 lg:grid-cols-2">
      <Card>
        <SectionTitle title="Identification fiscale" hint="Utilisée sur chaque PDF généré" />
        <div className="space-y-4">
          <Field label="ICE (15 chiffres)">
            <input
              defaultValue="002784510000037"
              className={`${inputCls} border-slate-300 font-mono`}
            />
          </Field>
          <Field label="Identifiant fiscal (IF)">
            <input defaultValue="40218855" className={`${inputCls} border-slate-300 font-mono`} />
          </Field>
          <Field label="Registre du commerce">
            <input
              defaultValue="412885 — Casablanca"
              className={`${inputCls} border-slate-300`}
            />
          </Field>
          <Field label="Régime de TVA">
            <select
              className={`${inputCls} border-slate-300`}
              defaultValue="Débit — déclaration mensuelle"
            >
              <option>Débit — déclaration mensuelle</option>
              <option>Encaissement — déclaration mensuelle</option>
              <option>Encaissement — déclaration trimestrielle</option>
            </select>
          </Field>
          <p className="text-xs text-slate-500">
            Utilisateur connecté : {currentUser?.email ?? 'non identifié'}
          </p>
        </div>
      </Card>
      <Card>
        <SectionTitle title="Contrôles activés" hint="27 règles Art. 145 du CGI" />
        <ul className="space-y-2.5 text-sm text-slate-700">
          {[
            'Présence et validité de l’ICE client',
            'Numérotation chronologique ininterrompue',
            'Cohérence des taux de TVA (20 / 14 / 10 / 7 %)',
            'Droit de timbre 0,25 % sur les règlements en espèces',
            'Mentions obligatoires et conditions de règlement',
            'Plafond de paiement en espèces (5 000 MAD)',
          ].map((r) => (
            <li key={r} className="flex items-start gap-2.5">
              <IconCheck className="mt-0.5 h-4 w-4 shrink-0 text-emerald-600" />
              {r}
            </li>
          ))}
        </ul>
      </Card>
    </div>
  )
}

const MEMBRES = [
  { nom: 'Salma Bennani', role: 'Administratrice fiscale', mail: 's.bennani@zenith.ma', actif: true },
  { nom: 'Youssef El Amrani', role: 'Comptable senior', mail: 'y.elamrani@zenith.ma', actif: true },
  { nom: 'Nadia Cherkaoui', role: 'Auditrice', mail: 'n.cherkaoui@zenith.ma', actif: true },
  { nom: 'Karim Tazi', role: 'Lecture seule', mail: 'k.tazi@zenith.ma', actif: false },
]

export function Equipe() {
  return (
    <Card padded={false} className="max-w-4xl overflow-hidden">
      <div className="flex items-center justify-between border-b border-slate-200 px-5 py-3.5">
        <p className="font-display text-[15px] font-bold text-slate-900">
          Membres de l&apos;équipe <span className="text-slate-400">· 4 / 10 sièges</span>
        </p>
        <button
          type="button"
          className="rounded-md bg-indigo-600 px-3 py-1.5 text-xs font-semibold text-white transition-colors hover:bg-indigo-700"
        >
          Inviter un collaborateur
        </button>
      </div>
      <ul className="divide-y divide-slate-100">
        {MEMBRES.map((m) => (
          <li key={m.mail} className="flex items-center gap-4 px-5 py-3.5">
            <span className="flex h-9 w-9 items-center justify-center rounded-full bg-indigo-50 text-xs font-bold text-indigo-700">
              {m.nom
                .split(' ')
                .map((p) => p[0])
                .join('')}
            </span>
            <div className="min-w-0">
              <p className="text-sm font-semibold text-slate-900">{m.nom}</p>
              <p className="truncate text-xs text-slate-500">{m.mail}</p>
            </div>
            <span className="ml-auto rounded-md bg-slate-100 px-2 py-1 text-[11px] font-semibold text-slate-600">
              {m.role}
            </span>
            <span
              className={`text-[11px] font-semibold ${m.actif ? 'text-emerald-700' : 'text-slate-400'}`}
            >
              {m.actif ? 'Actif' : 'Invitation en attente'}
            </span>
          </li>
        ))}
      </ul>
    </Card>
  )
}

export function Abonnement() {
  return (
    <div className="grid max-w-4xl grid-cols-1 gap-6 lg:grid-cols-[1.2fr_1fr]">
      <Card className="border-l-4 border-l-indigo-600">
        <p className="text-xs font-semibold tracking-wide text-slate-500 uppercase">
          Formule actuelle
        </p>
        <p className="font-display mt-2 text-2xl font-extrabold text-slate-900">
          Cabinet — 1 490 MAD <span className="text-base font-semibold text-slate-500">/ mois</span>
        </p>
        <p className="mt-1 text-sm text-slate-500">
          Renouvellement le 1er septembre 2026 · facturation annuelle
        </p>
        <dl className="mt-5 grid grid-cols-2 gap-4 border-t border-slate-200 pt-5 text-sm">
          <div>
            <dt className="text-xs text-slate-500">Factures auditées ce mois</dt>
            <dd className="tabular font-display text-xl font-bold text-slate-900">184 / 500</dd>
          </div>
          <div>
            <dt className="text-xs text-slate-500">Sièges utilisés</dt>
            <dd className="tabular font-display text-xl font-bold text-slate-900">4 / 10</dd>
          </div>
        </dl>
        <div className="mt-4 h-1.5 overflow-hidden rounded-full bg-slate-100">
          <div className="h-full w-[37%] rounded-full bg-indigo-600" />
        </div>
      </Card>
      <Card>
        <SectionTitle title="Dernières factures d'abonnement" />
        <ul className="divide-y divide-slate-100 text-sm">
          {[
            ['Août 2026', '1 490,00 MAD', 'Payée'],
            ['Juillet 2026', '1 490,00 MAD', 'Payée'],
            ['Juin 2026', '1 490,00 MAD', 'Payée'],
          ].map(([mois, montant, etat]) => (
            <li key={mois} className="flex items-center justify-between py-2.5">
              <span className="text-slate-700">{mois}</span>
              <span className="tabular text-slate-900">{montant}</span>
              <span className="rounded bg-emerald-50 px-2 py-0.5 text-[11px] font-semibold text-emerald-700">
                {etat}
              </span>
            </li>
          ))}
        </ul>
      </Card>
    </div>
  )
}

export function AdminKpis() {
  const [data, setData] = useState<AnalyticsAdminResponse | null>(null)

  useEffect(() => {
    let mounted = true
    analyticsService
      .getAdminKpis()
      .then((res) => {
        if (mounted) setData(res)
      })
      .catch(() => {
        if (mounted) setData(null)
      })
    return () => {
      mounted = false
    }
  }, [])

  if (!data) {
    return null
  }

  return (
    <Card className="max-w-4xl">
      <SectionTitle title="KPI SaaS global" hint="Administration plateforme" />
      <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-4">
        <div>
          <p className="text-xs text-slate-500">Factures auditées</p>
          <p className="font-display text-2xl font-bold text-slate-900">{data.totalInvoicesAudited}</p>
        </div>
        <div>
          <p className="text-xs text-slate-500">Conformes</p>
          <p className="font-display text-2xl font-bold text-emerald-700">{data.totalCompliant}</p>
        </div>
        <div>
          <p className="text-xs text-slate-500">Non conformes</p>
          <p className="font-display text-2xl font-bold text-rose-700">{data.totalNonCompliant}</p>
        </div>
        <div>
          <p className="text-xs text-slate-500">Conformité plateforme</p>
          <p className="font-display text-2xl font-bold text-slate-900">
            {data.platformComplianceRate}%
          </p>
        </div>
      </div>
    </Card>
  )
}

