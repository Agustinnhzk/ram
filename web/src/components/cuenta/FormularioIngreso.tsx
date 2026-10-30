"use client";

import Link from "next/link";
import { useRouter, useSearchParams } from "next/navigation";
import { useState, type FormEvent } from "react";
import { Aviso, BotonPrincipal, Campo, CampoDeClave } from "@/components/formularios";
import { ErrorDeApi } from "@/lib/api-cliente";
import { destinoSeguro, ingresar } from "@/lib/cuenta";

export function FormularioIngreso() {
  const router = useRouter();
  const volver = useSearchParams().get("volver");
  const [correo, setCorreo] = useState("");
  const [clave, setClave] = useState("");
  const [enviando, setEnviando] = useState(false);
  const [error, setError] = useState<string | null>(null);

  async function enviar(evento: FormEvent) {
    evento.preventDefault();
    setEnviando(true);
    setError(null);
    try {
      await ingresar(correo, clave);
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
      <Campo etiqueta="Correo" type="email" autoComplete="email" required value={correo} onChange={(e) => setCorreo(e.target.value)} />
      <CampoDeClave etiqueta="Contraseña" autoComplete="current-password" required value={clave} onChange={(e) => setClave(e.target.value)} />
      <div className="text-right">
        <Link href="/recuperar-clave" className="text-sm text-oro underline underline-offset-4 hover:text-oro-claro">
          ¿Te olvidaste la contraseña?
        </Link>
      </div>
      <BotonPrincipal enviando={enviando} className="w-full">
        Ingresar
      </BotonPrincipal>
      <p className="border-t border-borde pt-4 text-center text-sm text-suave">
        ¿No tenés cuenta?{" "}
        <Link href={`/registro${conVolver}`} className="text-oro underline underline-offset-4 hover:text-oro-claro">
          Registrate
        </Link>
      </p>
    </form>
  );
}
