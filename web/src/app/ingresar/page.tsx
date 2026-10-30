import type { Metadata } from "next";
import { Suspense } from "react";
import { FormularioIngreso } from "@/components/cuenta/FormularioIngreso";
import { TarjetaDeCuenta } from "@/components/cuenta/TarjetaDeCuenta";

export const metadata: Metadata = { title: "Ingresá", robots: { index: false } };

export default function Pagina() {
  return (
    <TarjetaDeCuenta titulo="Ingresá" bajada="Para comprar y ver tus pedidos.">
      {/* Suspense: el formulario lee ?volver= de la dirección, y eso se sabe recién en el navegador. */}
      <Suspense>
        <FormularioIngreso />
      </Suspense>
    </TarjetaDeCuenta>
  );
}
