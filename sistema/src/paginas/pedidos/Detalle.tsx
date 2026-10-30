import { useState, type FormEvent } from "react";
import { Link, useNavigate, useParams } from "react-router";
import { useBorrarPedido, usePedido, useAccionSobrePedido, type AccionDePedido, type Comprobante, type DetalleDePedido } from "../../api/pedidos";
import { useCambiarStock, useReservasDePedido } from "../../api/stock";
import { useSesion } from "../../api/sesion";
import { BotonBorrar } from "../../componentes/BotonBorrar";
import { ESTILO_DE_CAMPO } from "../../componentes/Campo";
import { useAvisar } from "../../componentes/contextoDeAvisos";
import { Dialogo } from "../../componentes/Dialogo";
import { EtiquetaDePedido } from "../../componentes/EtiquetaDePedido";
import { IconoWhatsApp } from "../../componentes/IconoWhatsApp";
import { formatearFecha, formatearPrecio } from "../../utilidades/formato";
import { ENTREGAS, linkWhatsAppDelCliente } from "../../utilidades/pedidos";

/** /pedidos/:id — el pedido, el comprobante y los botones para el próximo paso. */
export function Detalle() {
  const { id = "" } = useParams();
  const { data: pedido, isPending, isError, error } = usePedido(id);
  const { data: sesion } = useSesion();
  // Lo que tiene que ver con plata (confirmar, rechazar, cancelar) lo hacen Dueño y Mostrador (ADR-011).
  const manejaPlata = sesion?.rol === "DUENO" || sesion?.rol === "MOSTRADOR";
  const borrar = useBorrarPedido(id);
  const navegar = useNavigate();

  if (isPending) return <div aria-hidden className="h-96 animate-pulse rounded-xl bg-superficie" />;
  if (isError) {
    return (
      <p role="alert" className="rounded-xl border border-peligro/60 bg-peligro/10 p-4">
        No se pudo cargar el pedido: {error.message}
      </p>
    );
  }

  const ultimo = pedido.comprobantes.at(-1);

  return (
    <>
      <Link to="/pedidos" className="text-sm text-suave hover:text-oro-claro">
        ← Pedidos
      </Link>
      <div className="mt-2 flex flex-wrap items-center gap-x-4 gap-y-2">
        <h1 className="texto-metalico text-4xl sm:text-5xl">
          Pedido <span className="numeros">{pedido.numero}</span>
        </h1>
        <EtiquetaDePedido estado={pedido.estado} />
      </div>
      <p className="mt-1 text-suave">Hecho el {formatearFecha(pedido.creadoEn)}</p>

      <div className="mt-8 grid items-start gap-6 lg:grid-cols-[1fr_20rem]">
        <div className="space-y-6">
          {pedido.estado === "PAGO_INFORMADO" && ultimo && (
            <section className="rounded-xl border border-info/50 bg-info/5 p-5">
              <h2 className="text-2xl">Revisá el pago</h2>
              <p className="mt-1 text-sm text-suave">
                Fijate en tu cuenta que haya entrado <strong className="text-principal">{formatearPrecio(pedido.total)}</strong> y
                confirmalo. Si no llegó o es otro monto, rechazá el comprobante: el cliente puede subir otro.
              </p>
              <VistaDeComprobante comprobante={ultimo} />
            </section>
          )}

          <Acciones pedido={pedido} manejaPlata={manejaPlata} />

          <ReservasDelPedido pedido={pedido} />

          <section>
            <h2 className="text-2xl">Qué compró</h2>
            <ul className="mt-3 divide-y divide-borde rounded-xl border border-borde">
              {pedido.lineas.map((l, i) => (
                <li key={i} className="px-4 py-3">
                  <div className="flex justify-between gap-4">
                    <span className="text-principal">
                      <span className="numeros">{l.cantidad}</span> × {l.descripcion}
                      {l.reserva && (
                        <span className="ml-2 rounded-full border border-aviso/60 px-2 py-0.5 text-xs text-aviso">Sin stock al pedir</span>
                      )}
                    </span>
                    <span className="numeros shrink-0 text-principal">{formatearPrecio(l.subtotal)}</span>
                  </div>
                  {l.detalle.length > 0 && (
                    <ul className="mt-1 space-y-0.5 text-sm text-suave">
                      {l.detalle.map((d) => (
                        <li key={d}>{d}</li>
                      ))}
                    </ul>
                  )}
                </li>
              ))}
              <li className="flex justify-between bg-superficie px-4 py-3 font-semibold">
                <span>Total</span>
                <span className="numeros">{formatearPrecio(pedido.total)}</span>
              </li>
            </ul>
          </section>

          {pedido.comprobantes.length > 0 && (
            <section>
              <h2 className="text-2xl">Comprobantes</h2>
              <ul className="mt-3 divide-y divide-borde rounded-xl border border-borde">
                {pedido.comprobantes.map((c) => (
                  <li key={c.id} className="flex flex-wrap items-center justify-between gap-2 px-4 py-3 text-sm">
                    <span>
                      <a href={c.url} target="_blank" rel="noreferrer" className="text-oro-claro underline underline-offset-4">
                        {c.nombre}
                      </a>
                      <span className="numeros ml-2 text-suave">{formatearFecha(c.subidoEn)}</span>
                    </span>
                    {c.rechazado && <span className="text-peligro">Rechazado: {c.motivoRechazo}</span>}
                  </li>
                ))}
              </ul>
            </section>
          )}
        </div>

        <aside className="space-y-4 rounded-xl border border-borde bg-superficie p-5 text-sm">
          <div>
            <p className="text-xs tracking-wide text-suave uppercase">Cliente</p>
            <p className="text-base font-semibold">{pedido.cliente}</p>
            <p className="numeros text-suave">{pedido.telefono}</p>
            <a
              href={linkWhatsAppDelCliente(pedido.telefono, `Hola ${pedido.cliente.split(" ")[0]}, te escribimos de R.A.M. por tu pedido N° ${pedido.numero}.`)}
              target="_blank"
              rel="noreferrer"
              className="mt-2 inline-flex items-center gap-2 rounded-full border border-borde px-3 py-1.5 hover:border-oro"
            >
              <IconoWhatsApp className="size-4 text-exito" /> Escribirle
            </a>
          </div>
          <div className="border-t border-borde pt-4">
            <p className="text-xs tracking-wide text-suave uppercase">Entrega</p>
            <p className="font-semibold">{ENTREGAS[pedido.entrega]}</p>
            {pedido.direccion && <p>{pedido.direccion}</p>}
            {pedido.entrega === "ENVIO_OTRA_LOCALIDAD" && (
              <p className="mt-1 text-aviso">Pasale el costo del envío por WhatsApp: no está en el total.</p>
            )}
          </div>
          {pedido.nota && (
            <div className="border-t border-borde pt-4">
              <p className="text-xs tracking-wide text-suave uppercase">Nota del cliente</p>
              <p className="whitespace-pre-line">{pedido.nota}</p>
            </div>
          )}
          {sesion?.rol === "DUENO" && (
            <div className="border-t border-borde pt-4">
              <BotonBorrar
                texto="Borrar pedido"
                titulo={`¿Borrar el pedido ${pedido.numero}?`}
                confirmar="Sí, borrar"
                alBorrar={() => borrar.mutateAsync()}
                alTerminar={() => navegar("/pedidos", { replace: true })}
              >
                Se borra con sus comprobantes. {pedido.estado === "PAGADO" && "Como está pagado, lo descontado vuelve al stock. "}
                Es para limpiar pruebas: un pedido real que no sigue, mejor cancelalo.
              </BotonBorrar>
            </div>
          )}
          <dl className="numeros space-y-1 border-t border-borde pt-4 text-suave">
            {pedido.pagadoEn && <div>Pagado: {formatearFecha(pedido.pagadoEn)}</div>}
            {pedido.entregadoEn && <div>Entregado: {formatearFecha(pedido.entregadoEn)}</div>}
            {pedido.canceladoEn && (
              <div>
                Cancelado: {formatearFecha(pedido.canceladoEn)} — {pedido.motivoCancelacion}
              </div>
            )}
          </dl>
        </aside>
      </div>
    </>
  );
}

