/**
 * Datos estructurados (JSON-LD, schema.org): le explican a Google qué es la página
 * (un negocio local, un producto) además de lo que dice el texto.
 *
 * Al pasarlo a texto se reemplaza "<" por <: si algún dato trae "</script>" (el nombre de un
 * producto, por ejemplo), no puede cerrar la etiqueta y meter código. Es lo que indica la guía de Next.js.
 *
 * Probarlo: https://search.google.com/test/rich-results
 */
export function DatosEstructurados({ datos }: { datos: Record<string, unknown> }) {
  return (
    <script
      type="application/ld+json"
      dangerouslySetInnerHTML={{ __html: JSON.stringify(datos).replace(/</g, "\\u003c") }}
    />
  );
}
