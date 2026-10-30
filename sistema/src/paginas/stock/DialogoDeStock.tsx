import { useState, type FormEvent } from "react";
import { useCambiarStock, useMovimientos, type CambioDeStock, type FilaDeStock } from "../../api/stock";
import { Campo, ESTILO_DE_CAMPO } from "../../componentes/Campo";
import { useAvisar } from "../../componentes/contextoDeAvisos";
import { Dialogo } from "../../componentes/Dialogo";
import { formatearFecha } from "../../utilidades/formato";
import { leerCantidad, NOMBRE_DEL_MOVIMIENTO } from "../../utilidades/stock";

type Pestana = "ingreso" | "ajuste" | "historial";

/** El stock de un producto: cargar mercadería, ajustar por conteo, ver el historial, o empezar/dejar de controlar. */
export function DialogoDeStock(props: { fila: FilaDeStock | null; manejaStock: boolean; esDueno: boolean; alCerrar: () => void }) {
  const { fila } = props;
  return (
    <Dialogo abierto={fila !== null} alCerrar={props.alCerrar} titulo={fila?.nombre ?? ""} ancho="max-w-xl">
      {/* key: al abrir otro producto, los formularios arrancan vacíos. */}
      {fila &&
        (fila.cantidad === null ? (
          <EmpezarAControlar key={fila.id} fila={fila} manejaStock={props.manejaStock} />
        ) : (
          <Controlado key={fila.id} fila={fila} manejaStock={props.manejaStock} esDueno={props.esDueno} />
        ))}
    </Dialogo>
  );
}

function EmpezarAControlar({ fila, manejaStock }: { fila: FilaDeStock; manejaStock: boolean }) {
  const [cantidad, setCantidad] = useState("");
  const { enviar, cambiar } = useEnviar();
  const valor = leerCantidad(cantidad);

  return (
    <div className="space-y-4">
      <p className="text-suave">
        Este producto <strong className="text-principal">no tiene control de stock</strong>: se vende sin límite y la tienda no dice si hay.
      </p>
      {manejaStock ? (
        <form
          onSubmit={(e) => {
            e.preventDefault();
            if (valor !== null) enviar({ tipo: "controlar", producto: fila.id, cantidad: valor }, "Ahora se controla el stock");
          }}
          className="space-y-4"
        >
          <Campo id="stock-inicial" etiqueta="¿Cuántas hay hoy?" ayuda="Desde ahora, cada venta confirmada se descuenta. Con 0, la tienda lo muestra sin stock y se puede reservar.">
            <input id="stock-inicial" inputMode="numeric" required value={cantidad} onChange={(e) => setCantidad(e.target.value)} className={ESTILO_DE_CAMPO} />
          </Campo>
          <ErrorDelCambio cambiar={cambiar} />
          <div className="flex justify-end">
            <button type="submit" disabled={valor === null || cambiar.isPending} className="boton-oro rounded-full px-5 py-2 text-sm font-semibold disabled:opacity-50">
              Empezar a controlar
            </button>
          </div>
        </form>
      ) : (
        <p className="text-sm text-suave">El control de stock lo activan el Dueño y Mostrador.</p>
      )}
    </div>
  );
}

function Controlado({ fila, manejaStock, esDueno }: { fila: FilaDeStock; manejaStock: boolean; esDueno: boolean }) {
  const [pestana, setPestana] = useState<Pestana>(manejaStock ? "ingreso" : "historial");
  const { enviar, cambiar } = useEnviar();
  const pestanas: { valor: Pestana; texto: string }[] = manejaStock
    ? [
        { valor: "ingreso", texto: "Llegó mercadería" },
        { valor: "ajuste", texto: "Ajustar" },
        { valor: "historial", texto: "Historial" },
      ]
    : [{ valor: "historial", texto: "Historial" }];

  return (
    <div className="space-y-5">
      <div className="flex flex-wrap items-end justify-between gap-3">
        <p>
          <span className="numeros text-5xl font-semibold text-principal">{fila.cantidad}</span>
          <span className="ml-2 text-suave">en stock</span>
        </p>
        {fila.reservado > 0 && (
          <p className="rounded-full border border-aviso/60 px-3 py-1 text-sm text-aviso">
            <span className="numeros">{fila.reservado}</span> {fila.reservado === 1 ? "reservada" : "reservadas"} esperando mercadería
          </p>
        )}
      </div>

      <div className="flex gap-1 border-b border-borde" role="tablist">
        {pestanas.map((p) => (
          <button
            key={p.valor}
            type="button"
            role="tab"
            aria-selected={pestana === p.valor}
            onClick={() => setPestana(p.valor)}
            className={`-mb-px border-b-2 px-3 py-2 text-sm ${pestana === p.valor ? "border-oro text-oro-claro" : "border-transparent text-suave hover:text-principal"}`}
          >
            {p.texto}
          </button>
        ))}
      </div>

      {pestana === "ingreso" && (
        <FormularioDeCantidad
          id="ingreso"
          etiqueta="¿Cuántas llegaron?"
          nota={{ etiqueta: "Nota", opcional: true, ayuda: "Proveedor, número de factura…" }}
          boton="Sumar al stock"
          ocupado={cambiar.isPending}
          alEnviar={(cantidad, nota, limpiar) => enviar({ tipo: "ingresar", producto: fila.id, cantidad, nota }, `Se sumaron ${cantidad}`, limpiar)}
          minimo={1}
          aviso={fila.reservado > 0 ? "Hay reservas esperando: después de cargar, separalas en Stock → Reservas." : undefined}
        />
      )}
      {pestana === "ajuste" && (
        <FormularioDeCantidad
          id="ajuste"
          etiqueta="¿Cuántas hay en realidad?"
          nota={{ etiqueta: "Motivo", opcional: false, ayuda: "Se rompió una, error de carga, conteo de fin de mes…" }}
          boton="Guardar el conteo"
          ocupado={cambiar.isPending}
          alEnviar={(cantidad, motivo, limpiar) => enviar({ tipo: "ajustar", producto: fila.id, cantidad, motivo }, "Stock ajustado", limpiar)}
          minimo={0}
        />
      )}
      {pestana === "historial" && <Historial producto={fila.id} />}
      <ErrorDelCambio cambiar={cambiar} />

      {esDueno && (
        <div className="border-t border-borde pt-4 text-sm">
          <button
            type="button"
            disabled={cambiar.isPending}
            onClick={() => enviar({ tipo: "dejar", producto: fila.id }, "Ya no se controla el stock de este producto")}
            className="text-peligro underline underline-offset-4 disabled:opacity-50"
          >
            Dejar de controlar el stock
          </button>
          <p className="mt-1 text-xs text-suave">Vuelve a venderse sin límite. El historial queda guardado.</p>
        </div>
      )}
    </div>
  );
}

