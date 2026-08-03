import { useEffect, useState } from 'react'
import {
  IconBell,
  IconCard,
  IconChart,
  IconChevron,
  IconLogout,
  IconPdf,
  IconRegistry,
  IconScan,
  IconSearch,
  IconShield,
  IconTeam,
} from './components/icons'
import Dashboard from './pages/Dashboard'
import Workspace from './pages/Workspace'
import Registre from './pages/Registre'
import PdfStudio from './pages/PdfStudio'
import { Abonnement, AdminKpis, Equipe, ProfilFiscal } from './pages/Admin'
import { authService } from './services/authService'

type Page =
  | 'dashboard'
  | 'workspace'
  | 'registre'
  | 'studio'
  | 'profil'
  | 'equipe'
  | 'abonnement'

const NAV: { key: Page; label: string; icon: typeof IconChart }[] = [
  { key: 'dashboard', label: 'Dashboard BI', icon: IconChart },
  { key: 'workspace', label: 'Analyser une facture', icon: IconScan },
  { key: 'registre', label: 'Registre des factures', icon: IconRegistry },
  { key: 'studio', label: 'Studio PDF', icon: IconPdf },
]

const ADMIN: { key: Page; label: string; icon: typeof IconChart }[] = [
  { key: 'profil', label: 'Profil fiscal DGI', icon: IconShield },
  { key: 'equipe', label: 'Équipe', icon: IconTeam },
  { key: 'abonnement', label: 'Abonnement', icon: IconCard },
]

const META: Record<Page, { titre: string; sous: string }> = {
  dashboard: {
    titre: 'Dashboard BI',
    sous: 'Vue consolidée de vos obligations fiscales de l’exercice',
  },
  workspace: {
    titre: 'Analyser une facture',
    sous: 'Audit flash de conformité — 27 contrôles Art. 145 du CGI',
  },
  registre: { titre: 'Registre des factures', sous: 'Historique auditable et exportable pour la DGI' },
  studio: { titre: 'Studio PDF', sous: 'Modèle de facture conforme et personnalisable' },
  profil: { titre: 'Profil fiscal DGI', sous: 'Identification de votre entité et règles de contrôle' },
  equipe: { titre: 'Équipe', sous: 'Collaborateurs et niveaux d’accès' },
  abonnement: { titre: 'Abonnement', sous: 'Formule, consommation et historique de facturation' },
}

