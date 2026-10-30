export function PantallaDeCarga({ error = false }: { error?: boolean }) {
  return (
    <div className="grid min-h-dvh place-items-center px-4 text-center">
      <div>
        <img src="/marca/logo.png" alt="" className={`mx-auto size-16 ${error ? "" : "animate-pulse"}`} />
        {error ? (
          <>
            <p className="mt-4 text-principal">No se pudo conectar con el servidor.</p>
            <button type="button" onClick={() => location.reload()} className="mt-3 text-oro underline underline-offset-4">
              Reintentar
            </button>
          </>
        ) : (
          <p className="sr-only">Cargando…</p>
        )}
      </div>
    </div>
  );
}
