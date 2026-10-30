import Link from "next/link";
import type { ComponentType } from "react";
import { BotonAgregarAlCarrito } from "@/components/BotonAgregarAlCarrito";
import { EstadoDeStock } from "@/components/EstadoDeStock";
import {
  IconoCheck,
  IconoChip,
  IconoDisco,
  IconoFuente,
  IconoMemoria,
  IconoMother,
  IconoPc,
  IconoPlacaDeVideo,
  IconoWhatsApp,
} from "@/components/Iconos";
import type { Componente, Disponibilidad, ModeloDePc, TipoComponente } from "@/lib/api";
import {
  armadoDesdeModelo,
  consumoEstimado,
  elegir,
  elegirSinPlacaDeVideo,
  estaSalteado,
  nombreDelPaso,
  opcionesCompatibles,
  PASOS,
  pasoSiguiente,
  piezaQueFalta,
  potenciaRecomendada,
  puedeIrSinPlacaDeVideo,
  revisarArmado,
  saltear,
  urlDelArmador,
  type Armado,
  type Paso,
} from "@/lib/arma-tu-pc";
import { formatearPrecio } from "@/lib/formato";
import { urlCompleta } from "@/lib/sitio";
import { linkWhatsApp } from "@/lib/whatsapp";

/** Las partes de la pantalla de Armá tu PC. Todas son de servidor: llegan como HTML, sin JavaScript. */

const iconos: Record<TipoComponente, ComponentType<{ className?: string }>> = {
  cpu: IconoChip,
  mother: IconoMother,
  ram: IconoMemoria,
  gpu: IconoPlacaDeVideo,
  almacenamiento: IconoDisco,
  fuente: IconoFuente,
  gabinete: IconoPc,
};

const SIN_PLACA_TEXTO = "Sin placa de video (gráficos del procesador)";

function precioOConsultar(precio: number | null): string {
  return precio === null ? "A consultar" : formatearPrecio(precio);
}

const SALTEADO_TEXTO = "No incluida (salteaste este paso)";

/** Lo decidido para un paso, en texto: el nombre, "Sin placa de video", "No incluida" o null si falta decidir. */
function eleccionDe(tipo: TipoComponente, armado: Armado): string | null {
  if (tipo === "gpu" && armado.sinPlacaDeVideo) return SIN_PLACA_TEXTO;
  if (estaSalteado(armado, tipo)) return SALTEADO_TEXTO;
  return armado.piezas[tipo]?.nombre ?? null;
}

// ─── Inicio ───────────────────────────────────────────────────────────────────────────────

export function Inicio({ modelos, componentes }: { modelos: ModeloDePc[]; componentes: Componente[] }) {
  return (
    <section aria-labelledby="como-empezar" className="mt-12">
      <h2 id="como-empezar" className="text-3xl text-principal">
        ¿Cómo querés empezar?
      </h2>
      <p className="mt-2 text-suave">Partí de uno de nuestros modelos y cambiale lo que quieras, o elegí cada pieza desde cero.</p>

      <div className="mt-8 grid gap-4 sm:grid-cols-2 sm:gap-6 lg:grid-cols-4">
        {modelos.map((modelo) => {
          const armado = armadoDesdeModelo(modelo, componentes);
          const { total } = revisarArmado(armado);
          const destacadas: TipoComponente[] = ["cpu", "gpu", "ram"];
          return (
            <article key={modelo.slug} className="flex flex-col rounded-xl border border-borde bg-superficie p-6">
              <h3 className="text-2xl text-oro-claro">{modelo.nombre}</h3>
              <p className="mt-2 text-sm text-suave">{modelo.descripcion}</p>
              <ul className="mt-4 space-y-1.5 text-sm text-principal">
                {destacadas.map((tipo) => {
                  const Icono = iconos[tipo];
                  return (
                    <li key={tipo} className="flex items-start gap-2">
                      <Icono className="mt-0.5 size-4 shrink-0 text-oro" />
                      {eleccionDe(tipo, armado)}
                    </li>
                  );
                })}
              </ul>
              <p className="mt-auto pt-5 font-display text-2xl font-bold text-principal">{precioOConsultar(total)}</p>
              <Link
                href={urlDelArmador(armado, "resumen")}
                className="mt-3 rounded-full border border-oro px-4 py-2 text-center text-sm font-semibold text-oro hover:bg-oro hover:text-fondo"
              >
                Partir de este modelo
              </Link>
            </article>
          );
        })}

        <article className="flex flex-col rounded-xl border border-dashed border-oro/60 p-6">
          <h3 className="text-2xl text-oro-claro">Desde cero</h3>
          <p className="mt-2 text-sm text-suave">
            Elegí vos cada pieza, en {PASOS.length} pasos. Te vamos mostrando solo lo que es compatible.
          </p>
          <Link
            href={urlDelArmador({ piezas: {}, sinPlacaDeVideo: false, conArmado: true }, "cpu")}
            className="boton-oro mt-auto rounded-full px-4 py-2 text-center text-sm font-semibold hover:brightness-110"
          >
            Empezar de cero
          </Link>
        </article>
      </div>
    </section>
  );
}

