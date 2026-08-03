import { useEffect, useState } from 'react'
import {
  Bar,
  BarChart,
  CartesianGrid,
  Cell,
  Line,
  LineChart,
  ResponsiveContainer,
  Tooltip,
  XAxis,
  YAxis,
} from 'recharts'
import { Card, SectionTitle } from '../components/ui'
import { analyticsService, type AnalyticsCompanyResponse } from '../services/analyticsService'
import {
  ANOMALIES,
  CONFORMITE_MENSUELLE,
  PALETTE,
  REGLEMENTS,
  TVA_MENSUELLE,
  mad,
} from '../data'

const axis = {
  stroke: '#cbd5e1',
  tick: { fill: '#64748b', fontSize: 11 },
  tickLine: false,
  axisLine: false,
}

function ChartTip({ active, payload, label, unit = 'MAD' }: any) {
  if (!active || !payload?.length) return null
  return (
    <div className="rounded-lg border border-slate-200 bg-white px-3 py-2 shadow-lg">
      <p className="mb-1 text-[11px] font-semibold text-slate-900">{label}</p>
      {payload.map((p: any) => (
        <p key={p.dataKey} className="flex items-center gap-2 text-[11px] text-slate-600">
          <span
            className="h-2 w-2 rounded-full"
            style={{ background: p.color || p.fill }}
            aria-hidden
          />
          {p.name}
          <span className="tabular ml-auto font-semibold text-slate-900">
            {new Intl.NumberFormat('fr-MA').format(p.value)} {unit}
          </span>
        </p>
      ))}
    </div>
  )
}

function Kpi({
  label,
  value,
  sub,
  delta,
  tone = 'neutral',
}: {
  label: string
  value: string
  sub: string
  delta?: string
  tone?: 'neutral' | 'good' | 'bad'
}) {
  const toneCls =
    tone === 'good'
      ? 'bg-emerald-50 text-emerald-700'
      : tone === 'bad'
        ? 'bg-rose-50 text-rose-700'
        : 'bg-slate-100 text-slate-600'
  return (
    <Card>
      <p className="text-xs font-semibold tracking-wide text-slate-500 uppercase">{label}</p>
      <p className="font-display tabular mt-3 text-[26px] leading-none font-extrabold text-slate-900">
        {value}
      </p>
      <div className="mt-3 flex items-center gap-2">
        {delta && (
          <span className={`rounded px-1.5 py-0.5 text-[11px] font-semibold ${toneCls}`}>
            {delta}
          </span>
        )}
        <span className="text-xs text-slate-500">{sub}</span>
      </div>
    </Card>
  )
}

