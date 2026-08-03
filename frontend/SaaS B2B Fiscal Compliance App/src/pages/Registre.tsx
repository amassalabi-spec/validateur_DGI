import { useEffect, useMemo, useState } from 'react'
import { Card, IconButton, StatutBadge, inputCls } from '../components/ui'
import { IconDownload, IconEdit, IconEye, IconSearch } from '../components/icons'
import { REGISTRE, type Statut, dateFr, mad, num } from '../data'
import { invoiceService, type Invoice as ApiInvoice } from '../services/invoiceService'

const FILTRES: { key: Statut | 'tous'; label: string }[] = [
  { key: 'tous', label: 'Toutes' },
  { key: 'conforme', label: 'Conformes' },
  { key: 'non-conforme', label: 'Non conformes' },
  { key: 'en-attente', label: 'En attente' },
]

export default function Registre() {
  const [statut, setStatut] = useState<Statut | 'tous'>('tous')
  const [q, setQ] = useState('')
  const [debut, setDebut] = useState('2026-06-01')
  const [fin, setFin] = useState('2026-07-31')
  const [selection, setSelection] = useState<string | null>(null)
  const [remoteRows, setRemoteRows] = useState<typeof REGISTRE | null>(null)

  useEffect(() => {
    let mounted = true
    invoiceService
      .list(0, 50)
      .then((res) => {
        if (!mounted) return
        const mapped = res.content.map((row: ApiInvoice) => ({
          numero: row.invoiceNumber,
          date: row.date || row.createdAt.slice(0, 10),
          client: row.clientName,
          ice: row.clientIce || '—',
          ht: row.totalHt,
          tva: row.totalTva,
          ttc: row.totalTtc,
          reglement: row.paymentMethod || 'Virement',
          statut:
            row.status === 'COMPLIANT'
              ? 'conforme'
              : row.status === 'PENDING_AUDIT'
                ? 'en-attente'
                : 'non-conforme',
          anomalies: row.isCompliant ? 0 : 1,
        }))
        setRemoteRows(mapped)
      })
      .catch(() => {
        if (mounted) setRemoteRows(null)
      })
    return () => {
      mounted = false
    }
  }, [])

  const lignes = useMemo(
    () =>
      (remoteRows ?? REGISTRE).filter((f) => {
        if (statut !== 'tous' && f.statut !== statut) return false
        if (f.date < debut || f.date > fin) return false
        const needle = q.trim().toLowerCase()
        if (!needle) return true
        return (
          f.client.toLowerCase().includes(needle) ||
          f.ice.includes(needle) ||
          f.numero.toLowerCase().includes(needle)
        )
      }),
    [remoteRows, statut, q, debut, fin],
  )

  const totalTtc = lignes.reduce((s, f) => s + f.ttc, 0)

  return (
    <div className="space-y-4">
      <Card className="flex flex-wrap items-end gap-4">
        <div className="min-w-56 flex-1">
          <span className="mb-1.5 block text-xs font-semibold text-slate-600">
            Recherche ICE / client / n° facture
          </span>
          <div className="relative">
            <IconSearch className="pointer-events-none absolute top-2.5 left-3 h-4 w-4 text-slate-400" />
            <input
              value={q}
              onChange={(e) => setQ(e.target.value)}
              placeholder="Ex. 001789456000031"
              className={`${inputCls} border-slate-300 pl-9`}
            />
          </div>
        </div>
        <div>
          <span className="mb-1.5 block text-xs font-semibold text-slate-600">Plage de dates</span>
          <div className="flex items-center gap-2">
            <input
              type="date"
              value={debut}
              onChange={(e) => setDebut(e.target.value)}
              className={`${inputCls} border-slate-300`}
            />
            <span className="text-slate-400">→</span>
            <input
              type="date"
              value={fin}
              onChange={(e) => setFin(e.target.value)}
              className={`${inputCls} border-slate-300`}
            />
          </div>
        </div>
        <div>
          <span className="mb-1.5 block text-xs font-semibold text-slate-600">Statut DGI</span>
          <div className="flex rounded-md border border-slate-300 bg-white p-0.5">
            {FILTRES.map((f) => (
              <button
                key={f.key}
                type="button"
                onClick={() => setStatut(f.key)}
                className={`rounded px-3 py-1.5 text-xs font-semibold transition-colors ${
                  statut === f.key
                    ? 'bg-indigo-600 text-white'
                    : 'text-slate-600 hover:bg-slate-100'
                }`}
              >
                {f.label}
              </button>
            ))}
          </div>
        </div>
      </Card>

      <Card padded={false} className="overflow-hidden">
        <div className="flex items-center justify-between border-b border-slate-200 px-5 py-3">
          <p className="text-xs text-slate-500">
            <span className="tabular font-semibold text-slate-900">{lignes.length}</span> factures ·
            total TTC <span className="tabular font-semibold text-slate-900">{mad(totalTtc)}</span>
          </p>
          <button
            type="button"
            className="rounded-md border border-slate-300 px-3 py-1.5 text-xs font-semibold text-slate-600 transition-colors hover:bg-slate-50"
          >
            Exporter le registre (EDI)
          </button>
        </div>

        <div className="overflow-x-auto">
          <table className="w-full min-w-[900px] text-left text-[13px]">
            <thead>
              <tr className="bg-slate-50 text-[10px] tracking-wide text-slate-500 uppercase">
                <th className="px-5 py-2.5 font-semibold">N° facture</th>
                <th className="px-3 py-2.5 font-semibold">Date</th>
                <th className="px-3 py-2.5 font-semibold">Client B2B</th>
                <th className="px-3 py-2.5 font-semibold">ICE client</th>
                <th className="px-3 py-2.5 text-right font-semibold">Total HT</th>
                <th className="px-3 py-2.5 text-right font-semibold">Total TTC</th>
                <th className="px-3 py-2.5 font-semibold">Statut DGI</th>
                <th className="px-5 py-2.5 text-right font-semibold">Actions</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100">
              {lignes.map((f) => (
                <tr
                  key={f.numero}
                  onClick={() => setSelection(f.numero === selection ? null : f.numero)}
                  className={`cursor-pointer transition-colors ${
                    selection === f.numero ? 'bg-indigo-50/60' : 'hover:bg-slate-50'
                  }`}
                >
                  <td className="px-5 py-3 font-mono text-xs font-medium text-indigo-700">
                    {f.numero}
                  </td>
                  <td className="px-3 py-3 whitespace-nowrap text-slate-600">{dateFr(f.date)}</td>
                  <td className="px-3 py-3 font-medium text-slate-900">{f.client}</td>
                  <td
                    className={`px-3 py-3 font-mono text-xs ${f.ice === '—' ? 'text-rose-600' : 'text-slate-500'}`}
                  >
                    {f.ice === '—' ? 'manquant' : f.ice}
                  </td>
                  <td className="tabular px-3 py-3 text-right text-slate-600">{num(f.ht, 0)}</td>
                  <td className="tabular px-3 py-3 text-right font-semibold text-slate-900">
                    {num(f.ttc, 0)}
                  </td>
                  <td className="px-3 py-3">
                    <StatutBadge
                      statut={f.statut}
                      suffix={f.anomalies ? `${f.anomalies} err.` : undefined}
                    />
                  </td>
                  <td className="px-5 py-3">
                    <div className="flex justify-end gap-0.5">
                      <IconButton label="Voir le rapport d'audit">
                        <IconEye />
                      </IconButton>
                      <IconButton label="Télécharger le PDF officiel">
                        <IconDownload />
                      </IconButton>
                      <IconButton label="Éditer la facture">
                        <IconEdit />
                      </IconButton>
                    </div>
                  </td>
                </tr>
              ))}
              {lignes.length === 0 && (
                <tr>
                  <td colSpan={8} className="px-5 py-14 text-center text-sm text-slate-500">
                    Aucune facture ne correspond à ces critères.
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      </Card>
    </div>
  )
}
