import type { Metadata } from "next";
import { FormularioRecuperar } from "@/components/cuenta/FormularioRecuperar";
import { TarjetaDeCuenta } from "@/components/cuenta/TarjetaDeCuenta";

export const metadata: Metadata = { title: "Recuperar contraseña", robots: { index: false } };

export default function Pagina() {
  return (
    <TarjetaDeCuenta titulo="¿Te olvidaste la contraseña?" bajada="Escribí tu correo y te mandamos un link para elegir una nueva.">
      <FormularioRecuperar />
    </TarjetaDeCuenta>
  );
}
