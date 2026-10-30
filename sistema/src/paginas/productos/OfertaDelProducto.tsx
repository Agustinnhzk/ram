import { useState, type FormEvent } from "react";
import { useOferta, type DetalleDeProducto } from "../../api/productos";
import { ESTILO_DE_CAMPO } from "../../componentes/Campo";
import { useAvisar } from "../../componentes/contextoDeAvisos";
import { formatearPrecio, leerPrecio } from "../../utilidades/formato";
import { formatearDia } from "../../utilidades/reparaciones";

/** Precio de oferta hasta un día. La tienda muestra el de lista tachado y cobra el de oferta (ADR-013). */
export function OfertaDelProducto({ producto, soloLectura }: { producto: DetalleDeProducto; soloLectura: boolean }) {
  const oferta = useOferta(producto.id);
  const avisar = useAvisar();
  const [precio, setPrecio] = useState(producto.precioOferta ? String(producto.precioOferta) : "");
  const [hasta, setHasta] = useState(producto.ofertaHasta ?? "");
  const vencida = producto.precioOferta !== null && !producto.enOferta;

  function guardar(evento: FormEvent) {
    evento.preventDefault();
    const valor = leerPrecio(precio);
    if (valor === null) return avisar("El precio de oferta tiene que ser un número de pesos", "error");
    oferta.mutate({ precio: valor, hasta }, { onSuccess: () => avisar("Oferta guardada"), onError: (e) => avisar(e.message, "error") });
  }

  function quitar() {
    oferta.mutate(null, {
      onSuccess: () => {
        avisar("Oferta quitada");
        setPrecio("");
        setHasta("");
      },
      onError: (e) => avisar(e.message, "error"),
    });
  }

  return (
    <form onSubmit={guardar} className="rounded-xl border border-borde bg-superficie p-5">
      <h2 className="text-2xl">Oferta</h2>
      {producto.enOferta && producto.precioOferta !== null && producto.ofertaHasta && (
        <p className="mt-1 text-sm text-exito">
          En oferta: {formatearPrecio(producto.precioOferta)} hasta el {formatearDia(producto.ofertaHasta)}.
        </p>
      )}
      {vencida && <p className="mt-1 text-sm text-aviso">La oferta cargada ya no está vigente.</p>}
      {producto.precio === null && <p className="mt-1 text-sm text-suave">Para poner una oferta, primero cargale un precio de lista.</p>}
      <fieldset disabled={soloLectura || producto.precio === null} className="mt-3 grid gap-3 sm:grid-cols-2 lg:grid-cols-1 xl:grid-cols-2">
        <div>
          <label htmlFor="precio-oferta" className="text-sm text-suave">
            Precio de oferta
          </label>
          <input id="precio-oferta" required inputMode="numeric" value={precio} onChange={(e) => setPrecio(e.target.value)} className={`numeros mt-1 ${ESTILO_DE_CAMPO}`} />
        </div>
        <div>
          <label htmlFor="oferta-hasta" className="text-sm text-suave">
            Hasta el (inclusive)
          </label>
          <input id="oferta-hasta" required type="date" value={hasta} onChange={(e) => setHasta(e.target.value)} className={`mt-1 ${ESTILO_DE_CAMPO}`} />
        </div>
        <div className="flex gap-2 sm:col-span-2 lg:col-span-1 xl:col-span-2">
          <button type="submit" disabled={oferta.isPending} className="boton-oro rounded-full px-5 py-2 text-sm font-semibold hover:brightness-110">
            Guardar oferta
          </button>
          {producto.precioOferta !== null && (
            <button type="button" onClick={quitar} disabled={oferta.isPending} className="rounded-full border border-borde px-4 py-2 text-sm hover:border-oro">
              Quitar
            </button>
          )}
        </div>
      </fieldset>
    </form>
  );
}
