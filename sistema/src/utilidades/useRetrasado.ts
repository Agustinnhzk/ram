import { useEffect, useState } from "react";

/**
 * Devuelve el valor recién cuando dejó de cambiar por un rato. Para los buscadores:
 * si escribís "perez" no se hacen 5 pedidos (p, pe, per...), sino uno solo cuando parás.
 */
export function useRetrasado<T>(valor: T, milisegundos = 300): T {
  const [retrasado, setRetrasado] = useState(valor);
  useEffect(() => {
    const espera = setTimeout(() => setRetrasado(valor), milisegundos);
    return () => clearTimeout(espera);
  }, [valor, milisegundos]);
  return retrasado;
}
