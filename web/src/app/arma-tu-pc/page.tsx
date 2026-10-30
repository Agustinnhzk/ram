import type { Metadata } from "next";
import { Suspense } from "react";
import { obtenerComponentes, obtenerModelosDePc, obtenerTaller } from "@/lib/api";
import { leerArmado, leerPaso, pasoSiguiente } from "@/lib/arma-tu-pc";
import type { ParametrosDeUrl } from "@/lib/catalogo";
import { Inicio, PasoDeEleccion, Progreso, Resumen, TuPc } from "./partes";

export const metadata: Metadata = {
  title: "Armá tu PC",
  description: "Armá tu PC a medida pieza por pieza, con compatibilidad revisada, o partí de uno de nuestros modelos.",
};

/**
 * /arma-tu-pc — TODO el estado está en la URL:
 *   /arma-tu-pc                                  → inicio (elegir modelo o empezar de cero)
 *   /arma-tu-pc?paso=mother&cpu=ryzen-5-5600     → eligiendo la mother, con ese procesador
 *   /arma-tu-pc?paso=resumen&cpu=...&...         → el resumen, listo para el carrito o WhatsApp
 *
 * Cada opción es un LINK a la URL con esa pieza agregada. No hay estado de React: anda sin
 * JavaScript, el botón "atrás" del navegador vuelve al paso anterior, y el link se puede compartir.
 */
export default function Pagina({ searchParams }: PageProps<"/arma-tu-pc">) {
  return (
    <main className="mx-auto max-w-6xl px-4 py-12 sm:px-6 sm:py-16">
      <h1 className="texto-metalico text-5xl sm:text-6xl">Armá tu PC</h1>
      <p className="mt-3 max-w-prose text-lg text-suave">
        Elegí pieza por pieza y nosotros revisamos que todo sea compatible. La armamos, la probamos y te la entregamos lista.
      </p>

      <Suspense fallback={<Cargando />}>
        {searchParams.then((parametros) => (
          <Armador parametros={parametros} />
        ))}
      </Suspense>
    </main>
  );
}

async function Armador({ parametros }: { parametros: ParametrosDeUrl }) {
  const [componentes, modelos, taller] = await Promise.all([obtenerComponentes(), obtenerModelosDePc(), obtenerTaller()]);
  const armado = leerArmado(parametros, componentes);
  const pasoPedido = leerPaso(parametros);
  const nadaElegido = Object.keys(armado.piezas).length === 0 && !armado.sinPlacaDeVideo;

  if (!pasoPedido && nadaElegido) return <Inicio modelos={modelos} componentes={componentes} />;

  // Sin ?paso= pero con piezas (un link compartido viejo): se sigue por lo que falta.
  const paso = pasoPedido ?? pasoSiguiente(armado);

  return (
    <div className="mt-10 grid items-start gap-8 lg:grid-cols-[1fr_19rem]">
      <div className="min-w-0">
        <Progreso armado={armado} pasoActual={paso} />
        {paso === "resumen" ? (
          <Resumen armado={armado} whatsapp={taller.whatsapp} precioArmado={taller.precioArmado} />
        ) : (
          <PasoDeEleccion tipo={paso} armado={armado} componentes={componentes} />
        )}
      </div>
      {/* En compu, "Tu PC" acompaña al costado. En el celular no entra: hay un total arriba de las opciones. */}
      <aside className="hidden lg:sticky lg:top-36 lg:block">
        <TuPc armado={armado} />
      </aside>
    </div>
  );
}

function Cargando() {
  return (
    <div aria-hidden className="mt-10 animate-pulse">
      <div className="h-10 rounded-full bg-superficie" />
      <div className="mt-8 grid gap-4 sm:grid-cols-2">
        {Array.from({ length: 4 }, (_, i) => (
          <div key={i} className="h-40 rounded-xl bg-superficie" />
        ))}
      </div>
    </div>
  );
}
