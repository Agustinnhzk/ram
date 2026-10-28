/**
 * Un <script> que corre una sola vez, cuando el navegador lee el HTML que manda el servidor.
 *
 * React avisa cuando un componente renderiza un <script>, porque en el navegador nunca se ejecuta.
 * Para no ver ese aviso: en el servidor sale como "text/javascript" (se ejecuta al leer el HTML)
 * y en el navegador como "text/plain" (React lo trata como texto). suppressHydrationWarning acepta
 * esa diferencia, que es a propósito. Es la solución de la guía oficial de Next.js
 * ("Preventing flash before hydration").
 */
export function ScriptEnLinea({ codigo }: { codigo: string }) {
  return (
    <script
      type={typeof window === "undefined" ? "text/javascript" : "text/plain"}
      suppressHydrationWarning
      dangerouslySetInnerHTML={{ __html: codigo }}
    />
  );
}
