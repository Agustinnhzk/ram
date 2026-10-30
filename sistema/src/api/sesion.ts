import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { ErrorDeApi, pedir } from "./cliente";

export type Rol = "DUENO" | "TECNICO" | "MOSTRADOR";

/** Lo que devuelve GET /api/sistema/sesion (SesionResponse en el backend). */
export type Sesion = {
  id: string;
  nombre: string;
  tipo: "PERSONAL" | "CLIENTE";
  rol: Rol;
};

const CLAVE = ["sesion"] as const;

/**
 * ¿Quién está logueado? null = nadie (el backend respondió 401).
 * TanStack Query guarda la respuesta: todas las pantallas que la piden comparten un solo pedido.
 */
export function useSesion() {
  return useQuery({
    queryKey: CLAVE,
    queryFn: async () => {
      try {
        return await pedir<Sesion>("GET", "/api/sistema/sesion");
      } catch (e) {
        if (e instanceof ErrorDeApi && e.sinSesion) return null;
        throw e;
      }
    },
    retry: false,
    staleTime: 5 * 60_000,
  });
}

export function useIniciarSesion() {
  const consultas = useQueryClient();
  return useMutation({
    mutationFn: (credenciales: { correo: string; clave: string }) =>
      pedir<Sesion>("POST", "/api/sistema/sesion", credenciales),
    // El backend ya devuelve quién es: se guarda directo, sin volver a preguntar.
    onSuccess: (sesion) => consultas.setQueryData(CLAVE, sesion),
  });
}

export function useCerrarSesion() {
  const consultas = useQueryClient();
  return useMutation({
    mutationFn: () => pedir<void>("DELETE", "/api/sistema/sesion"),
    // Se borra TODO lo guardado: lo que vio una persona no tiene que quedar para la siguiente.
    onSettled: () => {
      consultas.clear();
      consultas.setQueryData(CLAVE, null);
    },
  });
}

export const NOMBRE_DEL_ROL: Record<Rol, string> = {
  DUENO: "Dueño",
  TECNICO: "Técnico",
  MOSTRADOR: "Mostrador",
};
