"use client";

import Link from "next/link";
import { useState, type FormEvent } from "react";
import { Aviso, BotonPrincipal, Campo } from "@/components/formularios";
import { ErrorDeApi } from "@/lib/api-cliente";
import { pedirLinkDeRecuperacion } from "@/lib/cuenta";

export function FormularioRecuperar() {
  const [correo, setCorreo] = useState("");
  const [enviando, setEnviando] = useState(false);
  const [enviado, setEnviado] = useState(false);
  const [error, setError] = useState<string | null>(null);

  async function enviar(evento: FormEvent) {
    evento.preventDefault();
    setEnviando(true);
    setError(null);
    try {
      await pedirLinkDeRecuperacion(correo);
      setEnviado(true);
    } catch (e) {
      setError(e instanceof ErrorDeApi ? e.message : "Algo salió mal. Probá de nuevo.");
    }
    setEnviando(false);
  }

  if (enviado) {
    return (
      <div className="space-y-4">
        {/* El mismo mensaje exista o no la cuenta: así nadie averigua quién tiene cuenta (ADR-012). */}
        <Aviso tipo="exito">
          Si hay una cuenta con <strong>{correo}</strong>, te mandamos un mail con un link para elegir una contraseña nueva.
          Vale por 1 hora.
        </Aviso>
        <p className="text-sm text-suave">¿No te llegó? Revisá la carpeta de spam o correo no deseado.</p>
        <Link href="/ingresar" className="inline-block text-sm text-oro underline underline-offset-4 hover:text-oro-claro">
          Volver a ingresar
        </Link>
      </div>
    );
  }

  return (
    <form onSubmit={enviar} className="space-y-4">
      {error && <Aviso tipo="error">{error}</Aviso>}
      <Campo etiqueta="Correo de tu cuenta" type="email" autoComplete="email" required value={correo} onChange={(e) => setCorreo(e.target.value)} />
      <BotonPrincipal enviando={enviando} className="w-full">
        Mandame el link
      </BotonPrincipal>
      <Link href="/ingresar" className="block text-center text-sm text-suave hover:text-oro-claro">
        ← Volver
      </Link>
    </form>
  );
}
