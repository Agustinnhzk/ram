import { useState, type FormEvent } from "react";
import { useCambiarPrecioDelArmado, usePrecioDelArmado } from "../../api/productos";
import { ESTILO_DE_CAMPO } from "../../componentes/Campo";
import { useAvisar } from "../../componentes/contextoDeAvisos";
import { formatearPrecio, leerPrecio } from "../../utilidades/formato";

/** El precio del servicio de armado de Armá tu PC. Vacío = no se ofrece (solo se venden los componentes). */
export function TarjetaArmado({ esDueno }: { esDueno: boolean }) {
  const { data: precio, isPending } = usePrecioDelArmado();
  const cambiar = useCambiarPrecioDelArmado();
  const avisar = useAvisar();
  const [texto, setTexto] = useState<string | null>(null);
  const valor = texto ?? (precio == null ? "" : String(precio));

  function guardar(evento: FormEvent) {
    evento.preventDefault();
    const nuevo = valor.trim() ? leerPrecio(valor) : null;
    if (valor.trim() && nuevo === null) return avisar("El precio tiene que ser un número de pesos, sin centavos", "error");
    cambiar.mutate(nuevo, {
      onSuccess: () => {
        avisar(nuevo === null ? "Ya no se ofrece el armado" : "Precio del armado actualizado");
        setTexto(null);
      },
      onError: (e) => avisar(e.message, "error"),
    });
  }

  return (
    <form onSubmit={guardar} className="mt-6 flex flex-wrap items-end gap-3 rounded-xl border border-borde bg-superficie p-4">
      <div className="min-w-60 flex-1">
        <label htmlFor="precio-armado" className="font-semibold">
          Armado y prueba de PC (Armá tu PC)
        </label>
        <p className="text-sm text-suave">
          {isPending ? "…" : precio == null ? "Ahora no se ofrece: solo se venden los componentes." : `Ahora: ${formatearPrecio(precio)} por PC.`}
          {esDueno && " Vacío = dejar de ofrecerlo."}
        </p>
      </div>
      <fieldset disabled={!esDueno} className="flex gap-2">
        <input id="precio-armado" inputMode="numeric" value={valor} onChange={(e) => setTexto(e.target.value)} placeholder="Sin armado" className={`numeros w-40 ${ESTILO_DE_CAMPO}`} />
        <button type="submit" disabled={cambiar.isPending} className="rounded-full border border-borde px-4 text-sm font-semibold hover:border-oro">
          Guardar
        </button>
      </fieldset>
    </form>
  );
}