// ─── Progreso ─────────────────────────────────────────────────────────────────────────────

export function Progreso({ armado, pasoActual }: { armado: Armado; pasoActual: Paso }) {
  const indice = PASOS.findIndex((p) => p.tipo === pasoActual);
  const textoPaso = pasoActual === "resumen" ? "Resumen" : `Paso ${indice + 1} de ${PASOS.length} · ${nombreDelPaso(pasoActual)}`;
  const elegidos = PASOS.filter((p) => eleccionDe(p.tipo, armado)).length;

  return (
    <nav aria-label="Pasos para armar tu PC">
      {/* En el celular: texto y barra. Los 8 pasos no entran a lo ancho. */}
      <div className="sm:hidden">
        <p className="text-sm text-principal">{textoPaso}</p>
        <div className="mt-2 h-1.5 overflow-hidden rounded-full bg-superficie">
          <div className="h-full rounded-full bg-oro" style={{ width: `${(elegidos / PASOS.length) * 100}%` }} />
        </div>
      </div>

      <ol className="hidden flex-wrap gap-2 sm:flex">
        {[...PASOS.map((p) => ({ paso: p.tipo as Paso, nombre: p.nombre })), { paso: "resumen" as Paso, nombre: "Resumen" }].map(
          ({ paso, nombre }, i) => {
            const actual = paso === pasoActual;
            const hecho = paso !== "resumen" && !!eleccionDe(paso, armado);
            return (
              <li key={paso}>
                <Link
                  href={urlDelArmador(armado, paso)}
                  aria-current={actual ? "step" : undefined}
                  className={`inline-flex items-center gap-2 rounded-full border px-3 py-1.5 text-sm ${
                    actual
                      ? "border-oro bg-oro/10 text-oro-claro"
                      : "border-borde text-suave hover:border-oro hover:text-oro-claro"
                  }`}
                >
                  <span
                    className={`grid size-5 place-items-center rounded-full text-[0.7rem] font-semibold ${
                      hecho ? "bg-oro text-negro" : "border border-current"
                    }`}
                  >
                    {hecho ? <IconoCheck className="size-3.5" /> : paso === "resumen" ? "✓" : i + 1}
                  </span>
                  {nombre}
                </Link>
              </li>
            );
          },
        )}
      </ol>
    </nav>
  );
}

// ─── Un paso ──────────────────────────────────────────────────────────────────────────────

