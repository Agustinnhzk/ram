import { useState, type FormEvent } from "react";
import { Navigate, useLocation, useNavigate } from "react-router";
import { ErrorDeApi } from "../api/cliente";
import { useIniciarSesion, useSesion } from "../api/sesion";
import { BotonTema } from "../componentes/BotonTema";

/** /ingresar — login del personal. Si ya hay sesión, va directo al sistema. */
export function Ingresar() {
  const { data: sesion } = useSesion();
  const iniciar = useIniciarSesion();
  const navegar = useNavigate();
  const ubicacion = useLocation();
  const [correo, setCorreo] = useState("");
  const [clave, setClave] = useState("");

  // A dónde volver después de entrar: la pantalla que se quiso abrir sin sesión (o el inicio).
  const destino = (ubicacion.state as { desde?: string } | null)?.desde ?? "/";

  if (sesion) return <Navigate to={destino} replace />;

  function enviar(evento: FormEvent) {
    evento.preventDefault();
    iniciar.mutate({ correo, clave }, { onSuccess: () => navegar(destino, { replace: true }) });
  }

  const error = iniciar.error;
  const mensaje =
    error instanceof ErrorDeApi && error.codigo === "credenciales_invalidas"
      ? "El correo o la contraseña no son correctos."
      : error?.message;

  return (
    <main className="grid min-h-dvh place-items-center px-4 py-10">
      <div className="absolute top-4 right-4">
        <BotonTema />
      </div>
      <div className="w-full max-w-sm">
        <div className="text-center">
          <img src="/marca/logo.png" alt="" className="mx-auto size-20" />
          <h1 className="texto-metalico mt-4 text-4xl">Sistema de gestión</h1>
          <p className="mt-2 text-suave">Ingresá con tu cuenta del taller.</p>
        </div>

        <form onSubmit={enviar} className="mt-8 space-y-4 rounded-2xl border border-borde bg-superficie p-6" noValidate>
          <div>
            <label htmlFor="correo" className="text-sm text-suave">
              Correo
            </label>
            <input
              id="correo"
              type="email"
              autoComplete="username"
              required
              value={correo}
              onChange={(e) => setCorreo(e.target.value)}
              className="mt-1 w-full rounded-lg border border-borde bg-fondo px-3 py-2.5 text-principal focus:border-oro focus:outline-none"
            />
          </div>
          <div>
            <label htmlFor="clave" className="text-sm text-suave">
              Contraseña
            </label>
            <input
              id="clave"
              type="password"
              autoComplete="current-password"
              required
              value={clave}
              onChange={(e) => setClave(e.target.value)}
              className="mt-1 w-full rounded-lg border border-borde bg-fondo px-3 py-2.5 text-principal focus:border-oro focus:outline-none"
            />
          </div>

          {mensaje && (
            <p role="alert" className="rounded-lg border border-peligro/60 bg-peligro/10 px-3 py-2 text-sm">
              {mensaje}
            </p>
          )}

          <button
            type="submit"
            disabled={iniciar.isPending || !correo || !clave}
            className="boton-oro w-full rounded-full py-2.5 font-semibold hover:brightness-110 disabled:opacity-50"
          >
            {iniciar.isPending ? "Ingresando…" : "Ingresar"}
          </button>
        </form>
      </div>
    </main>
  );
}
