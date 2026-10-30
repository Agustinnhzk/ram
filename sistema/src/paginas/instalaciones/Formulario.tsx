import { useState, type FormEvent } from "react";
import { Link, useNavigate, useParams } from "react-router";
import { ESTADOS_DE_INSTALACION, useGuardarInstalacion, useInstalacion, type EstadoInstalacion, type Instalacion } from "../../api/instalaciones";
import { Campo, ESTILO_DE_CAMPO } from "../../componentes/Campo";
import { useAvisar } from "../../componentes/contextoDeAvisos";
import { ElegirCliente, type ClienteElegido } from "../../componentes/ElegirCliente";
import { IconoWhatsApp } from "../../componentes/IconoWhatsApp";
import { leerPrecio } from "../../utilidades/formato";
import { linkWhatsAppDelCliente } from "../../utilidades/pedidos";

/** /instalaciones/nueva y /instalaciones/:id — la misma pantalla para cargar y para editar. */
export function Formulario() {
  const { id } = useParams();
  const { data: instalacion, isPending, isError, error } = useInstalacion(id);
  if (id && isPending) return <div aria-hidden className="h-96 animate-pulse rounded-xl bg-superficie" />;
  if (id && isError) {
    return (
      <p role="alert" className="rounded-xl border border-peligro/60 bg-peligro/10 p-4">
        No se pudo cargar: {error.message}
      </p>
    );
  }
  return <Contenido key={id ?? "nueva"} instalacion={instalacion} />;
}

