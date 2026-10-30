/**
 * Forma de los datos que devuelve (o va a devolver) el backend.
 *
 * Es el CONTRATO entre la web y la API: las pantallas solo conocen estos tipos.
 * Cuando el backend exista, sus respuestas tienen que tener exactamente esta forma.
 */

/** Datos públicos del taller: lo que hace que la web sea "de RAM". Viene de la configuración del taller. */
export type Taller = {
  nombre: string;
  nombreCompleto: string;
  eslogan: string;
  ciudad: string;
  /** Formato internacional sin "+", para armar links de WhatsApp: 5493417847613 */
  whatsapp: string;
  /** Cómo se muestra el número a las personas */
  whatsappVisible: string;
  instagram: string;
  /** El mail de contacto del negocio. null = todavía no hay: no se muestra en ningún lado. */
  correo: string | null;
  /** Lo que cobra el taller por armar y probar una PC de Armá tu PC. null = no ofrece el armado. */
  precioArmado: number | null;
};

/**
 * Una categoría del catálogo. Viene de la API (y no es una lista fija en el código) porque
 * cada taller va a tener las suyas cuando la plataforma sea para varios talleres.
 */
export type Categoria = {
  /** Lo que va en la URL: /productos?categoria=camaras */
  slug: string;
  nombre: string;
  /** Si los productos de esta categoría se instalan a domicilio (cámaras, alarmas). */
  conInstalacion: boolean;
};

export type Condicion = "nuevo" | "reacondicionado";

/**
 * El stock como lo muestra la tienda (ADR-014). null en el producto = el taller no controla su stock:
 * se vende como siempre, sin decir nada.
 */
export type Disponibilidad = {
  estado: "DISPONIBLE" | "ULTIMAS_UNIDADES" | "SIN_STOCK";
  /** Solo con ULTIMAS_UNIDADES: "Últimas 2". */
  quedan: number | null;
};

export type Producto = {
  id: string;
  slug: string;
  nombre: string;
  /** Slug de su categoría. */
  categoria: string;
  condicion: Condicion;
  /** Lo que se cobra HOY (si está en oferta, el de oferta). null = "Consultar precio". */
  precio: number | null;
  /** Solo si está en oferta: el precio de lista, para mostrarlo tachado. */
  precioAnterior: number | null;
  /** Último día de la oferta ("2026-11-30"), o null. */
  ofertaHasta: string | null;
  /** La foto principal (grande), o null si todavía no tiene. */
  imagen: string | null;
  /** La principal en chico (600 px), para las tarjetas. */
  miniatura: string | null;
  /** Todas las fotos, en orden, para la galería. */
  imagenes: string[];
  /** Una línea, para la tarjeta del catálogo. */
  resumen: string;
  /** Uno o más párrafos, para la página del producto. */
  descripcion: string;
  caracteristicas: string[];
  garantiaMeses: number | null;
  /** null = sin control de stock. Sin stock se puede comprar igual: queda reservado. */
  stock: Disponibilidad | null;
};

/**
 * Los filtros del catálogo, tal como llegan en la URL: /productos?categoria=camaras&condicion=nuevo&q=dvr
 * Todos son opcionales: sin ninguno, se ve el catálogo completo.
 */
export type FiltrosCatalogo = {
  categoria?: string;
  condicion?: Condicion;
  q?: string;
  /** Solo los que están en oferta. */
  oferta?: boolean;
};

/** Las piezas de una PC, en el orden en que se eligen en Armá tu PC. */
export type TipoComponente = "cpu" | "mother" | "ram" | "gpu" | "almacenamiento" | "fuente" | "gabinete";

export type TipoRam = "DDR4" | "DDR5";
export type FormatoMother = "ATX" | "mATX";

/**
 * Un componente de Armá tu PC. Además de lo comercial, trae los pocos datos técnicos
 * que hacen falta para revisar la compatibilidad básica. Cada tipo usa los suyos:
 * - cpu: socket, consumoW, graficosIntegrados
 * - mother: socket, tipoRam, formato
 * - ram: tipoRam
 * - gpu: consumoW
 * - fuente: potenciaW
 * - gabinete: formatosSoportados
 */
export type Componente = {
  id: string;
  slug: string;
  tipo: TipoComponente;
  nombre: string;
  /** null = todavía sin precio cargado: se puede elegir, pero el total queda "a consultar". */
  precio: number | null;
  /** Datos cortos para mostrar: "6 núcleos", "16 GB", "80 Plus Bronze"... */
  detalles: string[];
  socket?: string;
  tipoRam?: TipoRam;
  formato?: FormatoMother;
  formatosSoportados?: FormatoMother[];
  consumoW?: number;
  potenciaW?: number;
  graficosIntegrados?: boolean;
  stock: Disponibilidad | null;
};

/**
 * Una PC armada de antemano ("Gamer inicial", "Oficina"), para partir de ahí y cambiar lo que se quiera.
 * componentes: el slug elegido para cada pieza; gpu ausente = usa los gráficos del procesador.
 */
export type ModeloDePc = {
  slug: string;
  nombre: string;
  descripcion: string;
  componentes: Partial<Record<TipoComponente, string>>;
};

/** Qué dibujo acompaña a un servicio. La web tiene un ícono para cada uno de estos nombres. */
export type IconoDeServicio = "camara" | "alarma" | "monitoreo" | "notebook" | "pc" | "armado" | "venta" | "codigo";

export type Servicio = {
  slug: string;
  nombre: string;
  resumen: string;
  /** Lo que incluye, en pocas palabras: "Cambio de repuestos", "Bisagras"... */
  incluye: string[];
  icono: IconoDeServicio;
  /** Opcional: un link a otra parte de la web (los kits de cámaras, Armá tu PC). */
  enlace?: { href: string; texto: string };
};

/** Los servicios agrupados como se muestran en /servicios. El slug es el ancla: /servicios#reparacion */
export type GrupoDeServicios = {
  slug: string;
  titulo: string;
  descripcion: string;
  /** Lo que vale para todo el grupo: "Retiro y entrega", "Sin costo si no se logra reparar"... */
  ventajas: string[];
  servicios: Servicio[];
  /** false = se menciona al final en una línea, sin tarjetas (por ejemplo, desarrollo de software). */
  destacado: boolean;
};

/** El contenido de /quienes-somos. Futuro: se edita desde el sistema de gestión. */
export type QuienesSomos = {
  /** El texto principal, en una o dos oraciones. */
  presentacion: string;
  /** Qué significa la sigla del nombre, si la tiene. */
  significadoDelNombre: string | null;
  /** Números o datos cortos que se muestran grandes: "+3" / "años de experiencia". */
  datos: { valor: string; texto: string }[];
  /** Lo que define al taller: un título y una explicación corta cada uno. */
  valores: { titulo: string; texto: string }[];
};

/** Una imagen del carrusel de la home. Se cargan desde el sistema de gestión. */
export type Promocion = {
  id: string;
  imagen: string;
  textoAlternativo: string;
  enlace: string;
};
