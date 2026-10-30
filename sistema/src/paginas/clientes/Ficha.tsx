import { useState, type FormEvent, type ReactNode } from "react";
import { Link, useNavigate, useParams } from "react-router";
import {
  useBorrarCliente,
  useEditarCliente,
  useFichaDeCliente,
  usePedidosDeCuenta,
  useReparacionesDeCliente,
  useVincularCuenta,
  type FichaDeCliente,
} from "../../api/clientes";
import { useSesion } from "../../api/sesion";
import { BotonBorrar } from "../../componentes/BotonBorrar";
import { Campo, ESTILO_DE_CAMPO } from "../../componentes/Campo";
import { useAvisar } from "../../componentes/contextoDeAvisos";
import { EtiquetaDeEstado } from "../../componentes/EtiquetaDeEstado";
import { EtiquetaDePedido } from "../../componentes/EtiquetaDePedido";
import { IconoWhatsApp } from "../../componentes/IconoWhatsApp";
import { formatearFecha, formatearPrecio } from "../../utilidades/formato";
import { linkWhatsAppDelCliente } from "../../utilidades/pedidos";

/** /clientes/:id — la ficha: datos, notas internas, cuenta de la tienda, reparaciones y pedidos (ADR-014). */
export function Ficha() {
  const { id = "" } = useParams();
  const { data: cliente, isPending, isError, error } = useFichaDeCliente(id);
  const [editando, setEditando] = useState(false);
  const borrar = useBorrarCliente(id);
  const { data: sesion } = useSesion();
  const navegar = useNavigate();

  if (isPending) return <div aria-hidden className="h-96 animate-pulse rounded-xl bg-superficie" />;
  if (isError) {
    return (
      <p role="alert" className="rounded-xl border border-peligro/60 bg-peligro/10 p-4">
        No se pudo cargar el cliente: {error.message}
      </p>
    );
  }

  return (
    <>
      <Link to="/clientes" className="text-sm text-suave hover:text-oro-claro">
        ← Clientes
      </Link>
      <div className="mt-2 flex flex-wrap items-center justify-between gap-4">
        <h1 className="texto-metalico text-4xl break-words sm:text-5xl">{cliente.nombreCompleto}</h1>
        {!editando && (
          <button type="button" onClick={() => setEditando(true)} className="rounded-full border border-borde px-5 py-2 text-sm font-semibold hover:border-oro">
            Editar datos
          </button>
        )}
      </div>
      {cliente.creadoEn && <p className="mt-1 text-suave">Cliente desde el {formatearFecha(cliente.creadoEn)}</p>}

      <div className="mt-8 grid items-start gap-6 lg:grid-cols-[1fr_20rem]">
        <div className="space-y-8">
          {editando ? <FormularioDeFicha cliente={cliente} alTerminar={() => setEditando(false)} /> : <Notas notas={cliente.notas} />}
          <Reparaciones cliente={cliente.id} />
          <Pedidos cliente={cliente} />
        </div>

        <aside className="space-y-4 rounded-xl border border-borde bg-superficie p-5 text-sm">
          <div>
            <p className="text-xs tracking-wide text-suave uppercase">Contacto</p>
            <p className="numeros text-base">{cliente.telefono}</p>
            {cliente.correo && <p className="break-all text-suave">{cliente.correo}</p>}
            <a
              href={linkWhatsAppDelCliente(cliente.telefono, `Hola ${cliente.nombreCompleto.split(" ")[0]}, te escribimos de R.A.M.`)}
              target="_blank"
              rel="noreferrer"
              className="mt-2 inline-flex items-center gap-2 rounded-full border border-borde px-3 py-1.5 hover:border-oro"
            >
              <IconoWhatsApp className="size-4 text-exito" /> Escribirle
            </a>
          </div>
          <CuentaDeLaTienda cliente={cliente} />
          {sesion?.rol === "DUENO" && (
            <div className="border-t border-borde pt-4">
              <BotonBorrar
                texto="Borrar cliente"
                titulo={`¿Borrar a ${cliente.nombreCompleto}?`}
                confirmar="Sí, borrar"
                alBorrar={() => borrar.mutateAsync()}
                alTerminar={() => navegar("/clientes", { replace: true })}
              >
                Se borra la ficha{cliente.cuenta ? " y su cuenta de la tienda (ya no va a poder ingresar)" : ""}. Si tiene pedidos,
                borralos primero; si tiene reparaciones, instalaciones o abonos, no se puede borrar.
              </BotonBorrar>
            </div>
          )}
        </aside>
      </div>
    </>
  );
}

