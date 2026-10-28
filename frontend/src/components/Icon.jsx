const paths = {
  equipment: <><rect x="3" y="4" width="18" height="12" rx="2" /><path d="M8 20h8m-6-4v4m4-4v4" /></>,
  arrow: <path d="M5 12h14m-5-5 5 5-5 5" />,
  refresh: <><path d="M20 7v5h-5M4 17v-5h5" /><path d="M6.1 7a7 7 0 0 1 11.6-1L20 9M4 15l2.3 3A7 7 0 0 0 17.9 17" /></>,
  search: <><circle cx="10.5" cy="10.5" r="6.5" /><path d="m16 16 4 4" /></>,
  check: <path d="m5 12 4 4L19 6" />,
  clock: <><circle cx="12" cy="12" r="9" /><path d="M12 7v5l3 2" /></>,
  lock: <><rect x="5" y="10" width="14" height="11" rx="2" /><path d="M8 10V7a4 4 0 0 1 8 0v3m-4 5v2" /></>,
  logout: <><path d="M9 4H4v16h5m6-12 4 4-4 4m-6-4h10" /></>,
  alert: <><path d="m12 3 10 18H2L12 3Zm0 6v5m0 3h.01" /></>,
  laptop: <><rect x="5" y="4" width="14" height="12" rx="1" /><path d="m5 16-3 4h20l-3-4" /></>,
  projector: <><rect x="3" y="6" width="18" height="12" rx="3" /><circle cx="15" cy="12" r="3" /><path d="M6 10h2m-2 4h2M7 18v3m10-3v3" /></>,
  tablet: <><rect x="5" y="2" width="14" height="20" rx="2" /><path d="M11 18h2" /></>,
};

export default function Icon({ name, size = 20, className = '' }) {
  return (
    <svg width={size} height={size} viewBox="0 0 24 24" fill="none" stroke="currentColor"
      strokeWidth="1.7" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true" className={className}>
      {paths[name] || paths.equipment}
    </svg>
  );
}
