import { useState, type ReactNode } from "react";
import { Link, useLocation, useParams } from "react-router";
import { useOrden, useTaller, type DetalleDeOrden, type Estado } from "../../api/reparaciones";
import { EtiquetaDeEstado } from "../../componentes/EtiquetaDeEstado";
import { IconoWhatsApp } from "../../componentes/IconoWhatsApp";
import { formatearFecha, formatearPrecio } from "../../utilidades/formato";
import { mensajeDeIngreso, mensajeDePresupuesto, mensajeDeRetiro, mensajeDeSeguimiento } from "../../utilidades/mensajes";
import { ACCION_HACIA, ESTADOS, formatearDia, linkDeSeguimiento, linkWhatsApp } from "../../utilidades/reparaciones";
import { DialogoDeAccion, type Accion } from "./DialogoDeAccion";

/** /reparaciones/:id — todo sobre una orden, y los botones para el próximo paso. */
export function Detalle() {
  const { id = "" } = useParams();
  const { data: orden, isPending, isError, error } = useOrden(id);
  const ubicacion = useLocation();
  const recienIngresada = (ubicacion.state as { recienIngresada?: boolean } | null)?.recienIngresada === true;
  const [accion, setAccion] = useState<Accion | null>(null);
  const { data: taller } = useTaller();

  if (isPending) return <div aria-hidden className="h-96 animate-pulse rounded-xl bg-superficie" />;
  if (isError) {
    return (
      <p role="alert" className="rounded-xl border border-peligro/60 bg-peligro/10 p-4">
        No se pudo cargar la orden: {error.message}
      </p>
    );
  }

  const telefono = orden.cliente?.telefono;

  return (
    <>
      <Link to="/reparaciones" className="text-sm text-suave hover:text-oro-claro">
        ← Reparaciones
      </Link>

      <div className="mt-2 flex flex-wrap items-center gap-x-4 gap-y-2">
        <h1 className="texto-metalico text-4xl sm:text-5xl">
          Orden <span className="numeros">{orden.numero}</span>
        </h1>
        <EtiquetaDeEstado estado={orden.estado} enGarantia={orden.enGarantia} />
      </div>
      <p className="mt-1 text-suave">
        {orden.equipo} · ingresó el {formatearFecha(orden.ingresadoEn)}
      </p>

      {recienIngresada && (
        <div role="status" className="mt-6 flex flex-wrap items-center justify-between gap-4 rounded-xl border border-exito/60 bg-exito/10 px-5 py-4">
          <p className="text-principal">✓ Ingreso registrado. Dale al cliente su comprobante:</p>
          <div className="flex flex-wrap gap-2">
            <Link to={`/reparaciones/${orden.id}/comprobante`} className="rounded-full border border-borde bg-fondo px-4 py-2 text-sm font-semibold hover:border-oro">
              Imprimir comprobante
            </Link>
            {telefono && (
              <BotonWhatsApp telefono={telefono} mensaje={mensajeDeIngreso(orden, taller?.nombre ?? "el taller")} texto="Mandar por WhatsApp" />
            )}
          </div>
        </div>
      )}

      <div className="mt-8 grid items-start gap-6 lg:grid-cols-[1fr_20rem]">
        <div className="min-w-0 space-y-6">
          <ProximoPaso orden={orden} alElegir={setAccion} />

          <Tarjeta titulo="Qué le pasa">
            <p className="whitespace-pre-line text-principal">{orden.problema}</p>
            {orden.observaciones && (
              <div className="mt-4 rounded-lg border border-dashed border-borde px-3 py-2">
                <p className="text-xs tracking-wide text-suave uppercase">Observaciones internas</p>
                <p className="mt-1 whitespace-pre-line text-sm text-principal">{orden.observaciones}</p>
              </div>
            )}
          </Tarjeta>

          {orden.presupuestoMonto !== null && (
            <Tarjeta titulo="Presupuesto">
              <p className="numeros font-display text-3xl font-bold">{formatearPrecio(orden.presupuestoMonto)}</p>
              <p className="mt-1 whitespace-pre-line text-principal">{orden.presupuestoDescripcion}</p>
              {orden.estado === "PRESUPUESTADO" && telefono && (
                <div className="mt-4 flex flex-wrap gap-2">
                  <BotonWhatsApp telefono={telefono} mensaje={mensajeDePresupuesto(orden)} texto="Mandar presupuesto" />
                  <button type="button" onClick={() => setAccion("PRESUPUESTADO")} className="rounded-full border border-borde px-4 py-2 text-sm hover:border-oro">
                    Corregir presupuesto
                  </button>
                </div>
              )}
            </Tarjeta>
          )}

          <Bitacora orden={orden} alAnotar={() => setAccion("NOTA")} />
        </div>

        <aside className="space-y-6">
          <Tarjeta titulo="Cliente">
            {orden.cliente ? (
              <>
                <Link to={`/clientes/${orden.cliente.id}`} className="font-semibold text-principal underline-offset-4 hover:text-oro-claro hover:underline">
                  {orden.cliente.nombreCompleto}
                </Link>
                <p className="numeros text-sm text-suave">{orden.cliente.telefono}</p>
                {orden.cliente.correo && <p className="text-sm text-suave">{orden.cliente.correo}</p>}
                {telefono && (
                  <a href={linkWhatsApp(telefono, "")} className="mt-3 inline-flex items-center gap-2 text-sm text-oro underline underline-offset-2">
                    <IconoWhatsApp className="size-4" />
                    Escribirle
                  </a>
                )}
              </>
            ) : (
              <p className="text-suave">Sin datos del cliente.</p>
            )}
          </Tarjeta>

          <Tarjeta titulo="Equipo">
            <dl className="space-y-2 text-sm">
              <Dato nombre="Equipo">{orden.equipo}</Dato>
              {orden.procesador && <Dato nombre="Procesador">{orden.procesador}</Dato>}
              <Dato nombre="Cargador">{orden.conCargador ? "Sí, lo dejó" : "No"}</Dato>
              <Dato nombre="Password">
                <Password orden={orden} />
              </Dato>
            </dl>
          </Tarjeta>

          <Tarjeta titulo="Seguimiento del cliente">
            <p className="text-sm text-suave">
              Con el N° <strong className="numeros text-principal">{orden.numero}</strong> y los últimos 4 números de su teléfono.
            </p>
            <a href={linkDeSeguimiento(orden.numero)} target="_blank" rel="noreferrer" className="mt-2 block truncate text-sm text-oro underline underline-offset-2">
              {linkDeSeguimiento(orden.numero)}
            </a>
            <div className="mt-3 flex flex-wrap gap-2">
              {telefono && <BotonWhatsApp telefono={telefono} mensaje={mensajeDeSeguimiento(orden)} texto="Mandar link" chico />}
              <Link to={`/reparaciones/${orden.id}/comprobante`} className="rounded-full border border-borde px-3 py-1.5 text-xs hover:border-oro">
                Comprobante
              </Link>
            </div>
          </Tarjeta>
        </aside>
      </div>

      <DialogoDeAccion orden={orden} accion={accion} alCerrar={() => setAccion(null)} />
    </>
  );
}

