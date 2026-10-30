import type { Metadata } from "next";
import { Suspense } from "react";
import { FormularioRegistro } from "@/components/cuenta/FormularioRegistro";
import { TarjetaDeCuenta } from "@/components/cuenta/TarjetaDeCuenta";

export const metadata: Metadata = { title: "Registrate", robots: { index: false } };

export default function Pagina() {
  return (
    <TarjetaDeCuenta titulo="Creá tu cuenta" bajada="Con tu cuenta comprás, pagás por transferencia y seguís tus pedidos.">
      <Suspense>
        <FormularioRegistro />
      </Suspense>
    </TarjetaDeCuenta>
  );
}
