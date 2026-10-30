/**
 * Íconos de trazo en SVG, en el estilo de línea dorada de los flyers.
 * Son decorativos: aria-hidden, y el texto de al lado dice qué significan.
 */

type Props = { className?: string };

const base = {
  fill: "none",
  stroke: "currentColor",
  strokeWidth: 1.6,
  strokeLinecap: "round" as const,
  strokeLinejoin: "round" as const,
  "aria-hidden": true,
};

export function IconoCarrito({ className }: Props) {
  return (
    <svg viewBox="0 0 24 24" className={className} {...base}>
      <path d="M3 4h2l2.4 11.2a1.5 1.5 0 0 0 1.5 1.2h8.7a1.5 1.5 0 0 0 1.5-1.1L21 8H6.2" />
      <circle cx="9.5" cy="20" r="1.3" />
      <circle cx="17.5" cy="20" r="1.3" />
    </svg>
  );
}

export function IconoLupa({ className }: Props) {
  return (
    <svg viewBox="0 0 24 24" className={className} {...base}>
      <circle cx="11" cy="11" r="6.5" />
      <path d="m20 20-4.2-4.2" />
    </svg>
  );
}

export function IconoNotebook({ className }: Props) {
  return (
    <svg viewBox="0 0 24 24" className={className} {...base}>
      <rect x="4" y="5" width="16" height="11" rx="1.5" />
      <path d="M2 19h20" />
    </svg>
  );
}

export function IconoEscudo({ className }: Props) {
  return (
    <svg viewBox="0 0 24 24" className={className} {...base}>
      <path d="M12 3 5 6v5.5c0 4.3 3 8 7 9.5 4-1.5 7-5.2 7-9.5V6l-7-3Z" />
      <path d="m9 12 2 2 4-4" />
    </svg>
  );
}

export function IconoCheck({ className }: Props) {
  return (
    <svg viewBox="0 0 24 24" className={className} {...base}>
      <path d="m5 12.5 4.5 4.5L19 7.5" />
    </svg>
  );
}

export function IconoDomicilio({ className }: Props) {
  return (
    <svg viewBox="0 0 24 24" className={className} {...base}>
      <path d="M3 11 12 4l9 7" />
      <path d="M5 10v9h5v-5h4v5h5v-9" />
    </svg>
  );
}

export function IconoInstagram({ className }: Props) {
  return (
    <svg viewBox="0 0 24 24" className={className} {...base}>
      <rect x="3.5" y="3.5" width="17" height="17" rx="5" />
      <circle cx="12" cy="12" r="4" />
      <circle cx="17.2" cy="6.8" r="0.6" fill="currentColor" />
    </svg>
  );
}

/** El logo de WhatsApp va relleno, como lo reconoce la gente. */
export function IconoWhatsApp({ className }: Props) {
  return (
    <svg viewBox="0 0 24 24" className={className} fill="currentColor" aria-hidden>
      <path d="M12 2a10 10 0 0 0-8.6 15.1L2 22l5-1.3A10 10 0 1 0 12 2Zm0 18.2c-1.6 0-3.1-.4-4.4-1.2l-.3-.2-3 .8.8-2.9-.2-.3A8.2 8.2 0 1 1 12 20.2Zm4.5-6.1c-.2-.1-1.5-.7-1.7-.8-.2-.1-.4-.1-.6.1l-.8 1c-.1.2-.3.2-.5.1a6.7 6.7 0 0 1-3.3-2.9c-.3-.4.2-.4.7-1.4.1-.2 0-.3 0-.4l-.8-1.8c-.2-.5-.4-.4-.6-.4h-.5a1 1 0 0 0-.7.3 3 3 0 0 0-.9 2.2c0 1.3.9 2.5 1 2.7.1.2 1.8 2.8 4.4 3.9 1.6.7 2.3.8 3.1.6.5-.1 1.5-.6 1.7-1.2.2-.6.2-1.1.1-1.2l-.6-.3Z" />
    </svg>
  );
}

export function IconoCorreo({ className }: Props) {
  return (
    <svg viewBox="0 0 24 24" className={className} fill="none" stroke="currentColor" strokeWidth={1.8} strokeLinecap="round" strokeLinejoin="round" aria-hidden>
      <rect x="3" y="5" width="18" height="14" rx="2" />
      <path d="m3 7 9 6 9-6" />
    </svg>
  );
}

