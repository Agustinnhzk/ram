import type { GrupoDeServicios, ModeloDePc, QuienesSomos, Taller } from "./tipos";

/**
 * Lo que todavía no está en el backend: datos del taller, servicios, quiénes somos y modelos de Armá tu PC.
 * El catálogo, los componentes y los banners ya vienen de la API (ver index.ts).
 * Tienen la misma forma que va a devolver la API cuando exista (ver tipos.ts).
 * Solo los usa ./index.ts: ninguna pantalla importa este archivo directamente.
 */

export const taller: Taller = {
  nombre: "R.A.M.",
  nombreCompleto: "R.A.M. Seguridad y Tecnología",
  eslogan: "Tu seguridad, en buenas manos.",
  ciudad: "Rosario",
  whatsapp: "5493417847613",
  whatsappVisible: "341 784-7613",
  instagram: "r.a.mantenimiento",
  // El mail del negocio (el del hosting, por ejemplo reparaciones@tudominio.com.ar). Con null no se muestra.
  // Cambiándolo acá aparece en el pie de todas las páginas, en Quiénes somos y en Servicios.
  correo: null,
  // Se pisa con el del backend (GET /api/publico/armado-de-pc): se edita desde el sistema.
  precioArmado: null,
};




export const gruposDeServicios: GrupoDeServicios[] = [
  {
    slug: "instalaciones",
    titulo: "Cámaras, alarmas y monitoreo",
    descripcion: "Instalamos sistemas de seguridad en casas y negocios, y te los dejamos funcionando en el celular.",
    ventajas: ["Hogar y empresa", "Interior y exterior", "Inalámbricas y cableadas"],
    destacado: true,
    servicios: [
      {
        slug: "instalacion-de-camaras",
        nombre: "Instalación de cámaras de seguridad",
        resumen: "Cámaras con grabación para ver tu casa o tu negocio desde donde estés.",
        incluye: ["Kits con DVR", "Visión nocturna", "Configuración en tu celular"],
        icono: "camara",
        enlace: { href: "/productos?categoria=camaras", texto: "Ver kits de cámaras" },
      },
      {
        slug: "instalacion-de-alarmas",
        nombre: "Instalación de alarmas",
        resumen: "Alarmas DSC, X28 y Garnet, con sensores, sirena y aviso inmediato.",
        incluye: ["Sensores y sirena", "Control desde el celular", "Sin obra en las inalámbricas"],
        icono: "alarma",
        enlace: { href: "/productos?categoria=alarmas", texto: "Ver kits de alarmas" },
      },
      {
        slug: "monitoreo",
        nombre: "Monitoreo",
        resumen: "Un abono mensual para que tu sistema de seguridad esté vigilado en tiempo real.",
        incluye: ["Abono mensual", "Débito automático o transferencia", "Monitoreo en tiempo real"],
        icono: "monitoreo",
      },
    ],
  },
  {
    slug: "reparacion",
    titulo: "Servicio técnico",
    descripcion: "Reparamos notebooks y PC en el local o a domicilio, y armamos equipos a medida.",
    ventajas: ["A domicilio o en el local", "Retiro y entrega", "Sin costo si no se logra reparar"],
    destacado: true,
    servicios: [
      {
        slug: "reparacion-de-notebooks",
        nombre: "Reparación de notebooks",
        resumen: "Cambio de repuestos, reparación de bisagras y fallas de encendido.",
        incluye: ["Cambio de repuestos", "Bisagras", "Fallas de encendido"],
        icono: "notebook",
      },
      {
        slug: "reparacion-de-pc",
        nombre: "Reparación de PC",
        resumen: "Diagnóstico y reparación de tu PC de escritorio.",
        incluye: ["Diagnóstico", "Cambio de componentes"],
        icono: "pc",
      },
      {
        slug: "armado-de-pc",
        nombre: "Armado de PC",
        resumen: "PC a medida para jugar, trabajar o estudiar, armada, probada y lista para usar.",
        incluye: ["Componentes a elección", "Sistema instalado", "Garantía"],
        icono: "armado",
        enlace: { href: "/arma-tu-pc", texto: "Armá tu PC" },
      },
    ],
  },
  {
    slug: "venta",
    titulo: "Venta de equipos e insumos",
    descripcion: "Todo lo que instalamos y reparamos, también lo vendemos.",
    ventajas: ["Garantía incluida", "Asesoramiento para elegir"],
    destacado: true,
    servicios: [
      {
        slug: "venta-de-insumos",
        nombre: "Alarmas, cámaras, notebooks y PC",
        resumen: "Kits de seguridad, notebooks nuevas y reacondicionadas, y PC armadas.",
        incluye: ["Notebooks nuevas: 12 meses de garantía", "Reacondicionadas: 3 meses de garantía"],
        icono: "venta",
        enlace: { href: "/productos", texto: "Ver productos" },
      },
    ],
  },
  {
    slug: "software",
    titulo: "Desarrollo de software",
    descripcion: "También desarrollamos software a medida. Si tenés una idea, contanos.",
    ventajas: [],
    destacado: false,
    servicios: [],
  },
];

export const quienesSomos: QuienesSomos = {
  presentacion: "Somos una empresa integral de servicios de IT con más de 3 años de experiencia en el rubro.",
  significadoDelNombre: "Reparación, Asesoramiento y Mantenimiento",
  datos: [
    { valor: "+3", texto: "años de experiencia" },
    { valor: "8", texto: "servicios en un solo lugar" },
    { valor: "A domicilio", texto: "o en el local" },
  ],
  valores: [
    {
      titulo: "Integral",
      texto: "Reparamos, instalamos, monitoreamos y vendemos: no tenés que buscar a un técnico para cada cosa.",
    },
    {
      titulo: "Cerca tuyo",
      texto: "Vamos a tu casa o a tu negocio, retiramos y entregamos los equipos.",
    },
    {
      titulo: "Con garantía",
      texto: "Lo que vendemos tiene garantía, y si una reparación no se logra, no la cobramos.",
    },
  ],
};


export const modelosDePc: ModeloDePc[] = [
  {
    slug: "oficina",
    nombre: "Oficina y estudio",
    descripcion: "Para trabajar, estudiar y navegar sin trabas. Sin placa de video: usa los gráficos del procesador.",
    componentes: {
      cpu: "core-i3-12100",
      mother: "h610m",
      ram: "ddr4-8gb",
      almacenamiento: "ssd-nvme-500gb",
      fuente: "fuente-500w",
      gabinete: "gabinete-matx",
    },
  },
  {
    slug: "gamer-inicial",
    nombre: "Gamer inicial",
    descripcion: "Para jugar en 1080p los juegos más populares.",
    componentes: {
      cpu: "ryzen-5-5600",
      mother: "a520m",
      ram: "ddr4-16gb",
      gpu: "rtx-3050",
      almacenamiento: "ssd-nvme-1tb",
      fuente: "fuente-500w",
      gabinete: "gabinete-matx",
    },
  },
  {
    slug: "gamer-avanzada",
    nombre: "Gamer avanzada",
    descripcion: "Para jugar en 1440p con todo al máximo, y para streaming o edición.",
    componentes: {
      cpu: "ryzen-7-7800x3d",
      mother: "b650",
      ram: "ddr5-32gb",
      gpu: "rtx-4070-super",
      almacenamiento: "ssd-nvme-1tb",
      fuente: "fuente-750w",
      gabinete: "gabinete-atx-flujo",
    },
  },
];
