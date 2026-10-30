import { useState, type FormEvent } from "react";
import { Link, useNavigate } from "react-router";
import { useClientes, useCrearCliente } from "../../api/clientes";
import { Campo, ESTILO_DE_CAMPO } from "../../componentes/Campo";
import { useAvisar } from "../../componentes/contextoDeAvisos";
import { Dialogo } from "../../componentes/Dialogo";
import { IconoLupa } from "../../componentes/Iconos";
import { useRetrasado } from "../../utilidades/useRetrasado";

/**
 * /clientes — las fichas del taller (ADR-014). Las de reparaciones y las de quienes compran en la tienda
 * son las mismas: una persona, una ficha.
 */
export function Listado() {
  const [busqueda, setBusqueda] = useState("");
  const texto = useRetrasado(busqueda.trim());
  const { data: clientes, isPending, isError, error, isFetching } = useClientes(texto);
  const [nuevoAbierto, setNuevoAbierto] = useState(false);
  const navegar = useNavigate();

  return (
    <>
      <div className="flex flex-wrap items-end justify-between gap-4">
        <div>
          <h1 className="texto-metalico text-4xl sm:text-5xl">Clientes</h1>
          <p className="mt-2 text-suave">Cada ficha junta sus reparaciones y sus compras en la tienda.</p>
        </div>
        <button type="button" onClick={() => setNuevoAbierto(true)} className="boton-oro rounded-full px-5 py-2.5 font-semibold hover:brightness-110">
          + Nuevo cliente
        </button>
      </div>

      <div className="mt-6 flex items-center rounded-full border border-borde bg-superficie pl-4 focus-within:border-oro">
        <IconoLupa className="size-5 text-suave" />
        <label htmlFor="buscar-cliente" className="sr-only">
          Buscar cliente
        </label>
        <input
          id="buscar-cliente"
          type="search"
          value={busqueda}
          onChange={(e) => setBusqueda(e.target.value)}
          placeholder="Nombre o teléfono…"
          className="w-full bg-transparent px-3 py-2 text-principal placeholder:text-suave/70 focus:outline-none"
        />
      </div>
      <p className="mt-2 text-xs text-suave" aria-live="polite">
        {texto ? (isFetching ? "Buscando…" : `${clientes?.length ?? 0} encontrados`) : "Los últimos clientes cargados"}
      </p>

      {isPending && <div aria-hidden className="mt-4 h-80 animate-pulse rounded-xl bg-superficie" />}
      {isError && (
        <p role="alert" className="mt-4 rounded-xl border border-peligro/60 bg-peligro/10 p-4">
          No se pudieron cargar los clientes: {error.message}
        </p>
      )}
      {clientes && clientes.length === 0 && (
        <p className="mt-4 rounded-xl border border-dashed border-borde px-6 py-12 text-center">
          {texto ? "No hay clientes que coincidan." : "Todavía no hay clientes cargados."}
        </p>
      )}
      {clientes && clientes.length > 0 && (
        <ul className="mt-4 divide-y divide-borde rounded-xl border border-borde">
          {clientes.map((c) => (
            <li key={c.id}>
              <Link to={`/clientes/${c.id}`} className="flex flex-wrap items-center justify-between gap-x-4 gap-y-1 px-4 py-3 hover:bg-superficie">
                <span className="min-w-0">
                  <span className="block truncate font-semibold text-principal">{c.nombreCompleto}</span>
                  <span className="numeros text-sm text-suave">
                    {c.telefono}
                    {c.correo && <span className="font-sans"> · {c.correo}</span>}
                  </span>
                </span>
                {c.cuenta && (
                  <span className="rounded-full border border-oro/50 px-2.5 py-0.5 text-xs text-oro-claro">Compra en la tienda</span>
                )}
              </Link>
            </li>
          ))}
        </ul>
      )}

      <DialogoNuevoCliente
        abierto={nuevoAbierto}
        alCerrar={() => setNuevoAbierto(false)}
        alCrear={(id) => navegar(`/clientes/${id}`)}
      />
    </>
  );
}

function DialogoNuevoCliente(props: { abierto: boolean; alCerrar: () => void; alCrear: (id: string) => void }) {
  const crear = useCrearCliente();
  const avisar = useAvisar();
  const [nombre, setNombre] = useState("");
  const [telefono, setTelefono] = useState("");
  const [correo, setCorreo] = useState("");

  function enviar(evento: FormEvent) {
    evento.preventDefault();
    crear.mutate(
      { nombreCompleto: nombre, telefono, correo: correo.trim() || null },
      {
        onSuccess: ({ id }) => {
          avisar("Cliente cargado");
          setNombre("");
          setTelefono("");
          setCorreo("");
          props.alCrear(id);
        },
      },
    );
  }

  return (
    <Dialogo abierto={props.abierto} alCerrar={props.alCerrar} titulo="Nuevo cliente" ancho="max-w-lg">
      <form onSubmit={enviar} className="space-y-4">
        <Campo id="nuevo-nombre" etiqueta="Nombre y apellido">
          <input id="nuevo-nombre" required maxLength={150} value={nombre} onChange={(e) => setNombre(e.target.value)} className={ESTILO_DE_CAMPO} />
        </Campo>
        <Campo id="nuevo-telefono" etiqueta="Teléfono">
          <input id="nuevo-telefono" required type="tel" value={telefono} onChange={(e) => setTelefono(e.target.value)} className={ESTILO_DE_CAMPO} />
        </Campo>
        <Campo id="nuevo-correo" etiqueta="Correo" opcional>
          <input id="nuevo-correo" type="email" value={correo} onChange={(e) => setCorreo(e.target.value)} className={ESTILO_DE_CAMPO} />
        </Campo>
        {crear.isError && (
          <p role="alert" className="text-sm text-peligro">
            {crear.error.message}
          </p>
        )}
        <div className="flex justify-end gap-3">
          <button type="button" onClick={props.alCerrar} className="px-4 py-2 text-sm text-suave hover:text-principal">
            Cancelar
          </button>
          <button type="submit" disabled={crear.isPending} className="boton-oro rounded-full px-5 py-2 text-sm font-semibold disabled:opacity-50">
            Guardar
          </button>
        </div>
      </form>
    </Dialogo>
  );
}