function Contenido({ instalacion }: { instalacion: Instalacion | undefined }) {
  const guardar = useGuardarInstalacion(instalacion?.id);
  const avisar = useAvisar();
  const navegar = useNavigate();
  const [cliente, setCliente] = useState<ClienteElegido | null>(null);
  const [direccion, setDireccion] = useState(instalacion?.direccion ?? "");
  const [estado, setEstado] = useState<EstadoInstalacion>(instalacion?.estado ?? "VISITA");
  const [equipos, setEquipos] = useState(instalacion?.equipos ?? "");
  const [camaras, setCamaras] = useState(instalacion?.camaras?.toString() ?? "");
  const [fecha, setFecha] = useState(instalacion?.fecha ?? "");
  const [tecnico, setTecnico] = useState(instalacion?.tecnico ?? "");
  const [presupuesto, setPresupuesto] = useState(instalacion?.presupuesto?.toString() ?? "");
  const [notas, setNotas] = useState(instalacion?.notas ?? "");

  function enviar(evento: FormEvent) {
    evento.preventDefault();
    if (!instalacion && !cliente) {
      avisar("Elegí o cargá el cliente", "error");
      return;
    }
    const monto = presupuesto.trim() ? leerPrecio(presupuesto) : null;
    if (presupuesto.trim() && monto === null) {
      avisar("El presupuesto tiene que ser un número de pesos", "error");
      return;
    }
    guardar.mutate(
      {
        ...(cliente ?? {}),
        direccion,
        estado,
        equipos: equipos.trim() || null,
        camaras: camaras.trim() ? Number(camaras) : null,
        fecha: fecha || null,
        tecnico: tecnico.trim() || null,
        presupuesto: monto,
        notas: notas.trim() || null,
      },
      {
        onSuccess: (nuevoId) => {
          avisar(instalacion ? "Cambios guardados" : "Instalación registrada");
          if (!instalacion) navegar(`/instalaciones/${nuevoId}`, { replace: true });
        },
      },
    );
  }

  return (
    <>
      <Link to="/instalaciones" className="text-sm text-suave hover:text-oro-claro">
        ← Instalaciones
      </Link>
      <h1 className="texto-metalico mt-2 text-4xl sm:text-5xl">
        {instalacion ? (
          <>
            Instalación <span className="numeros">{instalacion.numero}</span>
          </>
        ) : (
          "Nueva instalación"
        )}
      </h1>

      <form onSubmit={enviar} className="mt-8 max-w-3xl space-y-5">
        <section className="rounded-xl border border-borde bg-superficie p-5">
          <h2 className="text-xl">Cliente</h2>
          <div className="mt-3">
            {instalacion ? (
              <div className="flex flex-wrap items-center gap-3">
                <Link to={`/clientes/${instalacion.clienteId}`} className="font-semibold hover:text-oro-claro hover:underline">
                  {instalacion.cliente}
                </Link>
                {instalacion.telefono && (
                  <a
                    href={linkWhatsAppDelCliente(instalacion.telefono, `Hola ${instalacion.cliente.split(" ")[0]}, te escribimos de R.A.M. por la instalación.`)}
                    target="_blank"
                    rel="noreferrer"
                    className="inline-flex items-center gap-2 rounded-full border border-borde px-3 py-1 text-sm hover:border-oro"
                  >
                    <IconoWhatsApp className="size-4 text-exito" /> Escribirle
                  </a>
                )}
              </div>
            ) : (
              <ElegirCliente alCambiar={setCliente} />
            )}
          </div>
        </section>

        <div className="grid gap-4 sm:grid-cols-2">
          <Campo id="estado" etiqueta="Estado">
            <select id="estado" value={estado} onChange={(e) => setEstado(e.target.value as EstadoInstalacion)} className={ESTILO_DE_CAMPO}>
              {(Object.keys(ESTADOS_DE_INSTALACION) as EstadoInstalacion[]).map((e) => (
                <option key={e} value={e}>
                  {ESTADOS_DE_INSTALACION[e]}
                </option>
              ))}
            </select>
          </Campo>
          <Campo id="fecha" etiqueta="Fecha agendada" opcional>
            <input id="fecha" type="date" value={fecha} onChange={(e) => setFecha(e.target.value)} className={ESTILO_DE_CAMPO} />
          </Campo>
        </div>
        <Campo id="direccion" etiqueta="Dirección">
          <input id="direccion" required maxLength={300} value={direccion} onChange={(e) => setDireccion(e.target.value)} className={ESTILO_DE_CAMPO} />
        </Campo>
        <Campo id="equipos" etiqueta="Equipos" opcional ayuda="Qué se instala: DVR, cámaras, disco, alarma, sensores…">
          <textarea id="equipos" rows={3} maxLength={1000} value={equipos} onChange={(e) => setEquipos(e.target.value)} className={ESTILO_DE_CAMPO} />
        </Campo>
        <div className="grid gap-4 sm:grid-cols-3">
          <Campo id="camaras" etiqueta="Cámaras" opcional>
            <input id="camaras" type="number" min={0} max={500} value={camaras} onChange={(e) => setCamaras(e.target.value)} className={ESTILO_DE_CAMPO} />
          </Campo>
          <Campo id="tecnico" etiqueta="Técnico" opcional>
            <input id="tecnico" maxLength={120} value={tecnico} onChange={(e) => setTecnico(e.target.value)} className={ESTILO_DE_CAMPO} />
          </Campo>
          <Campo id="presupuesto" etiqueta="Presupuesto" opcional>
            <input id="presupuesto" inputMode="numeric" value={presupuesto} onChange={(e) => setPresupuesto(e.target.value)} placeholder="$" className={ESTILO_DE_CAMPO} />
          </Campo>
        </div>
        <Campo id="notas" etiqueta="Notas" opcional ayuda="Costo de la visita, horarios, lo que haga falta recordar.">
          <textarea id="notas" rows={3} maxLength={2000} value={notas} onChange={(e) => setNotas(e.target.value)} className={ESTILO_DE_CAMPO} />
        </Campo>
        {guardar.isError && (
          <p role="alert" className="text-sm text-peligro">
            {guardar.error.message}
          </p>
        )}
        <button type="submit" disabled={guardar.isPending} className="boton-oro rounded-full px-6 py-2.5 font-semibold disabled:opacity-50">
          {instalacion ? "Guardar cambios" : "Registrar instalación"}
        </button>
      </form>
    </>
  );
}
