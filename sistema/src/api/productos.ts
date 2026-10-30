import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { enviarFormulario, pedir } from "./cliente";

/** Las formas del JSON de /api/sistema/productos y /api/sistema/categorias (ver ProductosController, ADR-013). */

export type TipoComponente = "CPU" | "MOTHER" | "RAM" | "GPU" | "ALMACENAMIENTO" | "FUENTE" | "GABINETE";
export type TipoRam = "DDR4" | "DDR5";
export type FormatoMother = "ATX" | "MATX";
export type Condicion = "NUEVO" | "REACONDICIONADO";

export type ResumenDeProducto = {
  id: string;
  slug: string;
  nombre: string;
  categoria: { slug: string; nombre: string };
  /** Precio de lista. null = a consultar. */
  precio: number | null;
  /** Lo que se cobra hoy (la oferta, si hay). */
  precioVigente: number | null;
  enOferta: boolean;
  ofertaHasta: string | null;
  publicado: boolean;
  tipoComponente: TipoComponente | null;
  miniatura: string | null;
};

export type Tecnicos = {
  tipo: TipoComponente;
  socket?: string | null;
  tipoRam?: TipoRam | null;
  formato?: FormatoMother | null;
  formatosSoportados?: FormatoMother[];
  consumoW?: number | null;
  potenciaW?: number | null;
  graficosIntegrados?: boolean;
};

export type Ficha = {
  nombre: string;
  categoria: string;
  condicion: Condicion;
  resumen: string;
  descripcion: string;
  caracteristicas: string[];
  garantiaMeses: number | null;
  publicado: boolean;
  tecnicos: Tecnicos | null;
};

export type DetalleDeProducto = Ficha & {
  id: string;
  slug: string;
  precio: number | null;
  precioOferta: number | null;
  ofertaHasta: string | null;
  enOferta: boolean;
  /** Imagen de los datos de ejemplo (ruta de la tienda), si no tiene fotos subidas. */
  imagenHeredada: string | null;
  fotos: { id: string; url: string }[];
};

export type Categoria = { slug: string; nombre: string; conInstalacion: boolean };

const PRODUCTOS = ["productos"] as const;

export function useProductos() {
  return useQuery({ queryKey: [...PRODUCTOS, "lista"], queryFn: () => pedir<ResumenDeProducto[]>("GET", "/api/sistema/productos") });
}

export function useProducto(id: string | undefined) {
  return useQuery({
    queryKey: [...PRODUCTOS, "detalle", id],
    queryFn: () => pedir<DetalleDeProducto>("GET", `/api/sistema/productos/${id}`),
    enabled: !!id,
  });
}

/** Las categorías salen del endpoint público: son las mismas. */
export function useCategorias() {
  return useQuery({ queryKey: ["categorias"], queryFn: () => pedir<Categoria[]>("GET", "/api/publico/categorias") });
}

/** Cualquier cambio en el catálogo vuelve a pedir productos, precios y categorías. */
function useInvalidarCatalogo() {
  const consultas = useQueryClient();
  return () =>
    Promise.all([
      consultas.invalidateQueries({ queryKey: PRODUCTOS }),
      consultas.invalidateQueries({ queryKey: ["precios"] }),
      consultas.invalidateQueries({ queryKey: ["categorias"] }),
    ]);
}

export function useCrearProducto() {
  const invalidar = useInvalidarCatalogo();
  return useMutation({
    mutationFn: (datos: Ficha & { precio: number | null }) => pedir<{ id: string }>("POST", "/api/sistema/productos", datos),
    onSuccess: invalidar,
  });
}

export function useEditarProducto(id: string) {
  const invalidar = useInvalidarCatalogo();
  return useMutation({
    mutationFn: (ficha: Ficha) => pedir<void>("PUT", `/api/sistema/productos/${id}`, ficha),
    onSuccess: invalidar,
  });
}

export function useCambiarPrecio(id: string) {
  const invalidar = useInvalidarCatalogo();
  return useMutation({
    mutationFn: (precio: number | null) =>
      pedir<void>("PUT", `/api/sistema/precios/${id}`, precio === null ? { aConsultar: true } : { precio }),
    onSuccess: invalidar,
  });
}

export function useOferta(id: string) {
  const invalidar = useInvalidarCatalogo();
  return useMutation({
    mutationFn: (oferta: { precio: number; hasta: string } | null) =>
      oferta
        ? pedir<void>("PUT", `/api/sistema/productos/${id}/oferta`, oferta)
        : pedir<void>("DELETE", `/api/sistema/productos/${id}/oferta`),
    onSuccess: invalidar,
  });
}

