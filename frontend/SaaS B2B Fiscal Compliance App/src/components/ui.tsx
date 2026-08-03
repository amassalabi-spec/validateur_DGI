import type { ReactNode } from 'react'
import type { Statut } from '../data'

export function Card({
  children,
  className = '',
  padded = true,
}: {
  children: ReactNode
  className?: string
  padded?: boolean
}) {
  return (
    <section
      className={`rounded-lg border border-slate-200 bg-white shadow-sm ${padded ? 'p-5' : ''} ${className}`}
    >
      {children}
    </section>
  )
}

export function SectionTitle({ title, hint }: { title: string; hint?: string }) {
  return (
    <div className="mb-4 flex items-baseline justify-between gap-4">
      <h3 className="font-display text-[15px] font-bold tracking-tight text-slate-900">{title}</h3>
      {hint && <span className="text-xs text-slate-500">{hint}</span>}
    </div>
  )
}

const STATUT_STYLES: Record<Statut, { label: string; cls: string; dot: string }> = {
  conforme: {
    label: 'Conforme',
    cls: 'bg-emerald-50 text-emerald-700 ring-emerald-600/20',
    dot: 'bg-emerald-600',
  },
  'non-conforme': {
    label: 'Non conforme',
    cls: 'bg-rose-50 text-rose-700 ring-rose-600/20',
    dot: 'bg-rose-600',
  },
  'en-attente': {
    label: 'En attente',
    cls: 'bg-slate-100 text-slate-600 ring-slate-500/20',
    dot: 'bg-slate-400',
  },
}

export function StatutBadge({ statut, suffix }: { statut: Statut; suffix?: string }) {
  const s = STATUT_STYLES[statut]
  return (
    <span
      className={`inline-flex items-center gap-1.5 rounded-md px-2 py-1 text-xs font-semibold ring-1 ring-inset ${s.cls}`}
    >
      <span className={`h-1.5 w-1.5 rounded-full ${s.dot}`} aria-hidden />
      {s.label}
      {suffix ? <span className="font-normal opacity-80">· {suffix}</span> : null}
    </span>
  )
}

export function IconButton({
  label,
  onClick,
  children,
}: {
  label: string
  onClick?: () => void
  children: ReactNode
}) {
  return (
    <button
      type="button"
      title={label}
      aria-label={label}
      onClick={onClick}
      className="rounded-md p-1.5 text-slate-400 transition-colors hover:bg-slate-100 hover:text-indigo-600 focus:outline-none focus-visible:ring-2 focus-visible:ring-indigo-600 focus-visible:ring-offset-1"
    >
      {children}
    </button>
  )
}

export function Field({
  label,
  children,
  hint,
  error,
}: {
  label: string
  children: ReactNode
  hint?: string
  error?: boolean
}) {
  return (
    <label className="block">
      <span className="mb-1.5 flex items-center gap-1.5 text-xs font-semibold text-slate-600">
        {label}
        {error && <span className="text-rose-600">•  à corriger</span>}
      </span>
      {children}
      {hint && <span className="mt-1 block text-[11px] text-slate-500">{hint}</span>}
    </label>
  )
}

export const inputCls =
  'w-full rounded-md border bg-white px-3 py-2 text-sm text-slate-900 tabular outline-none transition-shadow placeholder:text-slate-400 focus:ring-2 focus:ring-indigo-600/30 focus:border-indigo-600'