function Notas({ notas }: { notas: string | null }) {
  return (
    <Seccion titulo="Notas internas" ayuda="Solo las ve el taller.">
      {notas ? (
        <p className="rounded-xl border border-borde bg-superficie p-4 whitespace-pre-line">{notas}</p>
      ) : (
        <p className="text-sm text-suave">Sin notas. Tocá “Editar datos” para agregar algo (cómo prefiere que lo contacten, cómo paga…).</p>
      )}
    </Seccion>
  );
}

function FormularioDeFicha({ cliente, alTerminar }: { cliente: FichaDeCliente; alTerminar: () => void }) {
  const editar = useEditarCliente(cliente.id);
  const avisar = useAvisar();
  const [nombre, setNombre] = useState(cliente.nombreCompleto);
  const [telefono, setTelefono] = useState(cliente.telefono);
  const [correo, setCorreo] = useState(cliente.correo ?? "");
  const [notas, setNotas] = useState(cliente.notas ?? "");

  function guardar(evento: FormEvent) {
    evento.preventDefault();
    editar.mutate(
      { nombreCompleto: nombre, telefono, correo: correo.trim() || null, notas: notas.trim() || null },
      {
        onSuccess: () => {
          avisar("Datos guardados");
          alTerminar();
        },
      },
    );
  }

  return (
    <form onSubmit={guardar} className="space-y-4 rounded-xl border border-oro/40 bg-superficie p-5">
      <h2 className="text-2xl">Editar datos</h2>
      <div className="grid gap-4 sm:grid-cols-2">
        <Campo id="ficha-nombre" etiqueta="Nombre y apellido">
          <input id="ficha-nombre" required maxLength={150} value={nombre} onChange={(e) => setNombre(e.target.value)} className={ESTILO_DE_CAMPO} />
        </Campo>
        <Campo id="ficha-telefono" etiqueta="Teléfono">
          <input id="ficha-telefono" required type="tel" value={telefono} onChange={(e) => setTelefono(e.target.value)} className={ESTILO_DE_CAMPO} />
        </Campo>
      </div>
      <Campo id="ficha-correo" etiqueta="Correo" opcional>
        <input id="ficha-correo" type="email" value={correo} onChange={(e) => setCorreo(e.target.value)} className={ESTILO_DE_CAMPO} />
      </Campo>
      <Campo id="ficha-notas" etiqueta="Notas internas" opcional ayuda="Nunca las ve el cliente.">
        <textarea id="ficha-notas" rows={4} maxLength={2000} value={notas} onChange={(e) => setNotas(e.target.value)} className={ESTILO_DE_CAMPO} />
      </Campo>
      {editar.isError && (
        <p role="alert" className="text-sm text-peligro">
          {editar.error.message}
        </p>
      )}
      <div className="flex justify-end gap-3">
        <button type="button" onClick={alTerminar} className="px-4 py-2 text-sm text-suave hover:text-principal">
          Cancelar
        </button>
        <button type="submit" disabled={editar.isPending} className="boton-oro rounded-full px-5 py-2 text-sm font-semibold disabled:opacity-50">
          Guardar
        </button>
      </div>
    </form>
  );
}

