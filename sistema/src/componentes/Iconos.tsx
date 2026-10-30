/** Íconos de trazo, en el mismo estilo que la tienda. Decorativos: el texto de al lado dice qué son. */

type Props = { className?: string };

const base = {
  fill: "none",
  stroke: "currentColor",
  strokeWidth: 1.6,
  strokeLinecap: "round" as const,
  strokeLinejoin: "round" as const,
  "aria-hidden": true,
};

export function IconoInicio({ className }: Props) {
  return (
    <svg viewBox="0 0 24 24" className={className} {...base}>
      <path d="M3 11 12 4l9 7" />
      <path d="M5 10v9h5v-5h4v5h5v-9" />
    </svg>
  );
}

export function IconoEtiqueta({ className }: Props) {
  return (
    <svg viewBox="0 0 24 24" className={className} {...base}>
      <path d="M3 12V4h8l10 10-8 8Z" />
      <circle cx="7.5" cy="8.5" r="1.3" />
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

export function IconoCamara({ className }: Props) {
  return (
    <svg viewBox="0 0 24 24" className={className} {...base}>
      <path d="M3.5 7.5 16 4.5l1.6 5.6L5 13.1Z" />
      <path d="M8 12.2 9 16H4.5v4" />
    </svg>
  );
}

export function IconoPersonas({ className }: Props) {
  return (
    <svg viewBox="0 0 24 24" className={className} {...base}>
      <circle cx="9" cy="8" r="3.5" />
      <path d="M2.5 20a6.5 6.5 0 0 1 13 0" />
      <path d="M16 4.5a3.5 3.5 0 0 1 0 7M18 14a6.5 6.5 0 0 1 3.5 6" />
    </svg>
  );
}

export function IconoCaja({ className }: Props) {
  return (
    <svg viewBox="0 0 24 24" className={className} {...base}>
      <path d="m3 7 9-4 9 4v10l-9 4-9-4Z" />
      <path d="m3 7 9 4 9-4M12 11v10" />
    </svg>
  );
}

export function IconoSalir({ className }: Props) {
  return (
    <svg viewBox="0 0 24 24" className={className} {...base}>
      <path d="M14 4h5v16h-5M10 8l-4 4 4 4M6 12h10" />
    </svg>
  );
}

export function IconoReloj({ className }: Props) {
  return (
    <svg viewBox="0 0 24 24" className={className} {...base}>
      <circle cx="12" cy="12" r="8.5" />
      <path d="M12 7.5V12l3 2" />
    </svg>
  );
}

export function IconoPorcentaje({ className }: Props) {
  return (
    <svg viewBox="0 0 24 24" className={className} {...base}>
      <path d="M19 5 5 19" />
      <circle cx="7" cy="7" r="2.5" />
      <circle cx="17" cy="17" r="2.5" />
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

export function IconoCerrar({ className }: Props) {
  return (
    <svg viewBox="0 0 24 24" className={className} {...base}>
      <path d="m6 6 12 12M18 6 6 18" />
    </svg>
  );
}

export function IconoCarrito({ className }: Props) {
  return (
    <svg viewBox="0 0 24 24" className={className} {...base}>
      <circle cx="9" cy="20" r="1.4" />
      <circle cx="18" cy="20" r="1.4" />
      <path d="M2.5 3.5h2.6l2.4 11.2a1.6 1.6 0 0 0 1.6 1.3h8.6a1.6 1.6 0 0 0 1.6-1.2l1.7-7.3H6.1" />
    </svg>
  );
}

export function IconoProductos({ className }: Props) {
  return (
    <svg viewBox="0 0 24 24" className={className} {...base}>
      <rect x="3.5" y="3.5" width="7" height="7" rx="1.2" />
      <rect x="13.5" y="3.5" width="7" height="7" rx="1.2" />
      <rect x="3.5" y="13.5" width="7" height="7" rx="1.2" />
      <rect x="13.5" y="13.5" width="7" height="7" rx="1.2" />
    </svg>
  );
}

export function IconoMegafono({ className }: Props) {
  return (
    <svg viewBox="0 0 24 24" className={className} {...base}>
      <path d="M4 10v4h3l7 4V6L7 10H4Z" />
      <path d="M17.5 9a4 4 0 0 1 0 6" />
      <path d="M8 14.5 9.5 20h2.5l-1-4.5" />
    </svg>
  );
}
