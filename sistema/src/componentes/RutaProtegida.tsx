import { Navigate, Outlet, useLocation } from "react-router";
import { useSesion } from "../api/sesion";
import { PantallaDeCarga } from "./PantallaDeCarga";

/**
 * Envuelve todas las pantallas que necesitan login. Sin sesión, manda a /ingresar
 * y recuerda a dónde quería ir la persona, para volver ahí después de entrar.
 *
 * Ojo: esto es comodidad, NO seguridad. La seguridad está en el backend: aunque alguien
 * saltee esta pantalla, /api/sistema responde 401 sin una sesión válida (ADR-006).
 */
export function RutaProtegida() {
  const { data: sesion, isPending, isError } = useSesion();
  const ubicacion = useLocation();

  if (isPending) return <PantallaDeCarga />;
  if (isError) return <PantallaDeCarga error />;
  if (!sesion) return <Navigate to="/ingresar" replace state={{ desde: ubicacion.pathname }} />;
  return <Outlet />;
}