function FormularioDeCantidad(props: {
  id: string;
  etiqueta: string;
  nota: { etiqueta: string; opcional: boolean; ayuda: string };
  boton: string;
  ocupado: boolean;
  minimo: number;
  aviso?: string;
  alEnviar: (cantidad: number, nota: string, limpiar: () => void) => void;
}) {
  const [cantidad, setCantidad] = useState("");
  const [nota, setNota] = useState("");
  const valor = leerCantidad(cantidad);
  const valido = valor !== null && valor >= props.minimo && (props.nota.opcional || nota.trim() !== "");

  function enviar(evento: FormEvent) {
    evento.preventDefault();
    if (valido && valor !== null)
      props.alEnviar(valor, nota.trim(), () => {
        setCantidad("");
        setNota("");
      });
  }

  return (
    <form onSubmit={enviar} className="space-y-4">
      <Campo id={`${props.id}-cantidad`} etiqueta={props.etiqueta}>
        <input
          id={`${props.id}-cantidad`}
          inputMode="numeric"
          required
          value={cantidad}
          onChange={(e) => setCantidad(e.target.value)}
          className={ESTILO_DE_CAMPO}
        />
      </Campo>
      <Campo id={`${props.id}-nota`} etiqueta={props.nota.etiqueta} opcional={props.nota.opcional} ayuda={props.nota.ayuda}>
        <input
          id={`${props.id}-nota`}
          maxLength={300}
          required={!props.nota.opcional}
          value={nota}
          onChange={(e) => setNota(e.target.value)}
          className={ESTILO_DE_CAMPO}
        />
      </Campo>
      {props.aviso && <p className="text-sm text-aviso">{props.aviso}</p>}
      <div className="flex justify-end">
        <button type="submit" disabled={!valido || props.ocupado} className="boton-oro rounded-full px-5 py-2 text-sm font-semibold disabled:opacity-50">
          {props.boton}
        </button>
      </div>
    </form>
  );
}

function Historial({ producto }: { producto: string }) {
  const { data: movimientos, isPending } = useMovimientos(producto);
  if (isPending) return <div aria-hidden className="h-40 animate-pulse rounded-xl bg-superficie-alta" />;
  if (!movimientos || movimientos.length === 0) return <p className="text-sm text-suave">Sin movimientos.</p>;
  return (
    <ul className="divide-y divide-borde rounded-xl border border-borde text-sm">
      {movimientos.map((m, i) => (
        <li key={i} className="flex items-start justify-between gap-3 px-4 py-2.5">
          <span className="min-w-0">
            <span className="block text-principal">
              {NOMBRE_DEL_MOVIMIENTO[m.tipo]}
              {m.numeroPedido !== null && <span className="numeros text-suave"> · pedido {m.numeroPedido}</span>}
            </span>
            <span className="block text-xs text-suave">
              <span className="numeros">{formatearFecha(m.fecha)}</span>
              {m.usuario && ` · ${m.usuario}`}
              {m.nota && ` · ${m.nota}`}
            </span>
          </span>
          <span className="numeros shrink-0 text-right">
            <span className={`block font-semibold ${m.cantidad > 0 ? "text-exito" : m.cantidad < 0 ? "text-peligro" : "text-suave"}`}>
              {m.cantidad > 0 ? `+${m.cantidad}` : m.cantidad}
            </span>
            <span className="text-xs text-suave">quedan {m.saldo}</span>
          </span>
        </li>
      ))}
    </ul>
  );
}

function useEnviar() {
  const cambiar = useCambiarStock();
  const avisar = useAvisar();
  function enviar(cambio: CambioDeStock, exito: string, despues?: () => void) {
    cambiar.mutate(cambio, {
      onSuccess: () => {
        avisar(exito);
        despues?.();
      },
    });
  }
  return { enviar, cambiar };
}

function ErrorDelCambio({ cambiar }: { cambiar: ReturnType<typeof useCambiarStock> }) {
  if (!cambiar.isError) return null;
  return (
    <p role="alert" className="text-sm text-peligro">
      {cambiar.error.message}
    </p>
  );
}