export default function App() {
  const [page, setPage] = useState<Page>('dashboard')
  const [exercice, setExercice] = useState('2026')
  const [user, setUser] = useState(() => authService.getCurrentUser())

  useEffect(() => {
    const refresh = () => setUser(authService.getCurrentUser())
    window.addEventListener('storage', refresh)
    window.addEventListener('auth:logout', refresh)
    window.addEventListener('auth:expired', refresh)
    return () => {
      window.removeEventListener('storage', refresh)
      window.removeEventListener('auth:logout', refresh)
      window.removeEventListener('auth:expired', refresh)
    }
  }, [])

  const itemCls = (active: boolean) =>
    `flex w-full items-center gap-3 rounded-md px-3 py-2 text-[13px] font-medium transition-colors focus:outline-none focus-visible:ring-2 focus-visible:ring-indigo-400 ${
      active
        ? 'bg-indigo-600 text-white shadow-sm'
        : 'text-slate-300 hover:bg-white/5 hover:text-white'
    }`

  return (
    <div className="min-h-screen bg-slate-50 lg:grid lg:grid-cols-[250px_1fr]">
      <aside className="sticky top-0 hidden h-screen flex-col bg-slate-900 lg:flex">
        <div className="flex items-center gap-2.5 px-5 py-5">
          <span className="flex h-8 w-8 items-center justify-center rounded-md bg-indigo-600 font-display text-sm font-extrabold text-white">
            F
          </span>
          <div className="leading-tight">
            <p className="font-display text-sm font-extrabold tracking-tight text-white">
              Fiscalys
            </p>
            <p className="text-[10px] tracking-widest text-slate-400 uppercase">Conformité DGI</p>
          </div>
        </div>

        <nav className="flex-1 space-y-1 overflow-y-auto px-3">
          {NAV.map((n) => (
            <button key={n.key} type="button" onClick={() => setPage(n.key)} className={itemCls(page === n.key)}>
              <n.icon className="h-[17px] w-[17px] shrink-0" />
              {n.label}
            </button>
          ))}

          <p className="px-3 pt-6 pb-2 text-[10px] font-semibold tracking-widest text-slate-500 uppercase">
            Administration
          </p>
          {ADMIN.map((n) => (
            <button key={n.key} type="button" onClick={() => setPage(n.key)} className={itemCls(page === n.key)}>
              <n.icon className="h-[17px] w-[17px] shrink-0" />
              {n.label}
            </button>
          ))}
        </nav>

        <div className="m-3 rounded-lg bg-white/5 p-3">
          <div className="flex items-center gap-2.5">
            <span className="flex h-8 w-8 items-center justify-center rounded-full bg-indigo-500 text-[11px] font-bold text-white">
              {user?.email ? user.email.slice(0, 2).toUpperCase() : 'SB'}
            </span>
            <div className="min-w-0 flex-1">
              <p className="truncate text-[13px] font-semibold text-white">
                {user?.email ?? 'Session non connectée'}
              </p>
              <p className="truncate text-[11px] text-slate-400">
                {user?.companyName ?? 'Connectez-vous pour synchroniser votre entreprise'}
              </p>
            </div>
            <button
              type="button"
              aria-label="Se déconnecter"
              title="Se déconnecter"
              onClick={() => authService.logout()}
              className="rounded p-1.5 text-slate-400 transition-colors hover:bg-white/10 hover:text-white"
            >
              <IconLogout className="h-4 w-4" />
            </button>
          </div>
        </div>
      </aside>

      <div className="flex min-h-screen flex-col">
        <header className="sticky top-0 z-10 border-b border-slate-200 bg-white/90 backdrop-blur">
          <div className="flex flex-wrap items-center gap-4 px-5 py-3.5 lg:px-8">
            <div className="mr-auto">
              <h1 className="font-display text-lg leading-tight font-extrabold tracking-tight text-slate-900">
                {META[page].titre}
              </h1>
              <p className="text-xs text-slate-500">{META[page].sous}</p>
            </div>

            <div className="relative hidden xl:block">
              <IconSearch className="pointer-events-none absolute top-2.5 left-3 h-4 w-4 text-slate-400" />
              <input
                placeholder="Rechercher une facture, un ICE, un client…"
                className="w-72 rounded-md border border-slate-300 bg-slate-50 py-2 pr-3 pl-9 text-sm outline-none transition-shadow placeholder:text-slate-400 focus:border-indigo-600 focus:bg-white focus:ring-2 focus:ring-indigo-600/25"
              />
            </div>

            <div className="relative">
              <select
                value={exercice}
                onChange={(e) => setExercice(e.target.value)}
                aria-label="Exercice fiscal"
                className="appearance-none rounded-md border border-slate-300 bg-white py-2 pr-8 pl-3 text-sm font-semibold text-slate-700 outline-none focus:border-indigo-600 focus:ring-2 focus:ring-indigo-600/25"
              >
                {['2026', '2025', '2024'].map((a) => (
                  <option key={a} value={a}>
                    Année {a}
                  </option>
                ))}
              </select>
              <IconChevron className="pointer-events-none absolute top-2.5 right-2 h-4 w-4 text-slate-400" />
            </div>

            <button
              type="button"
              aria-label="Notifications — 3 non lues"
              className="relative rounded-md border border-slate-300 p-2 text-slate-500 transition-colors hover:bg-slate-50 hover:text-indigo-600"
            >
              <IconBell />
              <span className="absolute -top-1 -right-1 flex h-4 w-4 items-center justify-center rounded-full bg-rose-600 text-[9px] font-bold text-white">
                3
              </span>
            </button>
          </div>

          {!user && (
            <div className="border-t border-slate-200 bg-amber-50 px-5 py-2 text-xs text-amber-900">
              Aucun jeton JWT détecté. Utilisez les endpoints <code>/api/v1/auth/register</code> ou{' '}
              <code>/api/v1/auth/login</code> depuis votre client API pour activer les pages backend.
            </div>
          )}

          <nav className="flex gap-1 overflow-x-auto border-t border-slate-200 px-3 py-2 lg:hidden">
            {[...NAV, ...ADMIN].map((n) => (
              <button
                key={n.key}
                type="button"
                onClick={() => setPage(n.key)}
                className={`shrink-0 rounded-md px-3 py-1.5 text-xs font-semibold transition-colors ${
                  page === n.key ? 'bg-indigo-600 text-white' : 'text-slate-600 hover:bg-slate-100'
                }`}
              >
                {n.label}
              </button>
            ))}
          </nav>
        </header>

        <main className="flex-1 px-5 py-6 lg:px-8">
          {page === 'dashboard' && <Dashboard />}
          {page === 'workspace' && <Workspace />}
          {page === 'registre' && <Registre />}
          {page === 'studio' && <PdfStudio />}
          {page === 'profil' && <AdminKpis />}
          {page === 'profil' && <ProfilFiscal />}
          {page === 'equipe' && <Equipe />}
          {page === 'abonnement' && <Abonnement />}
        </main>
      </div>
    </div>
  )
}