export function PasoDeEleccion({ tipo, armado, componentes }: { tipo: TipoComponente; armado: Armado; componentes: Componente[] }) {
  const paso = PASOS.find((p) => p.tipo === tipo)!;
  const indice = PASOS.indexOf(paso);
  const anterior = PASOS[indice - 1];
  const falta = piezaQueFalta(tipo, armado);
  const opciones = opcionesCompatibles(tipo, armado, componentes);
  const ocultas = componentes.filter((c) => c.tipo === tipo).length - opciones.length;
  const { total } = revisarArmado(armado);

  return (
    <section aria-labelledby="titulo-paso" className="mt-8">
      <h2 id="titulo-paso" className="text-3xl text-principal sm:text-4xl">
        {paso.titulo}
      </h2>
      <p className="mt-2 max-w-prose text-suave">{paso.ayuda}</p>
      <Contexto tipo={tipo} armado={armado} />

      {/* En el celular, el total va arriba de las opciones (en compu está en el costado). */}
      <p className="mt-4 text-sm text-principal lg:hidden">
        Llevás <strong className="font-semibold">{precioOConsultar(total)}</strong> ·{" "}
        <Link href={urlDelArmador(armado, "resumen")} className="text-oro underline underline-offset-4">
          Ver resumen
        </Link>
      </p>

      {/* Cualquier paso se puede saltear: ya tenés esa pieza, o no la querés (ADR-013). */}
      <Link
        href={urlDelArmador(saltear(armado, tipo), pasoSiguiente(saltear(armado, tipo)))}
        className={`mt-5 inline-flex items-center gap-2 rounded-full border px-4 py-2 text-sm ${
          estaSalteado(armado, tipo) ? "border-oro bg-oro/10 text-oro-claro" : "border-borde text-principal hover:border-oro"
        }`}
        aria-current={estaSalteado(armado, tipo) ? "true" : undefined}
      >
        {estaSalteado(armado, tipo) ? "✓ Salteaste este paso" : `Saltear: no quiero ${paso.nombre.toLowerCase()}`}
      </Link>

      {falta ? (
        <div className="mt-6 rounded-xl border border-borde bg-superficie p-6">
          <p className="text-principal">
            Para mostrarte {paso.nombre.toLowerCase()} compatible, primero decidí {nombreDelPaso(falta).toLowerCase()}.
          </p>
          <div className="mt-3 flex flex-wrap gap-x-5 gap-y-2">
            <Link href={urlDelArmador(armado, falta)} className="text-oro underline underline-offset-4 hover:text-oro-claro">
              Elegir {nombreDelPaso(falta).toLowerCase()}
            </Link>
            <Link
              href={urlDelArmador(saltear(armado, falta), tipo)}
              className="text-suave underline underline-offset-4 hover:text-oro-claro"
            >
              No voy a llevar {nombreDelPaso(falta).toLowerCase()}: mostrame todas
            </Link>
          </div>
        </div>
      ) : (
        <ul className="mt-6 grid gap-4 sm:grid-cols-2">
          {tipo === "gpu" && puedeIrSinPlacaDeVideo(armado) && (
            <Opcion
              href={urlDelArmador(elegirSinPlacaDeVideo(armado), pasoSiguiente(elegirSinPlacaDeVideo(armado)))}
              elegida={armado.sinPlacaDeVideo}
              tipo={tipo}
              nombre="Sin placa de video"
              detalles={["Usa los gráficos del procesador", "Para oficina y estudio"]}
              precio={0}
            />
          )}
          {opciones.map((c) => {
            const nuevo = elegir(armado, c);
            return (
              <Opcion
                key={c.id}
                href={urlDelArmador(nuevo, pasoSiguiente(nuevo))}
                elegida={armado.piezas[tipo]?.id === c.id}
                tipo={tipo}
                nombre={c.nombre}
                detalles={c.detalles}
                precio={c.precio}
                stock={c.stock}
              />
            );
          })}
        </ul>
      )}

      {!falta && ocultas > 0 && (
        <p className="mt-4 text-sm text-suave">
          {ocultas === 1 ? "Hay 1 opción que no es compatible" : `Hay ${ocultas} opciones que no son compatibles`} con lo que
          elegiste antes, por eso no {ocultas === 1 ? "se muestra" : "se muestran"}.
        </p>
      )}

      <div className="mt-8 flex flex-wrap justify-between gap-4 border-t border-borde pt-5 text-sm">
        {anterior ? (
          <Link href={urlDelArmador(armado, anterior.tipo)} className="text-suave hover:text-oro-claro">
            ← {anterior.nombre}
          </Link>
        ) : (
          <Link href="/arma-tu-pc" className="text-suave hover:text-oro-claro">
            ← Volver a los modelos
          </Link>
        )}
        <Link href={urlDelArmador(armado, "resumen")} className="text-oro hover:text-oro-claro">
          Ver resumen →
        </Link>
      </div>
    </section>
  );
}