/** Los botones del próximo paso: solo los que el recorrido permite desde el estado actual. */
function ProximoPaso({ orden, alElegir }: { orden: DetalleDeOrden; alElegir: (a: Accion) => void }) {
  const telefono = orden.cliente?.telefono;

  if (orden.estado === "ENTREGADO") {
    return (
      <Tarjeta titulo="Entregada">
        <p className="text-principal">
          {/* formatearFecha termina en "p. m.": el punto final ya está, no se agrega otro. */}
          Se entregó el {orden.entregadoEn && formatearFecha(orden.entregadoEn).replace(/\.$/, "")}.{" "}
          {orden.garantiaHasta ? (
            orden.enGarantia ? (
              <>
                <strong className="text-exito">En garantía</strong> hasta el {formatearDia(orden.garantiaHasta)} ({orden.garantiaDias} días).
              </>
            ) : (
              <>La garantía venció el {formatearDia(orden.garantiaHasta)}.</>
            )
          ) : (
            "Sin garantía."
          )}
        </p>
      </Tarjeta>
    );
  }

  // Orden de los botones: primero el "camino feliz", después los desvíos.
  const PRIORIDAD: Estado[] = [
    "EN_DIAGNOSTICO", "PRESUPUESTADO", "APROBADO", "EN_REPARACION", "LISTO_PARA_RETIRAR", "ENTREGADO",
    "ESPERANDO_REPUESTO", "EN_PROVEEDOR", "RECHAZADO", "SIN_REPARACION",
  ];
  const opciones = PRIORIDAD.filter((e) => orden.siguientes.includes(e));

  return (
    <Tarjeta titulo="Próximo paso">
      <div className="flex flex-wrap gap-2">
        {opciones.map((estado, i) => (
          <button
            key={estado}
            type="button"
            onClick={() => alElegir(estado)}
            className={
              i === 0
                ? "boton-oro rounded-full px-5 py-2.5 font-semibold hover:brightness-110"
                : "rounded-full border border-borde px-4 py-2.5 text-principal hover:border-oro hover:text-oro-claro"
            }
          >
            {ACCION_HACIA[estado]}
          </button>
        ))}
      </div>
      {orden.estado === "LISTO_PARA_RETIRAR" && telefono && (
        <div className="mt-4 border-t border-borde pt-4">
          <BotonWhatsApp telefono={telefono} mensaje={mensajeDeRetiro(orden)} texto="Avisar que está listo" />
        </div>
      )}
    </Tarjeta>
  );
}

