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
};

export type Categoria = "camaras" | "alarmas" | "notebooks" | "pc" | "componentes";

export type Condicion = "nuevo" | "reacondicionado";

export type Producto = {
  id: string;
  slug: string;
  nombre: string;
  categoria: Categoria;
  condicion: Condicion;
  /** null = "Consultar precio" (se consulta por WhatsApp hasta que haya ventas online) */
  precio: number | null;
  imagen: string;
  resumen: string;
  garantiaMeses: number | null;
};

/** Una imagen del carrusel de la home. Se cargan desde el sistema de gestión. */
export type Promocion = {
  id: string;
  imagen: string;
  textoAlternativo: string;
  enlace: string;
};