/** Una línea que explica POR QUÉ se muestran esas opciones, con los datos de lo que ya eligió. */
function Contexto({ tipo, armado }: { tipo: TipoComponente; armado: Armado }) {
  const { cpu, mother } = armado.piezas;
  let texto: string | null = null;
  if (tipo === "mother" && cpu) texto = `Tu procesador usa socket ${cpu.socket}.`;
  if (tipo === "ram" && mother) texto = `Tu mother usa memorias ${mother.tipoRam}.`;
  if (tipo === "gabinete" && mother) texto = `Tu mother es formato ${mother.formato === "mATX" ? "Micro ATX" : "ATX"}.`;
  if (tipo === "fuente" && cpu) {
    texto = `Tu PC consume unos ${consumoEstimado(armado)} W: te mostramos fuentes de ${potenciaRecomendada(armado)} W o más.`;
  }
  if (tipo === "gpu" && puedeIrSinPlacaDeVideo(armado)) {
    texto = "Tu procesador tiene gráficos integrados: si no vas a jugar, podés no ponerle placa de video.";
  }
  if (!texto) return null;
  return <p className="mt-3 inline-block rounded-lg border border-oro/40 bg-oro/5 px-3 py-2 text-sm text-principal">{texto}</p>;
}

/**
 * Una opción: toda la tarjeta es un link a la URL con esa pieza elegida (y el paso siguiente).
 * aria-current marca la que ya estaba elegida.
 */
function Opcion(props: {
  href: string;
  elegida: boolean;
  tipo: TipoComponente;
  nombre: string;
  detalles: string[];
  precio: number | null;
  stock?: Disponibilidad | null;
}) {
  const Icono = iconos[props.tipo];
  return (
    <li>
      <Link
        href={props.href}
        aria-current={props.elegida ? "true" : undefined}
        className={`group flex h-full flex-col rounded-xl border p-5 transition-colors ${
          props.elegida
            ? "border-oro bg-oro/10 shadow-[0_0_24px_rgb(227_176_75/0.18)]"
            : "border-borde bg-superficie hover:border-oro/70"
        }`}
      >
        <div className="flex items-start justify-between gap-3">
          <Icono className="size-8 text-oro" />
          {props.elegida && (
            <span className="inline-flex items-center gap-1 rounded-full bg-oro px-2.5 py-0.5 text-xs font-semibold text-negro">
              <IconoCheck className="size-3.5" />
              Elegido
            </span>
          )}
        </div>
        <h3 className="mt-3 text-lg text-principal group-hover:text-oro-claro">{props.nombre}</h3>
        <ul className="mt-2 flex flex-wrap gap-1.5">
          {props.detalles.map((d) => (
            <li key={d} className="rounded-full border border-borde px-2.5 py-0.5 text-xs text-suave">
              {d}
            </li>
          ))}
        </ul>
        {props.stock && props.stock.estado !== "DISPONIBLE" && <EstadoDeStock stock={props.stock} className="mt-3 self-start" />}
        <p className="mt-auto pt-4 font-display text-xl font-bold text-principal">
          {props.precio === 0 ? "Sin costo" : precioOConsultar(props.precio)}
        </p>
      </Link>
    </li>
  );
}

// ─── Resumen ──────────────────────────────────────────────────────────────────────────────