/** La cuenta con la que compra en la tienda. Se vincula sola con su primer pedido; acá se corrige a mano. */
function CuentaDeLaTienda({ cliente }: { cliente: FichaDeCliente }) {
  const vincular = useVincularCuenta(cliente.id);
  const avisar = useAvisar();
  const [correo, setCorreo] = useState("");
  const [buscando, setBuscando] = useState(false);

  function enviar(evento: FormEvent) {
    evento.preventDefault();
    vincular.mutate(correo.trim(), {
      onSuccess: () => {
        avisar("Cuenta vinculada");
        setBuscando(false);
        setCorreo("");
      },
    });
  }

  return (
    <div className="border-t border-borde pt-4">
      <p className="text-xs tracking-wide text-suave uppercase">Cuenta de la tienda</p>
      {cliente.cuenta ? (
        <>
          <p className="font-semibold">{cliente.cuenta.nombre}</p>
          <p className="break-all text-suave">{cliente.cuenta.correo}</p>
          <p className="mt-1 text-xs text-suave">Ve sus reparaciones en “Mis pedidos” de la tienda.</p>
          <button
            type="button"
            disabled={vincular.isPending}
            onClick={() => vincular.mutate(null, { onSuccess: () => avisar("Cuenta desvinculada") })}
            className="mt-2 text-xs text-peligro underline underline-offset-4 disabled:opacity-50"
          >
            Desvincular
          </button>
        </>
      ) : buscando ? (
        <form onSubmit={enviar} className="mt-1 space-y-2">
          <label htmlFor="correo-cuenta" className="text-xs text-suave">
            Correo con el que se registró en la tienda
          </label>
          <input id="correo-cuenta" type="email" required value={correo} onChange={(e) => setCorreo(e.target.value)} className={ESTILO_DE_CAMPO} />
          {vincular.isError && (
            <p role="alert" className="text-xs text-peligro">
              {vincular.error.message}
            </p>
          )}
          <div className="flex gap-2">
            <button type="submit" disabled={vincular.isPending} className="boton-oro rounded-full px-4 py-1.5 text-xs font-semibold disabled:opacity-50">
              Vincular
            </button>
            <button type="button" onClick={() => setBuscando(false)} className="px-2 text-xs text-suave">
              Cancelar
            </button>
          </div>
        </form>
      ) : (
        <>
          <p className="text-suave">No tiene. Se vincula sola cuando compra en la tienda.</p>
          <button type="button" onClick={() => setBuscando(true)} className="mt-1 text-xs text-oro-claro underline underline-offset-4">
            Vincular a mano
          </button>
        </>
      )}
    </div>
  );
}

function Reparaciones({ cliente }: { cliente: string }) {
  const { data: ordenes, isPending } = useReparacionesDeCliente(cliente);
  return (
    <Seccion titulo="Reparaciones">
      {isPending && <div aria-hidden className="h-20 animate-pulse rounded-xl bg-superficie" />}
      {ordenes && ordenes.length === 0 && <p className="text-sm text-suave">Ninguna todavía.</p>}
      {ordenes && ordenes.length > 0 && (
        <ul className="divide-y divide-borde rounded-xl border border-borde">
          {ordenes.map((o) => (
            <li key={o.id}>
              <Link to={`/reparaciones/${o.id}`} className="flex flex-wrap items-center justify-between gap-2 px-4 py-3 hover:bg-superficie">
                <span>
                  <span className="numeros font-semibold">{o.numero}</span> · {o.equipo}
                  <span className="numeros block text-xs text-suave">{formatearFecha(o.ingresadoEn)}</span>
                </span>
                <EtiquetaDeEstado estado={o.estado} enGarantia={o.enGarantia} />
              </Link>
            </li>
          ))}
        </ul>
      )}
    </Seccion>
  );
}

function Pedidos({ cliente }: { cliente: FichaDeCliente }) {
  const { data: pedidos, isPending } = usePedidosDeCuenta(cliente.cuenta?.id);
  return (
    <Seccion titulo="Compras en la tienda">
      {!cliente.cuenta && <p className="text-sm text-suave">No tiene cuenta de la tienda vinculada.</p>}
      {cliente.cuenta && isPending && <div aria-hidden className="h-20 animate-pulse rounded-xl bg-superficie" />}
      {pedidos && pedidos.length === 0 && <p className="text-sm text-suave">Ninguna todavía.</p>}
      {pedidos && pedidos.length > 0 && (
        <ul className="divide-y divide-borde rounded-xl border border-borde">
          {pedidos.map((p) => (
            <li key={p.id}>
              <Link to={`/pedidos/${p.id}`} className="flex flex-wrap items-center justify-between gap-2 px-4 py-3 hover:bg-superficie">
                <span>
                  Pedido <span className="numeros font-semibold">{p.numero}</span> · <span className="numeros">{formatearPrecio(p.total)}</span>
                  <span className="numeros block text-xs text-suave">{formatearFecha(p.creadoEn)}</span>
                </span>
                <EtiquetaDePedido estado={p.estado} />
              </Link>
            </li>
          ))}
        </ul>
      )}
    </Seccion>
  );
}

function Seccion({ titulo, ayuda, children }: { titulo: string; ayuda?: string; children: ReactNode }) {
  return (
    <section>
      <h2 className="text-2xl">{titulo}</h2>
      {ayuda && <p className="text-sm text-suave">{ayuda}</p>}
      <div className="mt-3">{children}</div>
    </section>
  );
}
