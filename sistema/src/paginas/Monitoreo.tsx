import { useState, type FormEvent } from "react";
import { Link } from "react-router";
import { useAbonos, useAgregarAbonado, useEditarAbonado, useMarcarPago, type FilaDeAbonos } from "../api/instalaciones";
import { useSesion } from "../api/sesion";
import { Campo, ESTILO_DE_CAMPO } from "../componentes/Campo";
import { useAvisar } from "../componentes/contextoDeAvisos";
import { Dialogo } from "../componentes/Dialogo";
import { ElegirCliente, type ClienteElegido } from "../componentes/ElegirCliente";
import { formatearPrecio, leerPrecio } from "../utilidades/formato";

const MESES = ["Ene", "Feb", "Mar", "Abr", "May", "Jun", "Jul", "Ago", "Sep", "Oct", "Nov", "Dic"];
const MESES_LARGOS = ["enero", "febrero", "marzo", "abril", "mayo", "junio", "julio", "agosto", "septiembre", "octubre", "noviembre", "diciembre"];

/** /monitoreo — la planilla de abonos: cada abonado, de enero a diciembre, si pagó o no (ADR-015). */
export function Monitoreo() {
  const [anio, setAnio] = useState(() => new Date().getFullYear());
  const { data: filas, isPending, isError, error } = useAbonos(anio);
  const marcar = useMarcarPago(anio);
  const { data: sesion } = useSesion();
  const avisar = useAvisar();
  const [nuevoAbierto, setNuevoAbierto] = useState(false);
  const [editando, setEditando] = useState<FilaDeAbonos | null>(null);
  const manejaPlata = sesion?.rol === "DUENO" || sesion?.rol === "MOSTRADOR";

  function cambiar(fila: FilaDeAbonos, mes: number) {
    const pagado = !fila.pagos[mes];
    marcar.mutate({ abonado: fila.id, mes: mes + 1, pagado }, { onError: (e) => avisar(e.message, "error") });
  }

  return (
    <>
      <div className="flex flex-wrap items-end justify-between gap-4">
        <div>
          <h1 className="texto-metalico text-4xl sm:text-5xl">Monitoreo</h1>
          <p className="mt-2 text-suave">
            Abonos mensuales: tocá un mes para marcar si pagó.{!manejaPlata && " Los pagos los marcan el Dueño y Mostrador."}
          </p>
        </div>
        <button type="button" onClick={() => setNuevoAbierto(true)} className="boton-oro rounded-full px-5 py-2.5 font-semibold hover:brightness-110">
          + Nuevo abonado
        </button>
      </div>

      <div className="mt-6 flex items-center gap-3">
        <button type="button" onClick={() => setAnio(anio - 1)} aria-label="Año anterior" className="grid size-9 place-items-center rounded-full border border-borde hover:border-oro">
          ←
        </button>
        <span className="numeros text-2xl font-semibold">{anio}</span>
        <button type="button" onClick={() => setAnio(anio + 1)} aria-label="Año siguiente" className="grid size-9 place-items-center rounded-full border border-borde hover:border-oro">
          →
        </button>
      </div>

      {isPending && <div aria-hidden className="mt-4 h-60 animate-pulse rounded-xl bg-superficie" />}
      {isError && (
        <p role="alert" className="mt-4 rounded-xl border border-peligro/60 bg-peligro/10 p-4">
          No se pudo cargar: {error.message}
        </p>
      )}
      {filas && filas.length === 0 && <p className="mt-4 rounded-xl border border-dashed border-borde px-6 py-12 text-center">Todavía no hay abonados.</p>}
      {filas && filas.length > 0 && (
        <div className="mt-4 overflow-x-auto rounded-xl border border-borde">
          <table className="w-full min-w-[56rem] text-sm">
            <thead className="bg-superficie text-xs text-suave">
              <tr>
                <th scope="col" className="sticky left-0 bg-superficie px-3 py-2 text-left font-medium">Cliente</th>
                <th scope="col" className="px-2 py-2 text-right font-medium">Abono</th>
                {MESES.map((m) => (
                  <th key={m} scope="col" className="px-1 py-2 font-medium">{m}</th>
                ))}
              </tr>
            </thead>
            <tbody className="divide-y divide-borde">
              {filas.map((f) => (
                <tr key={f.id} className={f.activo ? "" : "opacity-50"}>
                  <th scope="row" className="sticky left-0 bg-fondo px-3 py-2 text-left font-normal">
                    <button type="button" onClick={() => setEditando(f)} className="text-left hover:text-oro-claro">
                      <span className="block font-semibold">{f.cliente}</span>
                      <span className="block text-xs text-suave">{f.activo ? f.direccion : "Dado de baja"}</span>
                    </button>
                  </th>
                  <td className="numeros px-2 py-2 text-right whitespace-nowrap text-suave">{f.monto ? formatearPrecio(f.monto) : "—"}</td>
                  {f.pagos.map((pago, mes) => (
                    <td key={mes} className="px-1 py-1.5 text-center">
                      <button
                        type="button"
                        disabled={!manejaPlata || marcar.isPending}
                        aria-pressed={pago}
                        aria-label={`${f.cliente}, ${MESES_LARGOS[mes]}: ${pago ? "pagó" : "no pagó"}`}
                        onClick={() => cambiar(f, mes)}
                        className={`w-11 rounded-md border py-1 text-xs font-semibold disabled:cursor-default ${
                          pago ? "border-exito/60 bg-exito/15 text-exito" : "border-borde text-suave hover:border-oro"
                        }`}
                      >
                        {pago ? "Sí" : "No"}
                      </button>
                    </td>
                  ))}
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      <DialogoNuevoAbonado abierto={nuevoAbierto} alCerrar={() => setNuevoAbierto(false)} />
      <DialogoEditarAbonado fila={editando} alCerrar={() => setEditando(null)} />
    </>
  );
}

function DialogoNuevoAbonado({ abierto, alCerrar }: { abierto: boolean; alCerrar: () => void }) {
  const agregar = useAgregarAbonado();
  const avisar = useAvisar();
  const [cliente, setCliente] = useState<ClienteElegido | null>(null);
  const [direccion, setDireccion] = useState("");
  const [monto, setMonto] = useState("");

  function enviar(evento: FormEvent) {
    evento.preventDefault();
    if (!cliente) return avisar("Elegí o cargá el cliente", "error");
    agregar.mutate(
      { ...cliente, direccion: direccion.trim() || null, monto: monto.trim() ? leerPrecio(monto) : null },
      { onSuccess: () => { avisar("Abonado agregado"); setDireccion(""); setMonto(""); alCerrar(); } },
    );
  }

  return (
    <Dialogo abierto={abierto} alCerrar={alCerrar} titulo="Nuevo abonado" ancho="max-w-lg">
      <form onSubmit={enviar} className="space-y-4">
        <ElegirCliente key={String(abierto)} alCambiar={setCliente} />
        <Campo id="abono-direccion" etiqueta="Dirección monitoreada" opcional>
          <input id="abono-direccion" maxLength={300} value={direccion} onChange={(e) => setDireccion(e.target.value)} className={ESTILO_DE_CAMPO} />
        </Campo>
        <Campo id="abono-monto" etiqueta="Abono mensual" opcional>
          <input id="abono-monto" inputMode="numeric" placeholder="$" value={monto} onChange={(e) => setMonto(e.target.value)} className={ESTILO_DE_CAMPO} />
        </Campo>
        {agregar.isError && <p role="alert" className="text-sm text-peligro">{agregar.error.message}</p>}
        <div className="flex justify-end">
          <button type="submit" disabled={agregar.isPending} className="boton-oro rounded-full px-5 py-2 text-sm font-semibold disabled:opacity-50">
            Agregar
          </button>
        </div>
      </form>
    </Dialogo>
  );
}

function DialogoEditarAbonado({ fila, alCerrar }: { fila: FilaDeAbonos | null; alCerrar: () => void }) {
  return (
    <Dialogo abierto={fila !== null} alCerrar={alCerrar} titulo={fila?.cliente ?? ""} ancho="max-w-lg">
      {fila && <FormularioAbonado key={fila.id} fila={fila} alCerrar={alCerrar} />}
    </Dialogo>
  );
}

function FormularioAbonado({ fila, alCerrar }: { fila: FilaDeAbonos; alCerrar: () => void }) {
  const editar = useEditarAbonado();
  const avisar = useAvisar();
  const [direccion, setDireccion] = useState(fila.direccion ?? "");
  const [monto, setMonto] = useState(fila.monto?.toString() ?? "");
  const [activo, setActivo] = useState(fila.activo);

  function enviar(evento: FormEvent) {
    evento.preventDefault();
    editar.mutate(
      { id: fila.id, direccion: direccion.trim() || null, monto: monto.trim() ? leerPrecio(monto) : null, activo },
      { onSuccess: () => { avisar("Guardado"); alCerrar(); } },
    );
  }

  return (
    <form onSubmit={enviar} className="space-y-4">
      <Link to={`/clientes/${fila.clienteId}`} className="text-sm text-oro-claro underline underline-offset-4">
        Ver la ficha del cliente
      </Link>
      <Campo id="editar-direccion" etiqueta="Dirección monitoreada" opcional>
        <input id="editar-direccion" maxLength={300} value={direccion} onChange={(e) => setDireccion(e.target.value)} className={ESTILO_DE_CAMPO} />
      </Campo>
      <Campo id="editar-monto" etiqueta="Abono mensual" opcional>
        <input id="editar-monto" inputMode="numeric" value={monto} onChange={(e) => setMonto(e.target.value)} className={ESTILO_DE_CAMPO} />
      </Campo>
      <label className="flex items-center gap-2 text-sm">
        <input type="checkbox" checked={activo} onChange={(e) => setActivo(e.target.checked)} className="size-4 accent-[var(--color-oro)]" />
        Abono activo (destildalo si se dio de baja)
      </label>
      {editar.isError && <p role="alert" className="text-sm text-peligro">{editar.error.message}</p>}
      <div className="flex justify-end">
        <button type="submit" disabled={editar.isPending} className="boton-oro rounded-full px-5 py-2 text-sm font-semibold disabled:opacity-50">
          Guardar
        </button>
      </div>
    </form>
  );
}