export default function Dashboard() {
  const [kpis, setKpis] = useState<AnalyticsCompanyResponse | null>(null)

  useEffect(() => {
    let mounted = true
    analyticsService
      .getCompanyKpis()
      .then((data) => {
        if (mounted) setKpis(data)
      })
      .catch(() => {
        if (mounted) setKpis(null)
      })
    return () => {
      mounted = false
    }
  }, [])

  const conformite = kpis?.complianceRate ?? 92
  const invoicesCount = kpis?.invoicesCount ?? 184
  const invoicesNonCompliant = kpis?.invoicesNonCompliant ?? 15
  const totalHt = kpis?.revenueHt ?? 1_770_000
  const totalTtc = kpis?.revenueTtc ?? 2_120_000
  const topErrors = kpis?.topErrors ?? ANOMALIES.map((a) => ({ errorMessage: a.motif, count: a.nb }))
  return (
    <div className="space-y-6">
      <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 xl:grid-cols-4">
        <Kpi
          label="TVA collectée"
          value="250 900 MAD"
          sub="206 800 à 20 % · 44 100 à 10 %"
          delta="+8,3 %"
          tone="good"
        />
        <Kpi
          label="Chiffre d'affaires cumulé"
          value={mad(totalHt)}
          sub={`HT · ${mad(totalTtc)}`}
          delta="+12,1 %"
          tone="good"
        />
        <Kpi
          label="Taux de conformité DGI"
          value={`${conformite} %`}
          sub={`${invoicesCount} factures auditées, ${invoicesNonCompliant} rejets`}
          delta="+2 pts"
          tone="good"
        />
        <Kpi
          label="Droits de timbre dus"
          value="2 310 MAD"
          sub="0,25 % sur 924 000 en espèces"
          delta="À régler"
          tone="bad"
        />
      </div>

      <div className="grid grid-cols-1 gap-6 xl:grid-cols-2">
        <Card>
          <SectionTitle title="TVA collectée par mois et par taux" hint="Exercice 2026 · MAD" />
          <div className="mb-3 flex items-center gap-4 text-[11px] font-medium text-slate-600">
            <span className="flex items-center gap-1.5">
              <span className="h-2 w-2 rounded-full" style={{ background: PALETTE.indigo }} />
              Taux 20 %
            </span>
            <span className="flex items-center gap-1.5">
              <span className="h-2 w-2 rounded-full" style={{ background: PALETTE.sky }} />
              Taux 10 %
            </span>
          </div>
          <ResponsiveContainer width="100%" height={230}>
            <BarChart data={TVA_MENSUELLE} barGap={2} margin={{ left: -12, right: 4, top: 4 }}>
              <CartesianGrid vertical={false} stroke="#f1f5f9" />
              <XAxis dataKey="mois" {...axis} />
              <YAxis {...axis} tickFormatter={(v) => `${v / 1000}k`} width={44} />
              <Tooltip cursor={{ fill: '#f8fafc' }} content={<ChartTip />} />
              <Bar dataKey="taux20" name="TVA 20 %" fill={PALETTE.indigo} radius={[4, 4, 0, 0]} />
              <Bar dataKey="taux10" name="TVA 10 %" fill={PALETTE.sky} radius={[4, 4, 0, 0]} />
            </BarChart>
          </ResponsiveContainer>
        </Card>

        <Card>
          <SectionTitle title="Évolution du taux de conformité" hint="% de factures validées DGI" />
          <div className="flex items-baseline gap-2">
            <span className="font-display tabular text-3xl font-extrabold text-emerald-700">
              {conformite} %
            </span>
            <span className="text-xs text-slate-500">au 31 juillet 2026</span>
          </div>
          <ResponsiveContainer width="100%" height={202}>
            <LineChart data={CONFORMITE_MENSUELLE} margin={{ left: -18, right: 12, top: 16 }}>
              <CartesianGrid vertical={false} stroke="#f1f5f9" />
              <XAxis dataKey="mois" {...axis} />
              <YAxis {...axis} domain={[60, 100]} width={44} tickFormatter={(v) => `${v}%`} />
              <Tooltip content={<ChartTip unit="%" />} />
              <Line
                type="monotone"
                dataKey="taux"
                name="Conformité"
                stroke={PALETTE.emerald}
                strokeWidth={2}
                dot={{ r: 3, strokeWidth: 2, fill: '#fff' }}
                activeDot={{ r: 5, strokeWidth: 2, stroke: '#fff' }}
              />
            </LineChart>
          </ResponsiveContainer>
        </Card>

        <Card>
          <SectionTitle title="Anomalies DGI les plus fréquentes" hint="12 derniers mois" />
          <ul className="space-y-3">
            {topErrors.map((a) => (
              <li key={a.errorMessage} className="flex items-center gap-3">
                <span className="w-52 shrink-0 text-[13px] text-slate-700">{a.errorMessage}</span>
                <span className="h-2.5 flex-1 overflow-hidden rounded-full bg-slate-100">
                  <span
                    className="block h-full rounded-full bg-rose-600 transition-[width] duration-500"
                    style={{ width: `${(a.count / topErrors[0].count) * 100}%` }}
                  />
                </span>
                <span className="tabular w-8 text-right text-[13px] font-semibold text-slate-900">
                  {a.count}
                </span>
              </li>
            ))}
          </ul>
        </Card>

        <Card>
          <SectionTitle title="Encaissements par mode de règlement" hint="Exercice 2026 · MAD HT" />
          <ResponsiveContainer width="100%" height={230}>
            <BarChart
              data={REGLEMENTS}
              layout="vertical"
              margin={{ left: 22, right: 24, top: 4, bottom: 4 }}
            >
              <CartesianGrid horizontal={false} stroke="#f1f5f9" />
              <XAxis type="number" {...axis} tickFormatter={(v) => `${v / 1000}k`} />
              <YAxis type="category" dataKey="mode" {...axis} width={70} />
              <Tooltip cursor={{ fill: '#f8fafc' }} content={<ChartTip />} />
              <Bar dataKey="montant" name="Encaissé" radius={[0, 4, 4, 0]} barSize={22}>
                {REGLEMENTS.map((r) => (
                  <Cell
                    key={r.mode}
                    fill={r.mode === 'Espèces' ? PALETTE.amber : PALETTE.indigo}
                  />
                ))}
              </Bar>
            </BarChart>
          </ResponsiveContainer>
          <p className="mt-2 flex items-center gap-1.5 text-[11px] text-slate-500">
            <span className="h-2 w-2 rounded-full" style={{ background: PALETTE.amber }} />
            Les espèces ({mad(281_480)}) déclenchent le droit de timbre de 0,25 %.
          </p>
        </Card>
      </div>
    </div>
  )
}