/**
 * Las imágenes se ven acá mismo; los PDF se abren en otra pestaña (el visor del navegador).
 * El archivo viene de nuestro backend con la sesión del sistema: el cliente no tiene esa URL.
 */
function VistaDeComprobante({ comprobante }: { comprobante: Comprobante }) {
  const esImagen = comprobante.tipoDeContenido.startsWith("image/");
  return (
    <div className="mt-4">
      {esImagen ? (
        <a href={comprobante.url} target="_blank" rel="noreferrer" title="Abrir en grande">
          <img
            src={comprobante.url}
            alt={`Comprobante ${comprobante.nombre}`}
            className="max-h-[28rem] w-auto rounded-lg border border-borde bg-fondo object-contain"
          />
        </a>
      ) : (
        <a
          href={comprobante.url}
          target="_blank"
          rel="noreferrer"
          className="inline-flex rounded-full border border-borde bg-fondo px-4 py-2 font-semibold hover:border-oro"
        >
          Abrir el comprobante (PDF)
        </a>
      )}
    </div>
  );
}

type ConMotivo = Extract<AccionDePedido, "rechazar-comprobante" | "cancelar">;

function Acciones({ pedido, manejaPlata }: { pedido: DetalleDePedido; manejaPlata: boolean }) {
  const accion = useAccionSobrePedido(pedido.id);
  const avisar = useAvisar();
  const [pidiendoMotivo, setPidiendoMotivo] = useState<ConMotivo | null>(null);
  const [motivo, setMotivo] = useState("");

  function hacer(tipo: AccionDePedido, mensajeDeExito: string, conMotivo?: string) {
    accion.mutate(
      { accion: tipo, motivo: conMotivo },
      {
        onSuccess: () => {
          avisar(mensajeDeExito);
          setPidiendoMotivo(null);
          setMotivo("");
        },
        onError: (e) => avisar(e.message, "error"),
      },
    );
  }

  function enviarMotivo(evento: FormEvent) {
    evento.preventDefault();
    if (!pidiendoMotivo || !motivo.trim()) return;
    if (pidiendoMotivo === "cancelar") hacer("cancelar", "Pedido cancelado", motivo);
    else hacer("rechazar-comprobante", "Comprobante rechazado: el cliente puede subir otro", motivo);
  }

  const { estado } = pedido;
  const botones: { texto: string; principal?: boolean; peligro?: boolean; alTocar: () => void }[] = [];
  if (manejaPlata && (estado === "PAGO_INFORMADO" || estado === "ESPERANDO_PAGO")) {
    botones.push({
      texto: estado === "PAGO_INFORMADO" ? "Confirmar pago" : "Marcar como pagado",
      principal: true,
      alTocar: () => hacer("confirmar-pago", "Pago confirmado"),
    });
  }
  if (manejaPlata && estado === "PAGO_INFORMADO") {
    botones.push({ texto: "Rechazar comprobante", alTocar: () => setPidiendoMotivo("rechazar-comprobante") });
  }
  if (estado === "PAGADO") {
    botones.push({ texto: "Marcar como entregado", principal: true, alTocar: () => hacer("entregar", "Pedido entregado") });
  }
  if (manejaPlata && (estado === "ESPERANDO_PAGO" || estado === "PAGO_INFORMADO" || estado === "PAGADO")) {
    botones.push({ texto: "Cancelar pedido", peligro: true, alTocar: () => setPidiendoMotivo("cancelar") });
  }

  if (botones.length === 0) {
    if (!manejaPlata && (estado === "ESPERANDO_PAGO" || estado === "PAGO_INFORMADO")) {
      return <p className="text-sm text-suave">Los pagos los confirman el Dueño y Mostrador.</p>;
    }
    return null;
  }

  return (
    <>
      <div className="flex flex-wrap gap-3">
        {botones.map((b) => (
          <button
            key={b.texto}
            type="button"
            disabled={accion.isPending}
            onClick={b.alTocar}
            className={`rounded-full px-5 py-2.5 text-sm font-semibold disabled:opacity-50 ${
              b.principal
                ? "boton-oro hover:brightness-110"
                : b.peligro
                  ? "border border-peligro/60 text-peligro hover:bg-peligro/10"
                  : "border border-borde hover:border-oro"
            }`}
          >
            {b.texto}
          </button>
        ))}
      </div>
      {estado === "ESPERANDO_PAGO" && manejaPlata && (
        <p className="text-xs text-suave">“Marcar como pagado”: si pagó en efectivo o transfirió sin subir el comprobante.</p>
      )}

      <Dialogo
        abierto={pidiendoMotivo !== null}
        alCerrar={() => setPidiendoMotivo(null)}
        titulo={pidiendoMotivo === "cancelar" ? `Cancelar el pedido ${pedido.numero}` : "Rechazar el comprobante"}
        ancho="max-w-lg"
      >
        <form onSubmit={enviarMotivo} className="space-y-4">
          <label htmlFor="motivo" className="text-sm text-suave">
            {pidiendoMotivo === "cancelar" ? "¿Por qué se cancela?" : "¿Por qué lo rechazás? El cliente lo va a ver."}
          </label>
          <textarea
            id="motivo"
            required
            maxLength={300}
            rows={3}
            value={motivo}
            onChange={(e) => setMotivo(e.target.value)}
            placeholder={pidiendoMotivo === "cancelar" ? "Sin stock, el cliente se arrepintió…" : "No llegó la transferencia, el monto no coincide…"}
            className={ESTILO_DE_CAMPO}
          />
          {pidiendoMotivo === "cancelar" && pedido.estado === "PAGADO" && (
            <p className="text-sm text-aviso">
              Ya estaba pagado: lo descontado vuelve al stock y se cancelan sus reservas. La devolución de la plata la hacés aparte.
            </p>
          )}
          <div className="flex justify-end gap-3">
            <button type="button" onClick={() => setPidiendoMotivo(null)} className="px-4 py-2 text-sm text-suave hover:text-principal">
              Volver
            </button>
            <button
              type="submit"
              disabled={accion.isPending || !motivo.trim()}
              className="rounded-full border border-peligro/60 px-5 py-2 text-sm font-semibold text-peligro hover:bg-peligro/10 disabled:opacity-50"
            >
              {pidiendoMotivo === "cancelar" ? "Cancelar pedido" : "Rechazar"}
            </button>
          </div>
        </form>
      </Dialogo>
    </>
  );
}

