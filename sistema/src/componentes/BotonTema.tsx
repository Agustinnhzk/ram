/**
 * Igual que en la tienda: el tema vive en el atributo data-tema de <html> (lo pone el script de
 * index.html antes de pintar) y el CSS decide qué ícono se ve. Sin estado de React.
 */
export function BotonTema() {
  function alternar() {
    const html = document.documentElement;
    const nuevo = html.dataset.tema === "claro" ? "oscuro" : "claro";
    html.dataset.tema = nuevo;
    try {
      localStorage.setItem("tema", nuevo);
    } catch {
      // Sin almacenamiento: vale hasta cerrar la pestaña.
    }
  }

  return (
    <button
      type="button"
      onClick={alternar}
      className="grid size-9 place-items-center rounded-full border border-borde text-oro hover:border-oro"
      aria-label="Cambiar entre modo claro y modo oscuro"
      title="Cambiar entre modo claro y modo oscuro"
    >
      <svg viewBox="0 0 24 24" className="icono-sol size-5" fill="none" stroke="currentColor" strokeWidth="1.6" aria-hidden>
        <circle cx="12" cy="12" r="4" />
        <path d="M12 2.5v2M12 19.5v2M4.6 4.6l1.4 1.4M18 18l1.4 1.4M2.5 12h2M19.5 12h2M4.6 19.4 6 18M18 6l1.4-1.4" strokeLinecap="round" />
      </svg>
      <svg viewBox="0 0 24 24" className="icono-luna size-5" fill="none" stroke="currentColor" strokeWidth="1.6" aria-hidden>
        <path d="M20 14.5A8 8 0 0 1 9.5 4a8 8 0 1 0 10.5 10.5Z" strokeLinejoin="round" />
      </svg>
    </button>
  );
}