export function IconoCamara({ className }: Props) {
  return (
    <svg viewBox="0 0 24 24" className={className} {...base}>
      <path d="M3.5 7.5 16 4.5l1.6 5.6L5 13.1Z" />
      <circle cx="14.2" cy="8.2" r="1.2" />
      <path d="M8 12.2 9 16H4.5v4" />
      <path d="m18 9 2.5-.8" />
    </svg>
  );
}

export function IconoAlarma({ className }: Props) {
  return (
    <svg viewBox="0 0 24 24" className={className} {...base}>
      <path d="M6 18v-6a6 6 0 0 1 12 0v6" />
      <path d="M4 18h16v2.5H4Z" />
      <path d="M12 2.5v2M4.2 5.2l1.4 1.4M19.8 5.2l-1.4 1.4" />
    </svg>
  );
}

export function IconoMonitoreo({ className }: Props) {
  return (
    <svg viewBox="0 0 24 24" className={className} {...base}>
      <path d="M2.5 12S6 5.5 12 5.5 21.5 12 21.5 12 18 18.5 12 18.5 2.5 12 2.5 12Z" />
      <circle cx="12" cy="12" r="3" />
    </svg>
  );
}

export function IconoPc({ className }: Props) {
  return (
    <svg viewBox="0 0 24 24" className={className} {...base}>
      <rect x="7" y="2.5" width="10" height="19" rx="1.5" />
      <circle cx="12" cy="15.5" r="2" />
      <path d="M9.5 6h5M9.5 8.5h5" />
    </svg>
  );
}

export function IconoHerramienta({ className }: Props) {
  return (
    <svg viewBox="0 0 24 24" className={className} {...base}>
      <path d="M14.5 6.5a4 4 0 0 0 5 5l-8.8 8.8a2 2 0 0 1-2.8-2.8l8.8-8.8a4 4 0 0 1 5-5l-2.7 2.7.3 2.2 2.2.3" />
    </svg>
  );
}

export function IconoCodigo({ className }: Props) {
  return (
    <svg viewBox="0 0 24 24" className={className} {...base}>
      <path d="m8 7-5 5 5 5M16 7l5 5-5 5M13.5 5l-3 14" />
    </svg>
  );
}

export function IconoChip({ className }: Props) {
  return (
    <svg viewBox="0 0 24 24" className={className} {...base}>
      <rect x="6" y="6" width="12" height="12" rx="1.5" />
      <rect x="9.5" y="9.5" width="5" height="5" rx="0.5" />
      <path d="M9 2.5V6M15 2.5V6M9 18v3.5M15 18v3.5M2.5 9H6M2.5 15H6M18 9h3.5M18 15h3.5" />
    </svg>
  );
}

export function IconoMother({ className }: Props) {
  return (
    <svg viewBox="0 0 24 24" className={className} {...base}>
      <rect x="3" y="3" width="18" height="18" rx="1.5" />
      <rect x="6" y="6" width="5" height="5" />
      <path d="M14 6h4M14 8.5h4M6 14.5h12M6 17.5h12" />
    </svg>
  );
}

export function IconoMemoria({ className }: Props) {
  return (
    <svg viewBox="0 0 24 24" className={className} {...base}>
      <path d="M2.5 7.5h19v8h-19Z" />
      <path d="M5.5 10h3v3h-3ZM10.5 10h3v3h-3ZM15.5 10h3v3h-3Z" />
      <path d="M5 15.5v2M8 15.5v2M11 15.5v2M14 15.5v2M17 15.5v2M20 15.5v2" />
    </svg>
  );
}

export function IconoPlacaDeVideo({ className }: Props) {
  return (
    <svg viewBox="0 0 24 24" className={className} {...base}>
      <rect x="2.5" y="6" width="19" height="10.5" rx="1.5" />
      <circle cx="8.5" cy="11.25" r="3" />
      <circle cx="15.5" cy="11.25" r="3" />
      <path d="M4 16.5v2.5h7v-2.5" />
    </svg>
  );
}

export function IconoDisco({ className }: Props) {
  return (
    <svg viewBox="0 0 24 24" className={className} {...base}>
      <rect x="2.5" y="8" width="19" height="8" rx="1.5" />
      <path d="M5.5 11h6M5.5 13h4" />
      <circle cx="17.5" cy="12" r="1" />
    </svg>
  );
}

export function IconoFuente({ className }: Props) {
  return (
    <svg viewBox="0 0 24 24" className={className} {...base}>
      <rect x="3" y="5" width="18" height="14" rx="1.5" />
      <path d="m12.5 8-3 4.5h4l-3 4.5" />
    </svg>
  );
}
