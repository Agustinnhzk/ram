// Espera a que el backend responda antes de compilar la tienda (solo se usa en producción, ver Dockerfile).
// Pregunta por las categorías del taller: si contesta, el backend ya arrancó Y reconoce el dominio de la tienda.
const backend = process.env.BACKEND_URL ?? "http://localhost:8080";
const dominio = process.env.TALLER_DOMINIO ?? "ram.localhost";
const espera = (ms) => new Promise((listo) => setTimeout(listo, ms));

for (let intento = 1; ; intento++) {
  try {
    const respuesta = await fetch(`${backend}/api/publico/categorias`, { headers: { "X-Forwarded-Host": dominio } });
    if (respuesta.ok) break;
    if (respuesta.status === 404 && intento % 10 === 0) {
      console.log(`El backend responde pero no reconoce "${dominio}". ¿Cargaste el dominio del taller? (DEPLOY.md, paso 7)`);
    }
  } catch {
    // Todavía arrancando.
  }
  if (intento % 10 === 1) console.log("Esperando al backend...");
  await espera(3000);
}
console.log("Backend listo. Compilando la tienda...");
