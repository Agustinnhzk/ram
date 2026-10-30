"use client";

import Link from "next/link";
import { useEffect, useRef, useState, type FormEvent } from "react";
import { Aviso, BotonPrincipal, CampoDeClave } from "@/components/formularios";
import { ErrorDeApi } from "@/lib/api-cliente";
import { elegirClaveNueva } from "@/lib/cuenta";

/**
 * El link del mail es /recuperar-clave/nueva#token. Lo que va después de "#" no viaja al servidor:
 * se lee acá, en el navegador, y se borra de la barra de direcciones (y del historial) enseguida.
 */
export function FormularioClaveNueva() {
  // undefined: todavía no se leyó la dirección. "": no vino token.
  const [token, setToken] = useState<string | undefined>(undefined);
  const [clave, setClave] = useState("");
  const [repetida, setRepetida] = useState("");
  const [enviando, setEnviando] = useState(false);
  const [listo, setListo] = useState(false);
  const [error, setError] = useState<string | null>(null);
  // En desarrollo React monta dos veces (StrictMode): la segunda vez la dirección ya no tiene el token.
  // Se guarda lo leído la primera vez.
  const leido = useRef<string | null>(null);

  useEffect(() => {
    if (leido.current === null) {
      leido.current = window.location.hash.slice(1);
      if (leido.current) history.replaceState(null, "", window.location.pathname);
    }
    // Leer la dirección es un "sistema externo" a React: se sincroniza una vez al montar.
    setToken(leido.current);
  }, []);

  async function enviar(evento: FormEvent) {
    evento.preventDefault();
    if (clave.length < 8) return setError("La contraseña tiene que tener al menos 8 caracteres.");
    if (clave !== repetida) return setError("Las dos contraseñas no coinciden.");
    setEnviando(true);
    setError(null);
    try {
      await elegirClaveNueva(token ?? "", clave);
      setListo(true);
    } catch (e) {
      setError(e instanceof ErrorDeApi ? e.message : "Algo salió mal. Probá de nuevo.");
    }
    setEnviando(false);
  }

  if (token === undefined) return <div aria-hidden className="h-48 animate-pulse rounded-xl bg-fondo/50" />;

  if (token === "") {
    return (
      <div className="space-y-4">
        <Aviso tipo="error">Este link no está completo. Abrilo directo desde el mail, o pedí uno nuevo.</Aviso>
        <Link href="/recuperar-clave" className="inline-block text-sm text-oro underline underline-offset-4">
          Pedir un link nuevo
        </Link>
      </div>
    );
  }

  if (listo) {
    return (
      <div className="space-y-4">
        <Aviso tipo="exito">¡Listo! Ya podés ingresar con tu contraseña nueva. Por seguridad, cerramos las sesiones abiertas.</Aviso>
        <Link href="/ingresar" className="boton-oro inline-block rounded-full px-6 py-3 font-semibold hover:brightness-110">
          Ingresar
        </Link>
      </div>
    );
  }

  return (
    <form onSubmit={enviar} className="space-y-4">
      {error && (
        <Aviso tipo="error">
          {error}{" "}
          {error.includes("link") && (
            <Link href="/recuperar-clave" className="text-oro underline underline-offset-4">
              Pedir otro
            </Link>
          )}
        </Aviso>
      )}
      <CampoDeClave etiqueta="Contraseña nueva" autoComplete="new-password" required ayuda="Al menos 8 caracteres." value={clave} onChange={(e) => setClave(e.target.value)} />
      <CampoDeClave etiqueta="Repetila" autoComplete="new-password" required value={repetida} onChange={(e) => setRepetida(e.target.value)} />
      <BotonPrincipal enviando={enviando} className="w-full">
        Guardar contraseña
      </BotonPrincipal>
    </form>
  );
}