export function Resumen({ armado, whatsapp, precioArmado }: { armado: Armado; whatsapp: string; precioArmado: number | null }) {
  const revision = revisarArmado(armado);
  // Si el taller no ofrece el armado, se compran los componentes sí o sí.
  const conArmado = armado.conArmado && precioArmado !== null;
  const enlace = urlDelArmador({ ...armado, conArmado }, "resumen");
  const lineas = PASOS.map((p) => `${p.nombre}: ${eleccionDe(p.tipo, armado) ?? "sin elegir"}`);
  const total = revision.total === null ? null : revision.total + (conArmado ? precioArmado! : 0);
  const mensaje = [
    "Hola, armé esta PC en la web y quería consultarles:",
    "",
    ...lineas.map((l) => `• ${l}`),
    "",
    conArmado ? `Con armado y prueba (${precioOConsultar(precioArmado)})` : "Solo los componentes, sin armado",
    `Total: ${precioOConsultar(total)}`,
    `Link: ${urlCompleta(enlace)}`,
  ].join("\n");
  const sePuedeComprar = revision.lista && total !== null;

  return (
    <section aria-labelledby="titulo-resumen" className="mt-8">
      <h2 id="titulo-resumen" className="text-3xl text-principal sm:text-4xl">
        Tu PC
      </h2>

      <ul className="mt-6 divide-y divide-borde rounded-xl border border-borde bg-superficie">
        {PASOS.map(({ tipo, nombre }) => {
          const Icono = iconos[tipo];
          const eleccion = eleccionDe(tipo, armado);
          const precio =
            (tipo === "gpu" && armado.sinPlacaDeVideo) || estaSalteado(armado, tipo) ? 0 : (armado.piezas[tipo]?.precio ?? null);
          return (
            <li key={tipo} className="flex items-center gap-4 px-4 py-4 sm:px-5">
              <Icono className="size-7 shrink-0 text-oro" />
              <div className="min-w-0 flex-1">
                <p className="text-xs tracking-wide text-suave uppercase">{nombre}</p>
                <p className={eleccion ? "text-principal" : "text-oro"}>{eleccion ?? "Falta elegir"}</p>
                {armado.piezas[tipo]?.stock?.estado === "SIN_STOCK" && !estaSalteado(armado, tipo) && (
                  <p className="text-xs text-aviso">Sin stock: se reserva y te avisamos cuando llegue</p>
                )}
              </div>
              {eleccion && (
                <p className="hidden shrink-0 text-principal sm:block">{precio === 0 ? "—" : precioOConsultar(precio)}</p>
              )}
              <Link
                href={urlDelArmador(armado, tipo)}
                className="shrink-0 text-sm text-oro underline underline-offset-4 hover:text-oro-claro"
                aria-label={`${eleccion ? "Cambiar" : "Elegir"} ${nombre.toLowerCase()}`}
              >
                {eleccion ? "Cambiar" : "Elegir"}
              </Link>
            </li>
          );
        })}
      </ul>

      {armado.piezas.cpu && (
        <p className="mt-3 text-sm text-suave">Consumo estimado: {consumoEstimado(armado)} W.</p>
      )}

      {revision.avisos.length > 0 && (
        <ul role="status" className="mt-4 space-y-1 rounded-xl border border-oro/60 bg-oro/10 p-4 text-sm text-principal">
          {revision.avisos.map((a) => (
            <li key={a}>⚠ {a}</li>
          ))}
        </ul>
      )}

      {revision.problemas.length > 0 && (
        <ul role="alert" className="mt-4 space-y-1 rounded-xl border border-red-500/60 bg-red-500/10 p-4 text-sm text-principal">
          {revision.problemas.map((p) => (
            <li key={p}>{p}</li>
          ))}
        </ul>
      )}

      {precioArmado !== null && <EleccionDeArmado armado={armado} precioArmado={precioArmado} />}

      <div className="mt-8 flex flex-wrap items-end justify-between gap-6 border-t border-borde pt-6">
        <div>
          <p className="text-sm text-suave">Total</p>
          <p className="font-display text-4xl font-bold text-principal">{precioOConsultar(total)}</p>
          <p className="mt-1 text-sm text-suave">
            {conArmado ? `Componentes + armado y prueba (${precioOConsultar(precioArmado)}).` : "Solo los componentes, sin armar."}
          </p>
        </div>

        <div className="flex flex-col items-start gap-3">
          {sePuedeComprar ? (
            <BotonAgregarAlCarrito
              item={{
                // El id es el armado (con o sin armado): si agregás la misma PC dos veces, se suma la cantidad.
                id: `pc:${enlace}`,
                nombre: conArmado ? "PC armada a medida" : "Componentes para armar tu PC",
                precioUnitario: total!,
                imagen: null,
                enlace,
                detalle: conArmado ? [...lineas, `Armado y prueba: ${precioOConsultar(precioArmado)}`] : lineas,
              }}
            />
          ) : revision.faltan.length > 0 ? (
            <div>
              <p className="text-sm text-principal">
                Te falta elegir (o saltear): {revision.faltan.join(", ").toLowerCase()}.
              </p>
              <Link
                href={urlDelArmador(armado, pasoSiguiente(armado))}
                className="boton-oro mt-3 inline-block rounded-full px-6 py-3 font-semibold hover:brightness-110"
              >
                Seguir armando
              </Link>
            </div>
          ) : (
            revision.total === null && (
              <p className="max-w-xs text-sm text-principal">Algunas piezas todavía no tienen precio: consultanos el total por WhatsApp.</p>
            )
          )}
          <a
            href={linkWhatsApp(whatsapp, mensaje)}
            className="inline-flex items-center gap-2 text-sm font-semibold text-oro underline underline-offset-4 hover:text-oro-claro"
          >
            <IconoWhatsApp className="size-4" />
            ¿Te quedó alguna duda? Consultanos por WhatsApp
          </a>
        </div>
      </div>

      <Link href="/arma-tu-pc" className="mt-8 inline-block text-sm text-suave hover:text-oro-claro">
        ← Empezar de nuevo
      </Link>
    </section>
  );
}