export function useFotos(id: string) {
  const invalidar = useInvalidarCatalogo();
  const subir = useMutation({
    mutationFn: (archivo: File) => {
      const formulario = new FormData();
      formulario.append("foto", archivo);
      return enviarFormulario<{ id: string }>("POST", `/api/sistema/productos/${id}/fotos`, formulario);
    },
    onSuccess: invalidar,
  });
  const borrar = useMutation({
    mutationFn: (foto: string) => pedir<void>("DELETE", `/api/sistema/productos/${id}/fotos/${foto}`),
    onSuccess: invalidar,
  });
  const ordenar = useMutation({
    mutationFn: (orden: string[]) => pedir<void>("PUT", `/api/sistema/productos/${id}/fotos`, { orden }),
    onSuccess: invalidar,
  });
  return { subir, borrar, ordenar };
}

export function useGuardarCategoria() {
  const invalidar = useInvalidarCatalogo();
  return useMutation({
    mutationFn: async ({ slug, nombre, conInstalacion }: { slug: string | null; nombre: string; conInstalacion: boolean }) => {
      if (slug) await pedir<void>("PUT", `/api/sistema/categorias/${slug}`, { nombre, conInstalacion });
      else await pedir<Categoria>("POST", "/api/sistema/categorias", { nombre, conInstalacion });
    },
    onSuccess: invalidar,
  });
}

// ─── Armado de PC y banners ──────────────────────────────────────────────────────────────

export function usePrecioDelArmado() {
  return useQuery({
    queryKey: ["armado-de-pc"],
    queryFn: async () => {
      try {
        return (await pedir<{ precio: number }>("GET", "/api/publico/armado-de-pc")).precio;
      } catch {
        return null; // 404 = no se ofrece
      }
    },
  });
}

export function useCambiarPrecioDelArmado() {
  const consultas = useQueryClient();
  return useMutation({
    mutationFn: (precio: number | null) => pedir<void>("PUT", "/api/sistema/ajustes/armado-de-pc", { precio }),
    onSuccess: () => consultas.invalidateQueries({ queryKey: ["armado-de-pc"] }),
  });
}

export type Banner = {
  id: string;
  imagen: string;
  textoAlternativo: string;
  enlace: string;
  activo: boolean;
  desde: string | null;
  hasta: string | null;
  orden: number;
  visibleHoy: boolean;
};

export type DatosDeBanner = { textoAlternativo: string; enlace: string; activo: boolean; desde: string | null; hasta: string | null };

const BANNERS = ["banners"] as const;

export function useBanners() {
  return useQuery({ queryKey: BANNERS, queryFn: () => pedir<Banner[]>("GET", "/api/sistema/banners") });
}

export function useAccionesDeBanners() {
  const consultas = useQueryClient();
  const refrescar = () => consultas.invalidateQueries({ queryKey: BANNERS });
  return {
    crear: useMutation({
      mutationFn: ({ datos, imagen }: { datos: DatosDeBanner; imagen: File }) => {
        const formulario = new FormData();
        formulario.append("imagen", imagen);
        formulario.append("textoAlternativo", datos.textoAlternativo);
        formulario.append("enlace", datos.enlace);
        formulario.append("activo", String(datos.activo));
        if (datos.desde) formulario.append("desde", datos.desde);
        if (datos.hasta) formulario.append("hasta", datos.hasta);
        return enviarFormulario<{ id: string }>("POST", "/api/sistema/banners", formulario);
      },
      onSuccess: refrescar,
    }),
    editar: useMutation({
      mutationFn: ({ id, datos }: { id: string; datos: DatosDeBanner }) => pedir<void>("PUT", `/api/sistema/banners/${id}`, datos),
      onSuccess: refrescar,
    }),
    cambiarImagen: useMutation({
      mutationFn: ({ id, imagen }: { id: string; imagen: File }) => {
        const formulario = new FormData();
        formulario.append("imagen", imagen);
        return enviarFormulario<void>("PUT", `/api/sistema/banners/${id}/imagen`, formulario);
      },
      onSuccess: refrescar,
    }),
    borrar: useMutation({
      mutationFn: (id: string) => pedir<void>("DELETE", `/api/sistema/banners/${id}`),
      onSuccess: refrescar,
    }),
    ordenar: useMutation({
      mutationFn: (orden: string[]) => pedir<void>("PUT", "/api/sistema/banners/orden", { orden }),
      onSuccess: refrescar,
    }),
  };
}
