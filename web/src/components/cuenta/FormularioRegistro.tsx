"use client";

import Link from "next/link";
import { useRouter, useSearchParams } from "next/navigation";
import { useState, type FormEvent } from "react";
import { Aviso, BotonPrincipal, Campo, CampoDeClave } from "@/components/formularios";
import { ErrorDeApi } from "@/lib/api-cliente";
import { destinoSeguro, registrarse } from "@/lib/cuenta";

export function FormularioRegistro() {
  const router = useRouter();
  const volver = useSearchParams().get("volver");
  const [nombre, setNombre] = useState("");
  const [correo, setCorreo] = useState("");
  const [clave, setClave] = useState("");
  const [enviando, setEnviando] = useState(false);
  const [error, setError] = useState<string | null>(null);

  async function enviar(evento: FormEvent) {
    evento.preventDefault();
    if (clave.length < 8) {
      setError("La contraseña tiene que tener al menos 8 caracteres.");
      return;
    }
    setEnviando(true);
    setError(null);
    try {
      await registrarse(nombre, correo, clave);
      router.push(destinoSeguro(volver));
    } catch (e) {
      setError(e instanceof ErrorDeApi ? e.message : "Algo salió mal. Probá de nuevo.");
      setEnviando(false);
    }
  }

  const conVolver = volver ? `?volver=${encodeURIComponent(volver)}` : "";

  return (
    <form onSubmit={enviar} className="space-y-4">
      {error && <Aviso tipo="error">{error}</Aviso>}
      <Campo etiqueta="Nombre y apellido" autoComplete="name" required maxLength={120} value={nombre} onChange={(e) => setNombre(e.target.value)} />
      <Campo etiqueta="Correo" type="email" autoComplete="email" required value={correo} onChange={(e) => setCorreo(e.target.value)} />
      <CampoDeClave
        etiqueta="Contraseña"
        autoComplete="new-password"
        required
        minLength={8}
        ayuda="Al menos 8 caracteres."
        value={clave}
        onChange={(e) => setClave(e.target.value)}
      />
      <BotonPrincipal enviando={enviando} className="w-full">
        Crear cuenta
      </BotonPrincipal>
      <p className="border-t border-borde pt-4 text-center text-sm text-suave">
        ¿Ya tenés cuenta?{" "}
        <Link href={`/ingresar${conVolver}`} className="text-oro underline underline-offset-4 hover:text-oro-claro">
          Ingresá
        </Link>
      </p>
    </form>
  );
}
