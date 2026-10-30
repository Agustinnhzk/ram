import { useState } from "react";
import { useClientes } from "../api/clientes";
import { useRetrasado } from "../utilidades/useRetrasado";
import { Campo, ESTILO_DE_CAMPO } from "./Campo";

export type ClienteElegido = { clienteId?: string; clienteNuevo?: { nombreCompleto: string; telefono: string } };

/** Buscar una ficha existente o cargar una nueva con nombre y teléfono (como en el ingreso de reparaciones). */
export function ElegirCliente({ alCambiar }: { alCambiar: (c: ClienteElegido | null) => void }) {
  const [busqueda, setBusqueda] = useState("");
  const texto = useRetrasado(busqueda.trim());
  const { data: encontrados } = useClientes(texto);
  const [elegido, setElegido] = useState<{ id: string; nombre: string } | null>(null);
  const [nuevo, setNuevo] = useState(false);
  const [nombre, setNombre] = useState("");
  const [telefono, setTelefono] = useState("");

  if (elegido) {
    return (
      <p className="flex flex-wrap items-center gap-3">
        <span className="font-semibold">{elegido.nombre}</span>
        <button type="button" onClick={() => { setElegido(null); alCambiar(null); }} className="text-sm text-oro-claro underline underline-offset-4">
          Cambiar
        </button>
      </p>
    );
  }
  if (nuevo) {
    const avisar = (n: string, t: string) => alCambiar(n.trim() && t.trim() ? { clienteNuevo: { nombreCompleto: n, telefono: t } } : null);
    return (
      <div className="grid gap-3 sm:grid-cols-2">
        <Campo id="cliente-nombre" etiqueta="Nombre y apellido">
          <input id="cliente-nombre" required value={nombre} onChange={(e) => { setNombre(e.target.value); avisar(e.target.value, telefono); }} className={ESTILO_DE_CAMPO} />
        </Campo>
        <Campo id="cliente-telefono" etiqueta="Teléfono">
          <input id="cliente-telefono" required type="tel" value={telefono} onChange={(e) => { setTelefono(e.target.value); avisar(nombre, e.target.value); }} className={ESTILO_DE_CAMPO} />
        </Campo>
        <button type="button" onClick={() => { setNuevo(false); alCambiar(null); }} className="justify-self-start text-sm text-suave underline underline-offset-4">
          Buscar uno que ya existe
        </button>
      </div>
    );
  }
  return (
    <div>
      <input
        aria-label="Buscar cliente"
        type="search"
        value={busqueda}
        onChange={(e) => setBusqueda(e.target.value)}
        placeholder="Buscar por nombre o teléfono…"
        className={ESTILO_DE_CAMPO}
      />
      {texto && encontrados && (
        <ul className="mt-2 divide-y divide-borde rounded-lg border border-borde">
          {encontrados.slice(0, 6).map((c) => (
            <li key={c.id}>
              <button
                type="button"
                onClick={() => { setElegido({ id: c.id, nombre: c.nombreCompleto }); alCambiar({ clienteId: c.id }); }}
                className="w-full px-3 py-2 text-left text-sm hover:bg-superficie-alta"
              >
                {c.nombreCompleto} <span className="numeros text-suave">· {c.telefono}</span>
              </button>
            </li>
          ))}
          {encontrados.length === 0 && <li className="px-3 py-2 text-sm text-suave">No hay coincidencias.</li>}
        </ul>
      )}
      <button type="button" onClick={() => setNuevo(true)} className="mt-2 text-sm text-oro-claro underline underline-offset-4">
        + Cliente nuevo
      </button>
    </div>
  );
}
