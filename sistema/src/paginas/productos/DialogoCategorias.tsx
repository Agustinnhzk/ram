import { useState, type FormEvent } from "react";
import { useCategorias, useGuardarCategoria, type Categoria } from "../../api/productos";
import { ESTILO_DE_CAMPO } from "../../componentes/Campo";
import { useAvisar } from "../../componentes/contextoDeAvisos";
import { Dialogo } from "../../componentes/Dialogo";

/** Crear y renombrar categorías. El slug (lo que va en la dirección de la tienda) no cambia nunca. */
export function DialogoCategorias({ abierto, alCerrar }: { abierto: boolean; alCerrar: () => void }) {
  const { data: categorias } = useCategorias();
  const guardar = useGuardarCategoria();
  const avisar = useAvisar();
  const [editando, setEditando] = useState<Categoria | null>(null);
  const [nombre, setNombre] = useState("");
  const [conInstalacion, setConInstalacion] = useState(false);

  function empezar(c: Categoria | null) {
    setEditando(c);
    setNombre(c?.nombre ?? "");
    setConInstalacion(c?.conInstalacion ?? false);
  }

  function enviar(evento: FormEvent) {
    evento.preventDefault();
    guardar.mutate(
      { slug: editando?.slug ?? null, nombre, conInstalacion },
      {
        onSuccess: () => {
          avisar(editando ? "Categoría actualizada" : "Categoría creada");
          empezar(null);
        },
        onError: (e) => avisar(e.message, "error"),
      },
    );
  }

  return (
    <Dialogo abierto={abierto} alCerrar={alCerrar} titulo="Categorías" ancho="max-w-lg">
      <ul className="divide-y divide-borde rounded-lg border border-borde">
        {categorias?.map((c) => (
          <li key={c.slug} className="flex items-center justify-between gap-3 px-3 py-2 text-sm">
            <span>
              {c.nombre}
              {c.conInstalacion && <span className="ml-2 text-xs text-suave">(con instalación)</span>}
            </span>
            <button type="button" onClick={() => empezar(c)} className="text-oro underline underline-offset-4">
              Editar
            </button>
          </li>
        ))}
      </ul>

      <form onSubmit={enviar} className="mt-5 space-y-3 rounded-lg border border-borde p-4">
        <p className="font-semibold">{editando ? `Editar “${editando.nombre}”` : "Nueva categoría"}</p>
        <label htmlFor="nombre-categoria" className="sr-only">
          Nombre
        </label>
        <input
          id="nombre-categoria"
          required
          maxLength={80}
          value={nombre}
          onChange={(e) => setNombre(e.target.value)}
          placeholder="Por ejemplo: Redes y Wi-Fi"
          className={ESTILO_DE_CAMPO}
        />
        <label className="flex items-center gap-2 text-sm">
          <input type="checkbox" checked={conInstalacion} onChange={(e) => setConInstalacion(e.target.checked)} />
          Se instala a domicilio (la tienda lo aclara en cada producto)
        </label>
        <div className="flex justify-end gap-3">
          {editando && (
            <button type="button" onClick={() => empezar(null)} className="px-3 py-2 text-sm text-suave">
              Cancelar
            </button>
          )}
          <button type="submit" disabled={guardar.isPending} className="boton-oro rounded-full px-5 py-2 text-sm font-semibold">
            {editando ? "Guardar" : "Crear"}
          </button>
        </div>
      </form>
    </Dialogo>
  );
}
