"use client";

import Link from "next/link";
import { useEffect, useState } from "react";
import { Aviso } from "@/components/formularios";
import { ErrorDeApi } from "@/lib/api-cliente";
import { sesionVencida, useSesion } from "@/lib/cuenta";
import { formatearPrecio } from "@/lib/formato";
import { misPedidos, type ResumenDePedido } from "@/lib/pedidos";
import { EtiquetaDeEstado } from "./EtiquetaDeEstado";
import { MisReparaciones } from "./MisReparaciones";
import { PedirIngreso } from "./PedirIngreso";

const fecha = new Intl.DateTimeFormat("es-AR", { dateStyle: "medium", timeZone: "America/Argentina/Buenos_Aires" });

export function MisPedidos() {
  const sesion = useSesion();
  if (sesion === undefined) return <Cargando />;
  if (sesion === null) return <PedirIngreso volver="/mis-pedidos" mensaje="Ingresá para ver tus pedidos" />;
  return (
    <>
      <Listado />
      <MisReparaciones />
    </>
  );
}

function Listado() {
  const [pedidos, setPedidos] = useState<ResumenDePedido[] | null>(null);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    misPedidos()
      .then(setPedidos)
      .catch((e: unknown) => {
        if (e instanceof ErrorDeApi && e.estado === 401) sesionVencida();
        setError(e instanceof Error ? e.message : "No pudimos traer tus pedidos.");
      });
  }, []);

  if (error) return <div className="mt-10"><Aviso tipo="error">{error}</Aviso></div>;
  if (!pedidos) return <Cargando />;
  if (pedidos.length === 0) {
    return (
      <div className="mt-10 rounded-xl border border-borde bg-superficie px-6 py-14 text-center">
        <p className="font-display text-2xl font-bold text-principal italic">Todavía no hiciste pedidos</p>
        <Link href="/productos" className="boton-oro mt-6 inline-block rounded-full px-6 py-2.5 font-semibold">
          Ver productos
        </Link>
      </div>
    );
  }

  return (
    <ul className="mt-10 divide-y divide-borde rounded-xl border border-borde bg-superficie">
      {pedidos.map((p) => (
        <li key={p.id}>
          <Link href={`/mis-pedidos/${p.id}`} className="flex flex-wrap items-center gap-x-6 gap-y-2 px-5 py-4 hover:bg-fondo/40">
            <span className="font-display text-xl font-bold text-principal">N° {p.numero}</span>
            <span className="text-sm text-suave">{fecha.format(new Date(p.creadoEn))}</span>
            <EtiquetaDeEstado estado={p.estado} />
            <span className="ml-auto font-display text-lg font-bold text-principal">{formatearPrecio(p.total)}</span>
            <span aria-hidden className="text-oro">
              →
            </span>
          </Link>
        </li>
      ))}
    </ul>
  );
}

function Cargando() {
  return <div aria-hidden className="mt-10 h-48 animate-pulse rounded-xl bg-superficie" />;
}