/**
 * Con o sin armado. Son dos links (como todo Armá tu PC: el estado está en la URL), con forma de opción.
 * aria-current marca la elegida para quien usa lector de pantalla.
 */
function EleccionDeArmado({ armado, precioArmado }: { armado: Armado; precioArmado: number }) {
  const opciones = [
    {
      conArmado: true,
      titulo: "Armada y probada",
      precio: `+ ${precioOConsultar(precioArmado)}`,
      detalle: "La armamos, ordenamos los cables, instalamos y la probamos. Te la llevás lista para usar.",
    },
    {
      conArmado: false,
      titulo: "Solo los componentes",
      precio: "Sin cargo extra",
      detalle: "Te llevás las piezas en sus cajas y la armás vos.",
    },
  ];
  return (
    <fieldset className="mt-8">
      <legend className="font-display text-xl font-bold text-principal italic">¿La armamos nosotros?</legend>
      <div className="mt-3 grid gap-3 sm:grid-cols-2">
        {opciones.map((o) => {
          const elegida = armado.conArmado === o.conArmado;
          return (
            <Link
              key={o.titulo}
              href={urlDelArmador({ ...armado, conArmado: o.conArmado }, "resumen")}
              scroll={false}
              aria-current={elegida ? "true" : undefined}
              className={`flex gap-3 rounded-xl border p-4 transition-colors ${
                elegida ? "border-oro bg-oro/10" : "border-borde bg-superficie hover:border-oro/60"
              }`}
            >
              <span
                aria-hidden
                className={`mt-1 grid size-5 shrink-0 place-items-center rounded-full border-2 ${elegida ? "border-oro" : "border-suave/60"}`}
              >
                {elegida && <span className="size-2.5 rounded-full bg-oro" />}
              </span>
              <span>
                <span className="flex flex-wrap items-baseline gap-x-3">
                  <span className="font-semibold text-principal">{o.titulo}</span>
                  <span className="text-sm text-oro">{o.precio}</span>
                </span>
                <span className="mt-1 block text-sm text-suave">{o.detalle}</span>
              </span>
            </Link>
          );
        })}
      </div>
    </fieldset>
  );
}

// ─── Costado: tu PC hasta ahora ───────────────────────────────────────────────────────────

export function TuPc({ armado }: { armado: Armado }) {
  const { total } = revisarArmado(armado);
  return (
    <div className="rounded-xl border border-borde bg-superficie p-5">
      <h2 className="text-2xl text-principal">Tu PC</h2>
      <ul className="mt-4 space-y-3">
        {PASOS.map(({ tipo, nombre }) => {
          const Icono = iconos[tipo];
          const eleccion = eleccionDe(tipo, armado);
          return (
            <li key={tipo} className="flex items-start gap-3">
              <Icono className={`mt-0.5 size-5 shrink-0 ${eleccion ? "text-oro" : "text-suave/50"}`} />
              <div className="min-w-0">
                <p className="text-xs text-suave">{nombre}</p>
                <p className={`text-sm ${eleccion ? "text-principal" : "text-suave/60"}`}>{eleccion ?? "—"}</p>
              </div>
            </li>
          );
        })}
      </ul>
      <div className="mt-5 border-t border-borde pt-4">
        <p className="text-xs text-suave">Total</p>
        <p className="font-display text-2xl font-bold text-principal">{precioOConsultar(total)}</p>
        <Link href={urlDelArmador(armado, "resumen")} className="mt-2 inline-block text-sm text-oro underline underline-offset-4">
          Ver resumen
        </Link>
      </div>
    </div>
  );
}