function Bitacora({ orden, alAnotar }: { orden: DetalleDeOrden; alAnotar: () => void }) {
  // La más nueva arriba: lo último que pasó es lo primero que se lee.
  const movimientos = [...orden.movimientos].reverse();
  return (
    <section aria-labelledby="titulo-bitacora" className="rounded-xl border border-borde bg-superficie p-5">
      <div className="flex items-center justify-between gap-4">
        <h2 id="titulo-bitacora" className="text-xl text-oro-claro">
          Bitácora
        </h2>
        <button type="button" onClick={alAnotar} className="rounded-full border border-borde px-3 py-1.5 text-sm hover:border-oro">
          + Nota
        </button>
      </div>
      <ol className="mt-4 space-y-4 border-l border-borde pl-5">
        {movimientos.map((m, i) => (
          <li key={i} className="relative">
            <span aria-hidden className={`absolute top-1.5 -left-[1.6rem] size-2.5 rounded-full ${m.estado ? "bg-oro" : "bg-borde"}`} />
            <p className="text-sm text-principal">
              {m.estado ? <strong>{ESTADOS[m.estado].nombre}</strong> : <span className="text-suave">Nota</span>}
              {!m.paraCliente && <span className="ml-2 rounded-full border border-borde px-1.5 text-[0.65rem] text-suave">Interna</span>}
            </p>
            {m.nota && <p className="mt-0.5 whitespace-pre-line text-sm text-principal">{m.nota}</p>}
            <p className="mt-0.5 text-xs text-suave">
              {formatearFecha(m.cuando)} · {m.usuario}
            </p>
          </li>
        ))}
      </ol>
    </section>
  );
}

/** El password se ve recién al tocar "Mostrar": así no queda a la vista de quien pasa por el mostrador. */
function Password({ orden }: { orden: DetalleDeOrden }) {
  const [visible, setVisible] = useState(false);
  if (!orden.passwordVisible) return <span className="text-suave">Solo lo ven el dueño y los técnicos</span>;
  if (!orden.password) return <span className="text-suave">No dejó</span>;
  return (
    <span className="inline-flex items-center gap-2">
      <span className="font-mono">{visible ? orden.password : "••••••"}</span>
      <button type="button" onClick={() => setVisible(!visible)} className="text-xs text-oro underline underline-offset-2">
        {visible ? "Ocultar" : "Mostrar"}
      </button>
    </span>
  );
}

function BotonWhatsApp({ telefono, mensaje, texto, chico = false }: { telefono: string; mensaje: string; texto: string; chico?: boolean }) {
  return (
    <a
      href={linkWhatsApp(telefono, mensaje)}
      target="_blank"
      rel="noreferrer"
      className={`inline-flex items-center gap-2 rounded-full bg-[#25d366] font-semibold text-negro hover:brightness-110 ${chico ? "px-3 py-1.5 text-xs" : "px-4 py-2 text-sm"}`}
    >
      <IconoWhatsApp className={chico ? "size-3.5" : "size-4"} />
      {texto}
    </a>
  );
}

function Tarjeta({ titulo, children }: { titulo: string; children: ReactNode }) {
  return (
    <section className="rounded-xl border border-borde bg-superficie p-5">
      <h2 className="text-xl text-oro-claro">{titulo}</h2>
      <div className="mt-3">{children}</div>
    </section>
  );
}

function Dato({ nombre, children }: { nombre: string; children: ReactNode }) {
  return (
    <div className="flex justify-between gap-4">
      <dt className="text-suave">{nombre}</dt>
      <dd className="text-right text-principal">{children}</dd>
    </div>
  );
}
