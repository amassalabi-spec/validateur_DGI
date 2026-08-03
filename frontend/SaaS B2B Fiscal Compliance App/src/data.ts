export type Statut = 'conforme' | 'non-conforme' | 'en-attente'

export type Facture = {
  numero: string
  date: string
  client: string
  ice: string
  ht: number
  tva: number
  ttc: number
  reglement: 'Virement' | 'Chèque' | 'Espèces' | 'Effet'
  statut: Statut
  anomalies: number
}

export const REGISTRE: Facture[] = [
  {
    numero: 'FAC-2026-0184',
    date: '2026-07-28',
    client: 'Marsa Maroc Logistique SA',
    ice: '001789456000031',
    ht: 148_600,
    tva: 29_720,
    ttc: 178_320,
    reglement: 'Virement',
    statut: 'conforme',
    anomalies: 0,
  },
  {
    numero: 'FAC-2026-0183',
    date: '2026-07-26',
    client: 'Atlas Textile Industries SARL',
    ice: '—',
    ht: 62_400,
    tva: 12_480,
    ttc: 74_880,
    reglement: 'Espèces',
    statut: 'non-conforme',
    anomalies: 3,
  },
  {
    numero: 'FAC-2026-0182',
    date: '2026-07-24',
    client: 'Cosumar Distribution',
    ice: '000512334000078',
    ht: 231_050,
    tva: 23_105,
    ttc: 254_155,
    reglement: 'Effet',
    statut: 'conforme',
    anomalies: 0,
  },
  {
    numero: 'FAC-2026-0181',
    date: '2026-07-21',
    client: 'Sotherma Boissons SA',
    ice: '002145879000012',
    ht: 89_700,
    tva: 17_940,
    ttc: 107_640,
    reglement: 'Espèces',
    statut: 'non-conforme',
    anomalies: 1,
  },
  {
    numero: 'FAC-2026-0180',
    date: '2026-07-19',
    client: 'Managem Services Miniers',
    ice: '000998112000045',
    ht: 412_800,
    tva: 82_560,
    ttc: 495_360,
    reglement: 'Virement',
    statut: 'conforme',
    anomalies: 0,
  },
  {
    numero: 'FAC-2026-0179',
    date: '2026-07-15',
    client: 'Ynna Asment Béton',
    ice: '001334567000090',
    ht: 74_250,
    tva: 14_850,
    ttc: 89_100,
    reglement: 'Chèque',
    statut: 'en-attente',
    anomalies: 0,
  },
  {
    numero: 'FAC-2026-0178',
    date: '2026-07-12',
    client: 'Label Vie Retail SA',
    ice: '000441209000027',
    ht: 156_900,
    tva: 15_690,
    ttc: 172_590,
    reglement: 'Virement',
    statut: 'conforme',
    anomalies: 0,
  },
  {
    numero: 'FAC-2026-0177',
    date: '2026-07-08',
    client: 'Delassus Agro Export',
    ice: '003201884000019',
    ht: 98_300,
    tva: 19_660,
    ttc: 117_960,
    reglement: 'Espèces',
    statut: 'non-conforme',
    anomalies: 2,
  },
  {
    numero: 'FAC-2026-0176',
    date: '2026-07-03',
    client: 'Afriquia Gaz Transport',
    ice: '000776554000063',
    ht: 305_400,
    tva: 61_080,
    ttc: 366_480,
    reglement: 'Effet',
    statut: 'conforme',
    anomalies: 0,
  },
  {
    numero: 'FAC-2026-0175',
    date: '2026-06-29',
    client: 'Ciments du Maroc Négoce',
    ice: '001002347000054',
    ht: 187_200,
    tva: 37_440,
    ttc: 224_640,
    reglement: 'Virement',
    statut: 'conforme',
    anomalies: 0,
  },
]

export const TVA_MENSUELLE = [
  { mois: 'Jan', taux20: 128_400, taux10: 31_200, timbre: 1_140 },
  { mois: 'Fév', taux20: 141_900, taux10: 26_800, timbre: 980 },
  { mois: 'Mar', taux20: 164_300, taux10: 34_500, timbre: 1_620 },
  { mois: 'Avr', taux20: 152_700, taux10: 29_100, timbre: 1_210 },
  { mois: 'Mai', taux20: 178_500, taux10: 41_300, timbre: 1_875 },
  { mois: 'Jun', taux20: 191_200, taux10: 38_700, timbre: 2_040 },
  { mois: 'Jul', taux20: 206_800, taux10: 44_100, timbre: 2_310 },
]

export const CONFORMITE_MENSUELLE = [
  { mois: 'Jan', taux: 71 },
  { mois: 'Fév', taux: 76 },
  { mois: 'Mar', taux: 74 },
  { mois: 'Avr', taux: 83 },
  { mois: 'Mai', taux: 88 },
  { mois: 'Jun', taux: 90 },
  { mois: 'Jul', taux: 92 },
]

export const ANOMALIES = [
  { motif: 'ICE client manquant', nb: 34 },
  { motif: 'Écart de calcul TVA', nb: 21 },
  { motif: 'Timbre fiscal absent', nb: 17 },
  { motif: 'Mention légale Art. 145', nb: 12 },
  { motif: 'Numérotation non séquentielle', nb: 6 },
]

export const REGLEMENTS = [
  { mode: 'Virement', montant: 1_264_800 },
  { mode: 'Effet', montant: 620_635 },
  { mode: 'Chèque', montant: 389_100 },
  { mode: 'Espèces', montant: 281_480 },
]

export const PALETTE = {
  indigo: '#4f46e5',
  emerald: '#059669',
  amber: '#f59e0b',
  sky: '#0ea5e9',
  rose: '#e11d48',
}

export const mad = (n: number) =>
  new Intl.NumberFormat('fr-MA', { maximumFractionDigits: 0 }).format(n) + ' MAD'

export const num = (n: number, d = 2) =>
  new Intl.NumberFormat('fr-MA', { minimumFractionDigits: d, maximumFractionDigits: d }).format(n)

export const dateFr = (iso: string) =>
  new Date(iso).toLocaleDateString('fr-FR', { day: '2-digit', month: 'short', year: 'numeric' })
