import type { Metadata } from "next";
import { FormularioClaveNueva } from "@/components/cuenta/FormularioClaveNueva";
import { TarjetaDeCuenta } from "@/components/cuenta/TarjetaDeCuenta";

export const metadata: Metadata = {
  title: "Elegí una contraseña nueva",
  robots: { index: false },
  // Por si la página carga algo de otro sitio: que no le cuente de dónde viene.
  referrer: "no-referrer",
};

export default function Pagina() {
  return (
    <TarjetaDeCuenta titulo="Contraseña nueva">
      <FormularioClaveNueva />
    </TarjetaDeCuenta>
  );
}
