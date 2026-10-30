import { useState, type FormEvent, type ReactNode } from "react";
import { Link, useNavigate } from "react-router";
import { ErrorDeApi } from "../../api/cliente";
import { useBuscarClientes, useRegistrarIngreso, useUltimoNumero, type ClienteDeOrden } from "../../api/reparaciones";
import { Campo, ESTILO_DE_CAMPO } from "../../componentes/Campo";
import { useRetrasado } from "../../utilidades/useRetrasado";

type ModoCliente = "buscar" | "nuevo";

/**
 * /reparaciones/nueva — el formulario de ingreso, en el orden en que se carga en el mostrador:
 * número, cliente (uno que vuelve o uno nuevo), equipo, y qué le pasa.
 */
export function NuevoIngreso() {
  const navegar = useNavigate();
  const registrar = useRegistrarIngreso();
  const { data: ultimo } = useUltimoNumero();

  const [numero, setNumero] = useState("");
  const [modo, setModo] = useState<ModoCliente>("buscar");
  const [busqueda, setBusqueda] = useState("");
  const [elegido, setElegido] = useState<ClienteDeOrden | null>(null);
  const [nuevo, setNuevo] = useState({ nombreCompleto: "", telefono: "", correo: "" });
  const [equipo, setEquipo] = useState({ descripcion: "", procesador: "", conCargador: false, password: "" });
  const [problema, setProblema] = useState("");
  const [observaciones, setObservaciones] = useState("");

  const encontrados = useBuscarClientes(useRetrasado(busqueda));
  const siguiente = ultimo?.ultimo != null ? ultimo.ultimo + 1 : null;

  const clienteListo = modo === "buscar" ? elegido !== null : nuevo.nombreCompleto.trim() !== "" && nuevo.telefono.trim() !== "";
  const faltan = [
    !/^\d+$/.test(numero) && "número",
    !clienteListo && "cliente",
    equipo.descripcion.trim() === "" && "equipo",
    problema.trim() === "" && "problema",
  ].filter(Boolean);
  const completo = faltan.length === 0;

  function pasarANuevo() {
    setModo("nuevo");
    // Lo que buscó pasa al nombre, con mayúscula en cada palabra: "ana lopez" → "Ana Lopez".
    const nombre = busqueda.trim().toLowerCase().replace(/(^|\s)\p{L}/gu, (letra) => letra.toUpperCase());
    setNuevo((n) => ({ ...n, nombreCompleto: n.nombreCompleto || nombre }));
  }

  function enviar(evento: FormEvent) {
    evento.preventDefault();
    if (!completo) return;
    registrar.mutate(
      {
        numero: Number(numero),
        ...(modo === "buscar"
          ? { clienteId: elegido!.id }
          : { clienteNuevo: { nombreCompleto: nuevo.nombreCompleto, telefono: nuevo.telefono, correo: nuevo.correo || null } }),
        equipo: equipo.descripcion,
        conCargador: equipo.conCargador,
        procesador: equipo.procesador || null,
        password: equipo.password || null,
        problema,
        observaciones: observaciones || null,
      },
      // Al detalle, avisando que es recién ingresada: ahí aparecen el comprobante y el WhatsApp.
      { onSuccess: ({ id }) => navegar(`/reparaciones/${id}`, { state: { recienIngresada: true } }) },
    );
  }

  const error = registrar.error;
  const mensajeDeError =
    error instanceof ErrorDeApi && error.codigo === "numero_repetido"
      ? `Ya hay una orden con el número ${numero}.${siguiente ? ` El siguiente libre es ${siguiente}.` : ""}`
      : error?.message;

  return (
    <>
      <Link to="/reparaciones" className="text-sm text-suave hover:text-oro-claro">
        ← Reparaciones
      </Link>
      <h1 className="texto-metalico mt-2 text-4xl sm:text-5xl">Nuevo ingreso</h1>

      <form onSubmit={enviar} noValidate className="mt-8 max-w-3xl space-y-8">
        <Seccion titulo="Orden">
          <div className="max-w-48">
            <Campo
              id="numero"
              etiqueta="N° de orden"
              ayuda={
                siguiente && (
                  <>
                    El último fue {ultimo?.ultimo}.{" "}
                    <button type="button" onClick={() => setNumero(String(siguiente))} className="text-oro underline underline-offset-2">
                      Usar {siguiente}
                    </button>
                  </>
                )
              }
            >
              <input
                id="numero"
                inputMode="numeric"
                value={numero}
                onChange={(e) => setNumero(e.target.value.replace(/\D/g, ""))}
                required
                autoFocus
                className={`${ESTILO_DE_CAMPO} numeros font-display text-xl font-bold`}
              />
            </Campo>
          </div>
        </Seccion>

        <Seccion titulo="Cliente">
          {modo === "buscar" && elegido && (
            <div className="flex items-center justify-between gap-4 rounded-lg border border-oro/60 bg-oro/5 px-4 py-3">
              <div>
                <p className="font-semibold text-principal">{elegido.nombreCompleto}</p>
                <p className="numeros text-sm text-suave">
                  {elegido.telefono}
                  {elegido.correo && ` · ${elegido.correo}`}
                </p>
              </div>
              <button type="button" onClick={() => setElegido(null)} className="text-sm text-oro underline underline-offset-2">
                Cambiar
              </button>
            </div>
          )}

          {modo === "buscar" && !elegido && (
            <>
              <Campo id="buscar-cliente" etiqueta="¿Ya vino antes? Buscalo por nombre y apellido">
                <input
                  id="buscar-cliente"
                  type="search"
                  value={busqueda}
                  onChange={(e) => setBusqueda(e.target.value)}
                  placeholder="Por ejemplo: agustin perez"
                  autoComplete="off"
                  className={ESTILO_DE_CAMPO}
                />
              </Campo>
              {encontrados.data && encontrados.data.length > 0 && (
                <ul className="mt-2 divide-y divide-borde rounded-lg border border-borde" aria-label="Clientes encontrados">
                  {encontrados.data.map((c) => (
                    <li key={c.id}>
                      <button type="button" onClick={() => setElegido(c)} className="w-full px-4 py-2.5 text-left hover:bg-superficie">
                        <span className="text-principal">{c.nombreCompleto}</span>
                        <span className="numeros ml-2 text-sm text-suave">{c.telefono}</span>
                      </button>
                    </li>
                  ))}
                </ul>
              )}
              {busqueda.trim().length >= 2 && encontrados.data?.length === 0 && (
                <p className="mt-2 text-sm text-suave">No hay clientes con ese nombre.</p>
              )}
              <button type="button" onClick={pasarANuevo} className="mt-3 text-sm font-semibold text-oro underline underline-offset-2">
                + Es un cliente nuevo
              </button>
            </>
          )}

          {modo === "nuevo" && (
            <>
              <div className="grid gap-4 sm:grid-cols-2">
                <Campo id="nombre" etiqueta="Nombre y apellido">
                  <input id="nombre" value={nuevo.nombreCompleto} onChange={(e) => setNuevo({ ...nuevo, nombreCompleto: e.target.value })} required className={ESTILO_DE_CAMPO} />
                </Campo>
                <Campo id="telefono" etiqueta="Teléfono" ayuda="Para avisarle por WhatsApp. Con los últimos 4 números sigue su reparación.">
                  <input id="telefono" type="tel" value={nuevo.telefono} onChange={(e) => setNuevo({ ...nuevo, telefono: e.target.value })} required placeholder="341 555-1234" className={ESTILO_DE_CAMPO} />
                </Campo>
                <Campo id="correo" etiqueta="Correo" opcional>
                  <input id="correo" type="email" value={nuevo.correo} onChange={(e) => setNuevo({ ...nuevo, correo: e.target.value })} className={ESTILO_DE_CAMPO} />
                </Campo>
              </div>
              <button type="button" onClick={() => setModo("buscar")} className="mt-3 text-sm text-oro underline underline-offset-2">
                ← Buscar un cliente que ya vino
              </button>
            </>
          )}
        </Seccion>

        <Seccion titulo="Equipo">
          <div className="grid gap-4 sm:grid-cols-2">
            <Campo id="equipo" etiqueta="Equipo">
              <input id="equipo" value={equipo.descripcion} onChange={(e) => setEquipo({ ...equipo, descripcion: e.target.value })} required placeholder="Notebook Lenovo IdeaPad 3" className={ESTILO_DE_CAMPO} />
            </Campo>
            <Campo id="procesador" etiqueta="Procesador" opcional>
              <input id="procesador" value={equipo.procesador} onChange={(e) => setEquipo({ ...equipo, procesador: e.target.value })} placeholder="Ryzen 5 5500U" className={ESTILO_DE_CAMPO} />
            </Campo>
            <Campo id="password" etiqueta="Password del equipo" opcional ayuda="Solo lo ven el dueño y los técnicos. No sale en el comprobante.">
              <input id="password" value={equipo.password} onChange={(e) => setEquipo({ ...equipo, password: e.target.value })} autoComplete="off" spellCheck={false} className={ESTILO_DE_CAMPO} />
            </Campo>
            <label className="flex items-center gap-3 self-center pt-5 text-principal">
              <input type="checkbox" checked={equipo.conCargador} onChange={(e) => setEquipo({ ...equipo, conCargador: e.target.checked })} className="size-5 accent-[var(--oro)]" />
              Deja el cargador
            </label>
          </div>
        </Seccion>

        <Seccion titulo="Qué le pasa">
          <div className="space-y-4">
            <Campo id="problema" etiqueta="Problema (lo que dice el cliente)">
              <textarea id="problema" value={problema} onChange={(e) => setProblema(e.target.value)} required rows={3} className={ESTILO_DE_CAMPO} />
            </Campo>
            <Campo id="observaciones" etiqueta="Observaciones internas" opcional ayuda="Estado físico, rayones, faltantes… No las ve el cliente.">
              <textarea id="observaciones" value={observaciones} onChange={(e) => setObservaciones(e.target.value)} rows={2} className={ESTILO_DE_CAMPO} />
            </Campo>
          </div>
        </Seccion>

        {mensajeDeError && (
          <p role="alert" className="rounded-lg border border-peligro/60 bg-peligro/10 px-4 py-3 text-sm">
            {mensajeDeError}
          </p>
        )}

        <div className="flex flex-wrap items-center gap-4 border-t border-borde pt-6">
          <button type="submit" disabled={!completo || registrar.isPending} className="boton-oro rounded-full px-6 py-3 font-semibold hover:brightness-110 disabled:opacity-50">
            {registrar.isPending ? "Registrando…" : "Registrar ingreso"}
          </button>
          {!completo && <p className="text-sm text-suave">Falta: {faltan.join(", ")}.</p>}
        </div>
      </form>
    </>
  );
}

function Seccion({ titulo, children }: { titulo: string; children: ReactNode }) {
  return (
    <fieldset className="rounded-xl border border-borde p-5 sm:p-6">
      <legend className="px-2 font-display text-xl font-bold italic text-oro-claro">{titulo}</legend>
      {children}
    </fieldset>
  );
}
