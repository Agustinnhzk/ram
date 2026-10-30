import type { ReactNode } from "react";
import { Link, useParams } from "react-router";
import { useOrden, useTaller } from "../../api/reparaciones";
import { formatearFecha } from "../../utilidades/formato";
import { linkDeSeguimiento } from "../../utilidades/reparaciones";

/**
 * /reparaciones/:id/comprobante — el comprobante de ingreso para el cliente (ADR-009).
 * Es una "hoja" blanca en cualquier tema, porque va al papel. Se imprime con el botón, o con
 * "Guardar como PDF" en la ventana de impresión del navegador.
 *
 * NO lleva el password ni las observaciones internas.
 */
export function Comprobante() {
  const { id = "" } = useParams();
  const { data: orden, isPending, isError } = useOrden(id);
  const { data: taller } = useTaller();

  if (isPending) return <p className="p-8 text-suave">Cargando…</p>;
  if (isError) return <p className="p-8">No se pudo cargar la orden.</p>;

  return (
    <div className="min-h-dvh py-8 print:py-0">
      <div className="no-imprimir mx-auto mb-6 flex max-w-[52rem] items-center justify-between gap-4 px-4">
        <Link to={`/reparaciones/${orden.id}`} className="text-sm text-suave hover:text-oro-claro">
          ← Volver a la orden
        </Link>
        <button type="button" onClick={() => window.print()} className="boton-oro rounded-full px-5 py-2 font-semibold hover:brightness-110">
          Imprimir
        </button>
      </div>

      <article className="mx-auto max-w-[52rem] bg-white p-10 text-neutral-900 shadow-xl print:max-w-none print:p-0 print:shadow-none">
        <header className="flex items-start justify-between gap-6 border-b-2 border-neutral-900 pb-5">
          <div className="flex items-center gap-4">
            <img src="/marca/logo.png" alt="" className="size-16" />
            <div>
              <p className="font-display text-2xl font-extrabold italic">{taller?.nombre ?? ""}</p>
              <p className="text-sm text-neutral-600">Comprobante de ingreso a reparación</p>
            </div>
          </div>
          <div className="text-right">
            <p className="text-xs tracking-wide text-neutral-600 uppercase">Orden N°</p>
            <p className="numeros font-display text-4xl font-black">{orden.numero}</p>
            <p className="text-sm text-neutral-600">{formatearFecha(orden.ingresadoEn)}</p>
          </div>
        </header>

        <section className="mt-6 grid gap-6 sm:grid-cols-2">
          <Bloque titulo="Cliente">
            <p className="font-semibold">{orden.cliente?.nombreCompleto}</p>
            <p className="numeros">{orden.cliente?.telefono}</p>
            {orden.cliente?.correo && <p>{orden.cliente.correo}</p>}
          </Bloque>
          <Bloque titulo="Equipo">
            <p className="font-semibold">{orden.equipo}</p>
            {orden.procesador && <p>Procesador: {orden.procesador}</p>}
            <p>Cargador: {orden.conCargador ? "sí" : "no"}</p>
          </Bloque>
        </section>

        <Bloque titulo="Problema informado" className="mt-6">
          <p className="whitespace-pre-line">{orden.problema}</p>
        </Bloque>

        <section className="mt-6 rounded-lg border border-neutral-300 p-4">
          <h2 className="font-sans text-sm font-semibold not-italic tracking-wide uppercase">Seguí tu reparación</h2>
          <p className="mt-1">
            Entrá a <strong>{linkDeSeguimiento(orden.numero)}</strong> con el número de orden <strong>{orden.numero}</strong> y los
            últimos 4 números de tu teléfono.
          </p>
        </section>

        <section className="mt-6 text-sm text-neutral-700">
          <h2 className="font-sans text-sm font-semibold not-italic tracking-wide text-neutral-900 uppercase">Condiciones</h2>
          <ul className="mt-2 list-disc space-y-1 pl-5">
            <li>Antes de reparar te informamos el presupuesto, y reparamos solo si lo aprobás.</li>
            <li>Si no se logra reparar el equipo, no se cobra.</li>
            <li>Para retirar el equipo presentá este comprobante o indicá el número de orden.</li>
          </ul>
        </section>

        <footer className="mt-14 grid grid-cols-2 gap-10 text-center text-sm text-neutral-600">
          <div className="border-t border-neutral-500 pt-2">Firma del cliente</div>
          <div className="border-t border-neutral-500 pt-2">Recibió (taller)</div>
        </footer>
      </article>
    </div>
  );
}

function Bloque({ titulo, children, className = "" }: { titulo: string; children: ReactNode; className?: string }) {
  return (
    <section className={className}>
      <h2 className="font-sans text-xs font-semibold not-italic tracking-wide text-neutral-600 uppercase">{titulo}</h2>
      <div className="mt-1">{children}</div>
    </section>
  );
}