/**
 * Lo que faltaba al confirmar el pago (ADR-014): quedó reservado hasta que llegue la mercadería.
 * Solo se muestra si el pedido tiene reservas.
 */
function ReservasDelPedido({ pedido }: { pedido: DetalleDePedido }) {
  const { data: reservas } = useReservasDePedido(pedido.id);
  const cambiar = useCambiarStock();
  const avisar = useAvisar();
  const { data: sesion } = useSesion();
  const manejaStock = sesion?.rol === "DUENO" || sesion?.rol === "MOSTRADOR";
  const pendientes = reservas?.filter((r) => r.estado === "PENDIENTE") ?? [];

  if (!reservas || reservas.length === 0) {
    if (pedido.estado === "PAGADO" || !pedido.lineas.some((l) => l.reserva)) return null;
    return (
      <p className="rounded-xl border border-aviso/50 bg-aviso/5 p-4 text-sm">
        Al hacer el pedido faltaba stock de algo. Al confirmar el pago se descuenta lo que haya y lo demás queda reservado.
      </p>
    );
  }

  return (
    <section className={`rounded-xl border p-5 ${pendientes.length > 0 ? "border-aviso/50 bg-aviso/5" : "border-borde"}`}>
      <h2 className="text-2xl">{pendientes.length > 0 ? "Falta mercadería" : "Reservas"}</h2>
      {pendientes.length > 0 && (
        <p className="mt-1 text-sm text-suave">Avisale al cliente la demora. Cuando llegue, separala acá o en Stock.</p>
      )}
      <ul className="mt-3 space-y-2 text-sm">
        {reservas.map((r) => (
          <li key={r.id} className="flex flex-wrap items-center justify-between gap-2">
            <span>
              <span className="numeros">{r.cantidad}</span> × {r.productoNombre}
            </span>
            {r.estado === "PENDIENTE" ? (
              manejaStock && r.sePuedeCubrir ? (
                <button
                  type="button"
                  disabled={cambiar.isPending}
                  onClick={() =>
                    cambiar.mutate({ tipo: "cubrir", reserva: r.id }, { onSuccess: () => avisar("Separado"), onError: (e) => avisar(e.message, "error") })
                  }
                  className="boton-oro rounded-full px-4 py-1 text-xs font-semibold disabled:opacity-50"
                >
                  Separar
                </button>
              ) : (
                <span className="text-aviso">{r.sePuedeCubrir ? "Ya llegó" : "Esperando mercadería"}</span>
              )
            ) : (
              <span className={r.estado === "CUBIERTA" ? "text-exito" : "text-suave"}>{r.estado === "CUBIERTA" ? "✓ Separada" : "Cancelada"}</span>
            )}
          </li>
        ))}
      </ul>
    </section>
  );
}
