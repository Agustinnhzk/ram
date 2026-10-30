import { Link } from "react-router";
import { useSesion } from "../api/sesion";
import { IconoCaja, IconoCamara, IconoCarrito, IconoEtiqueta, IconoHerramienta, IconoMegafono, IconoPersonas, IconoProductos, IconoReloj } from "../componentes/Iconos";

const ACCESOS = [
  { titulo: "Reparaciones", texto: "Ingresos, diagnósticos, presupuestos y salidas.", Icono: IconoHerramienta, ruta: "/reparaciones" },
  { titulo: "Pedidos", texto: "Compras de la tienda: revisá comprobantes y entregas.", Icono: IconoCarrito, ruta: "/pedidos" },
  { titulo: "Productos", texto: "Cargá productos y componentes con fotos y ofertas.", Icono: IconoProductos, ruta: "/productos" },
  { titulo: "Precios", texto: "Actualizá precios uno por uno o con un aumento por porcentaje.", Icono: IconoEtiqueta, ruta: "/precios" },
  { titulo: "Promociones", texto: "Los flyers del inicio de la tienda y los productos en oferta.", Icono: IconoMegafono, ruta: "/promociones" },
  { titulo: "Clientes", texto: "Datos, notas, reparaciones y compras de cada cliente.", Icono: IconoPersonas, ruta: "/clientes" },
  { titulo: "Stock", texto: "Cuánto hay de cada producto, reservas e historial.", Icono: IconoCaja, ruta: "/stock" },
  { titulo: "Instalaciones", texto: "Visitas, presupuestos e instalaciones de cámaras y alarmas.", Icono: IconoCamara, ruta: "/instalaciones" },
  { titulo: "Monitoreo", texto: "La planilla de abonos: quién pagó cada mes.", Icono: IconoReloj, ruta: "/monitoreo" },
];

/** / — el inicio del sistema: accesos a cada sección. Las que todavía no existen se ven como "Próximamente". */
export function Inicio() {
  const { data: sesion } = useSesion();
  const nombre = sesion?.nombre.split(" ")[0];

  return (
    <>
      <h1 className="texto-metalico text-4xl sm:text-5xl">Hola{nombre ? `, ${nombre}` : ""}</h1>
      <p className="mt-2 text-suave">¿Qué querés hacer?</p>

      <ul className="mt-8 grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
        {ACCESOS.map(({ titulo, texto, Icono, ruta }) => (
          <li key={titulo}>
            {ruta ? (
              <Link
                to={ruta}
                className="flex h-full flex-col rounded-xl border border-borde bg-superficie p-5 transition-colors hover:border-oro"
              >
                <Icono className="size-8 text-oro" />
                <span className="mt-3 font-display text-xl font-bold italic">{titulo}</span>
                <span className="mt-1 text-sm text-suave">{texto}</span>
              </Link>
            ) : (
              <div className="flex h-full flex-col rounded-xl border border-dashed border-borde p-5 opacity-60">
                <Icono className="size-8 text-suave" />
                <span className="mt-3 font-display text-xl font-bold italic">{titulo}</span>
                <span className="mt-1 text-sm text-suave">{texto}</span>
                <span className="mt-3 text-xs tracking-wide text-suave uppercase">Próximamente</span>
              </div>
            )}
          </li>
        ))}
      </ul>
    </>
  );
}
