import type { ComponentType } from "react";
import { NavLink, Outlet, useNavigate } from "react-router";
import { NOMBRE_DEL_ROL, useCerrarSesion, useSesion } from "../api/sesion";
import { BotonTema } from "./BotonTema";
import { IconoCaja, IconoCamara, IconoCarrito, IconoMegafono, IconoProductos, IconoEtiqueta, IconoHerramienta, IconoInicio, IconoPersonas, IconoReloj, IconoSalir } from "./Iconos";

type Seccion = { texto: string; Icono: ComponentType<{ className?: string }>; ruta?: string };

/** Las secciones del sistema. Las que no tienen ruta todavía no existen: se muestran como "Próximamente". */
const SECCIONES: Seccion[] = [
  { texto: "Inicio", Icono: IconoInicio, ruta: "/" },
  { texto: "Reparaciones", Icono: IconoHerramienta, ruta: "/reparaciones" },
  { texto: "Pedidos", Icono: IconoCarrito, ruta: "/pedidos" },
  { texto: "Productos", Icono: IconoProductos, ruta: "/productos" },
  { texto: "Precios", Icono: IconoEtiqueta, ruta: "/precios" },
  { texto: "Promociones", Icono: IconoMegafono, ruta: "/promociones" },
  { texto: "Clientes", Icono: IconoPersonas, ruta: "/clientes" },
  { texto: "Stock", Icono: IconoCaja, ruta: "/stock" },
  { texto: "Instalaciones", Icono: IconoCamara, ruta: "/instalaciones" },
  { texto: "Monitoreo", Icono: IconoReloj, ruta: "/monitoreo" },
];

/** El marco de todas las pantallas con login: menú, usuario y salir. Lo de cada pantalla va en <Outlet />. */
export function Marco() {
  const { data: sesion } = useSesion();
  const cerrarSesion = useCerrarSesion();
  const navegar = useNavigate();

  function salir() {
    cerrarSesion.mutate(undefined, { onSettled: () => navegar("/ingresar", { replace: true }) });
  }

  return (
    <div className="min-h-dvh lg:grid lg:grid-cols-[15rem_1fr]">
      <a
        href="#contenido"
        className="sr-only focus:not-sr-only focus:fixed focus:top-2 focus:left-2 focus:z-50 focus:rounded focus:bg-oro focus:px-3 focus:py-2 focus:text-negro"
      >
        Saltar al contenido
      </a>

      <aside className="border-b border-borde bg-superficie lg:sticky lg:top-0 lg:flex lg:h-dvh lg:flex-col lg:border-r lg:border-b-0">
        <div className="flex items-center justify-between gap-3 px-4 py-3 lg:px-5 lg:py-5">
          <div className="flex items-center gap-3">
            <img src="/marca/logo.png" alt="" className="size-10" />
            <div className="leading-tight">
              <p className="font-display text-lg font-extrabold italic">R.A.M.</p>
              <p className="text-xs text-oro">Sistema de gestión</p>
            </div>
          </div>
          <div className="lg:hidden">
            <BotonTema />
          </div>
        </div>

        <nav aria-label="Secciones del sistema" className="overflow-x-auto px-2 pb-2 lg:flex-1 lg:px-3">
          <ul className="flex gap-1 lg:flex-col">
            {SECCIONES.map(({ texto, Icono, ruta }) => (
              <li key={texto} className="shrink-0">
                {ruta ? (
                  <NavLink
                    to={ruta}
                    end={ruta === "/"}
                    className={({ isActive }) =>
                      `flex items-center gap-3 rounded-lg px-3 py-2 text-sm ${
                        isActive ? "bg-oro/15 text-oro-claro" : "text-principal hover:bg-superficie-alta"
                      }`
                    }
                  >
                    <Icono className="size-5 shrink-0" />
                    {texto}
                  </NavLink>
                ) : (
                  <span className="flex cursor-default items-center gap-3 rounded-lg px-3 py-2 text-sm text-suave/60" title="Próximamente">
                    <Icono className="size-5 shrink-0" />
                    {texto}
                    <span className="hidden rounded-full border border-borde px-1.5 text-[0.65rem] lg:inline">Pronto</span>
                  </span>
                )}
              </li>
            ))}
          </ul>
        </nav>

        <div className="hidden border-t border-borde p-4 lg:block">
          <p className="truncate text-sm font-semibold">{sesion?.nombre}</p>
          <p className="text-xs text-suave">{sesion && NOMBRE_DEL_ROL[sesion.rol]}</p>
          <div className="mt-3 flex items-center gap-2">
            <BotonTema />
            <button
              type="button"
              onClick={salir}
              className="flex items-center gap-2 rounded-full border border-borde px-3 py-1.5 text-sm hover:border-oro hover:text-oro-claro"
            >
              <IconoSalir className="size-4" />
              Salir
            </button>
          </div>
        </div>
      </aside>

      <div className="min-w-0">
        {/* En el celular, el usuario y Salir van arriba del contenido. */}
        <div className="flex items-center justify-between border-b border-borde px-4 py-2 text-sm lg:hidden">
          <span className="truncate">
            {sesion?.nombre} · <span className="text-suave">{sesion && NOMBRE_DEL_ROL[sesion.rol]}</span>
          </span>
          <button type="button" onClick={salir} className="flex items-center gap-1.5 text-oro">
            <IconoSalir className="size-4" />
            Salir
          </button>
        </div>
        <main id="contenido" className="mx-auto max-w-6xl px-4 py-8 sm:px-8">
          <Outlet />
        </main>
      </div>
    </div>
  );
}
