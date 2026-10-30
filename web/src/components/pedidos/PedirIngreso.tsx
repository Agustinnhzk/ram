import Link from "next/link";

/** Para las pantallas que necesitan cuenta: invita a ingresar y vuelve acá después. */
export function PedirIngreso({ volver, mensaje }: { volver: string; mensaje: string }) {
  const destino = encodeURIComponent(volver);
  return (
    <div className="mt-10 max-w-lg rounded-2xl border border-borde bg-superficie p-6 sm:p-8">
      <p className="font-display text-2xl font-bold text-principal italic">{mensaje}</p>
      <p className="mt-2 text-suave">Es un minuto: con tu cuenta seguís el pedido y subís el comprobante de pago.</p>
      <div className="mt-6 flex flex-wrap gap-3">
        <Link href={`/ingresar?volver=${destino}`} className="boton-oro rounded-full px-6 py-2.5 font-semibold hover:brightness-110">
          Ingresar
        </Link>
        <Link href={`/registro?volver=${destino}`} className="rounded-full border border-borde px-6 py-2.5 text-principal hover:border-oro">
          Crear cuenta
        </Link>
      </div>
    </div>
  );
}
