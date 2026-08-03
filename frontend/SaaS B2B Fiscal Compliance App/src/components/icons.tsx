type P = { className?: string }
const base = 'h-[18px] w-[18px]'

const svg = (d: string) =>
  function Icon({ className = '' }: P) {
    return (
      <svg
        viewBox="0 0 24 24"
        fill="none"
        stroke="currentColor"
        strokeWidth={1.7}
        strokeLinecap="round"
        strokeLinejoin="round"
        className={`${base} ${className}`}
        aria-hidden
      >
        {d.split('|').map((p, i) => (
          <path key={i} d={p} />
        ))}
      </svg>
    )
  }

export const IconChart = svg('M3 3v18h18|M7 15l4-5 3 3 4-6')
export const IconScan = svg(
  'M3 8V5a2 2 0 0 1 2-2h3|M21 8V5a2 2 0 0 0-2-2h-3|M3 16v3a2 2 0 0 0 2 2h3|M21 16v3a2 2 0 0 1-2 2h-3|M3 12h18',
)
export const IconRegistry = svg('M4 4h16v16H4z|M8 9h8|M8 13h8|M8 17h5')
export const IconPdf = svg('M6 2h8l4 4v16H6z|M14 2v5h5|M9 13h1.5a1.5 1.5 0 0 1 0 3H9v-3z|M9 19v-3')
export const IconShield = svg('M12 3l8 3v6c0 5-3.5 8-8 9-4.5-1-8-4-8-9V6z|M9 12l2 2 4-4')
export const IconTeam = svg(
  'M16 20v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2|M9 10a3 3 0 1 0 0-6 3 3 0 0 0 0 6|M22 20v-2a4 4 0 0 0-3-3.9',
)
export const IconCard = svg('M2 6h20v12H2z|M2 10h20|M6 15h3')
export const IconSearch = svg('M11 19a8 8 0 1 0 0-16 8 8 0 0 0 0 16z|M21 21l-4.3-4.3')
export const IconBell = svg('M18 9a6 6 0 1 0-12 0c0 6-2 7-2 7h16s-2-1-2-7|M13.7 20a2 2 0 0 1-3.4 0')
export const IconChevron = svg('M6 9l6 6 6-6')
export const IconUpload = svg('M12 16V4|M8 8l4-4 4 4|M4 16v2a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2v-2')
export const IconEye = svg('M2 12s3.6-7 10-7 10 7 10 7-3.6 7-10 7-10-7-10-7z|M12 15a3 3 0 1 0 0-6 3 3 0 0 0 0 6')
export const IconDownload = svg('M12 4v12|M8 12l4 4 4-4|M4 20h16')
export const IconEdit = svg('M4 20h4L20 8l-4-4L4 16z|M14 6l4 4')
export const IconAlert = svg('M12 3l9 16H3z|M12 10v4|M12 17h.01')
export const IconCheck = svg('M20 6L9 17l-5-5')
export const IconLogout = svg('M15 4h4v16h-4|M11 16l4-4-4-4|M15 12H3')
export const IconFile = svg('M7 3h7l5 5v13H7z|M14 3v5h5')
export const IconSparkle = svg('M12 3l1.8 5.2L19 10l-5.2 1.8L12 17l-1.8-5.2L5 10l5.2-1.8z')
