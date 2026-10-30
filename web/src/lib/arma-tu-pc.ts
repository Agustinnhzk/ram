import type { Componente, ModeloDePc, TipoComponente } from "@/lib/api";
import { primero, type ParametrosDeUrl } from "@/lib/catalogo";

/**
 * Las reglas de Armá tu PC, sin nada de pantalla: qué piezas hay, cuáles son compatibles,
 * cómo se guarda un armado en la URL y cuánto suma.
 *
 * Al no tener pantalla, se puede probar con tests sin abrir el navegador. Y cuando exista
 * el carrito en el backend, el servidor va a tener que revisar lo mismo antes de cobrar.
 */

/** Los pasos, en orden. El orden importa: cada pieza se revisa contra las que se eligieron ANTES. */
export const PASOS: { tipo: TipoComponente; nombre: string; titulo: string; ayuda: string }[] = [
  {
    tipo: "cpu",
    nombre: "Procesador",
    titulo: "Elegí el procesador",
    ayuda: "Es el corazón de la PC: define qué mother podés usar.",
  },
  {
    tipo: "mother",
    nombre: "Mother",
    titulo: "Elegí la mother",
    ayuda: "Solo te mostramos las que tienen el mismo socket que tu procesador.",
  },
  {
    tipo: "ram",
    nombre: "Memoria RAM",
    titulo: "Elegí la memoria RAM",
    ayuda: "Solo te mostramos las del tipo que acepta tu mother.",
  },
  {
    tipo: "gpu",
    nombre: "Placa de video",
    titulo: "Elegí la placa de video",
    ayuda: "Es la que más pesa para jugar. Si tu procesador tiene gráficos integrados, podés no ponerle.",
  },
  {
    tipo: "almacenamiento",
    nombre: "Almacenamiento",
    titulo: "Elegí el disco",
    ayuda: "Los NVMe son los más rápidos: la PC prende y abre todo en segundos.",
  },
  {
    tipo: "fuente",
    nombre: "Fuente",
    titulo: "Elegí la fuente",
    ayuda: "Solo te mostramos las que alcanzan para lo que consume tu PC, con margen.",
  },
  {
    tipo: "gabinete",
    nombre: "Gabinete",
    titulo: "Elegí el gabinete",
    ayuda: "Solo te mostramos los que tienen lugar para tu mother.",
  },
];

export type Paso = TipoComponente | "resumen";

/**
 * Lo elegido hasta ahora. sinPlacaDeVideo: eligió usar los gráficos del procesador.
 * conArmado: quiere que la armemos y la probemos (se cobra aparte); si no, se lleva los componentes sueltos.
 */
export type Armado = {
  piezas: Partial<Record<TipoComponente, Componente>>;
  sinPlacaDeVideo: boolean;
  conArmado: boolean;
  /** Pasos que la persona decidió saltear (ya tiene esa pieza, o no la quiere). Van en la URL como ?ram=no. */
  salteados?: TipoComponente[];
};

const SIN_PLACA = "integrada";
const SALTEADO = "no";
/** Lo que consume el resto (mother, memorias, disco, ventiladores), estimado. */
const CONSUMO_DEL_RESTO_W = 50;
/** La fuente tiene que dar 50% más de lo que consume la PC: no trabaja al límite y dura más. */
const MARGEN_FUENTE = 1.5;
const FUENTE_MINIMA_W = 450;

// ─── Leer y escribir la URL ────────────────────────────────────────────────────────────────

/**
 * La URL puede venir de cualquier lado (un link viejo, alguien que la escribió a mano):
 * un slug que no existe, o que no es de ese tipo, se ignora.
 */
export function leerArmado(parametros: ParametrosDeUrl, componentes: Componente[]): Armado {
  const piezas: Armado["piezas"] = {};
  const salteados: TipoComponente[] = [];
  let sinPlacaDeVideo = false;

  for (const { tipo } of PASOS) {
    const valor = primero(parametros[tipo]);
    if (tipo === "gpu" && valor === SIN_PLACA) {
      sinPlacaDeVideo = true;
      continue;
    }
    if (valor === SALTEADO) {
      salteados.push(tipo);
      continue;
    }
    const componente = componentes.find((c) => c.tipo === tipo && c.slug === valor);
    if (componente) piezas[tipo] = componente;
  }
  // El armado viene incluido salvo que diga ?armado=no: es lo que elige la mayoría.
  return { piezas, sinPlacaDeVideo, conArmado: primero(parametros.armado) !== "no", salteados };
}

export function leerPaso(parametros: ParametrosDeUrl): Paso | undefined {
  const valor = primero(parametros.paso);
  if (valor === "resumen") return "resumen";
  return PASOS.find((p) => p.tipo === valor)?.tipo;
}

/** /arma-tu-pc?paso=ram&cpu=ryzen-5-5600&mother=a520m — las piezas siempre en el mismo orden. */
export function urlDelArmador(armado: Armado, paso?: Paso): string {
  const parametros = new URLSearchParams();
  if (paso) parametros.set("paso", paso);
  for (const { tipo } of PASOS) {
    if (tipo === "gpu" && armado.sinPlacaDeVideo) parametros.set("gpu", SIN_PLACA);
    const pieza = armado.piezas[tipo];
    if (pieza) parametros.set(tipo, pieza.slug);
    else if (estaSalteado(armado, tipo)) parametros.set(tipo, SALTEADO);
  }
  if (!armado.conArmado) parametros.set("armado", "no");
  const texto = parametros.toString();
  return texto ? `/arma-tu-pc?${texto}` : "/arma-tu-pc";
}

export function armadoDesdeModelo(modelo: ModeloDePc, componentes: Componente[]): Armado {
  const parametros: ParametrosDeUrl = { ...modelo.componentes };
  if (!modelo.componentes.gpu) parametros.gpu = SIN_PLACA;
  return leerArmado(parametros, componentes);
}

// ─── Compatibilidad ────────────────────────────────────────────────────────────────────────

/**
 * ¿Se puede elegir este componente con lo que ya está elegido ANTES en el orden de pasos?
 * Si falta la pieza contra la que se compara (por ejemplo, mother sin procesador), no hay con qué
 * comparar: eso lo resuelve piezaQueFalta(), que le pide a la persona elegirla primero.
 */
export function esCompatible(componente: Componente, armado: Armado): boolean {
  const { cpu, mother } = armado.piezas;
  switch (componente.tipo) {
    case "mother":
      return !cpu || componente.socket === cpu.socket;
    case "ram":
      return !mother || componente.tipoRam === mother.tipoRam;
    case "fuente":
      return (componente.potenciaW ?? 0) >= potenciaRecomendada(armado);
    case "gabinete":
      return !mother || (!!mother.formato && !!componente.formatosSoportados?.includes(mother.formato));
    default:
      return true;
  }
}

/** Para elegir este paso hace falta haber elegido antes... (null = nada, se puede elegir). */
export function piezaQueFalta(tipo: TipoComponente, armado: Armado): TipoComponente | null {
  const necesita: Partial<Record<TipoComponente, TipoComponente>> = {
    mother: "cpu",
    ram: "mother",
    fuente: "cpu",
    gabinete: "mother",
  };
  const requisito = necesita[tipo];
  // Si la pieza de la que depende se salteó, no hay con qué comparar: se muestran todas (ADR-013).
  return requisito && !armado.piezas[requisito] && !estaSalteado(armado, requisito) ? requisito : null;
}

export function estaSalteado(armado: Armado, tipo: TipoComponente): boolean {
  return !!armado.salteados?.includes(tipo);
}

/** Saltear un paso: se saca la pieza que hubiera, y lo que dependía de ella se vuelve a revisar. */
export function saltear(armado: Armado, tipo: TipoComponente): Armado {
  const piezas = { ...armado.piezas };
  delete piezas[tipo];
  const salteados = [...(armado.salteados ?? []).filter((t) => t !== tipo), tipo];
  return quitarIncompatibles({
    piezas,
    sinPlacaDeVideo: tipo === "gpu" ? false : armado.sinPlacaDeVideo,
    conArmado: armado.conArmado,
    salteados,
  });
}

export function opcionesCompatibles(tipo: TipoComponente, armado: Armado, componentes: Componente[]): Componente[] {
  return componentes.filter((c) => c.tipo === tipo && esCompatible(c, armado));
}

/** Se puede usar sin placa de video solo si el procesador tiene gráficos integrados. */
export function puedeIrSinPlacaDeVideo(armado: Armado): boolean {
  return !!armado.piezas.cpu?.graficosIntegrados;
}

/**
 * Elegir una pieza. Si al cambiarla alguna de las que se eligieron DESPUÉS deja de ser compatible
 * (cambiaste a un procesador AM5 y tenías una mother AM4), esa se saca: la persona la vuelve a elegir.
 * Así un armado nunca queda con piezas que no andan juntas.
 */
export function elegir(armado: Armado, componente: Componente): Armado {
  const nuevo: Armado = {
    piezas: { ...armado.piezas, [componente.tipo]: componente },
    sinPlacaDeVideo: componente.tipo === "gpu" ? false : armado.sinPlacaDeVideo,
    conArmado: armado.conArmado,
    salteados: (armado.salteados ?? []).filter((t) => t !== componente.tipo),
  };
  return quitarIncompatibles(nuevo);
}

export function elegirSinPlacaDeVideo(armado: Armado): Armado {
  const piezas = { ...armado.piezas };
  delete piezas.gpu;
  return quitarIncompatibles({
    piezas,
    sinPlacaDeVideo: true,
    conArmado: armado.conArmado,
    salteados: (armado.salteados ?? []).filter((t) => t !== "gpu"),
  });
}

function quitarIncompatibles(armado: Armado): Armado {
  const piezas: Armado["piezas"] = {};
  const resultado: Armado = {
    piezas,
    sinPlacaDeVideo: armado.sinPlacaDeVideo,
    conArmado: armado.conArmado,
    salteados: armado.salteados ?? [],
  };
  // Se recorre en orden: cada pieza se compara contra las que ya quedaron.
  for (const { tipo } of PASOS) {
    const pieza = armado.piezas[tipo];
    if (pieza && esCompatible(pieza, resultado)) piezas[tipo] = pieza;
  }
  if (resultado.sinPlacaDeVideo && !puedeIrSinPlacaDeVideo(resultado)) resultado.sinPlacaDeVideo = false;
  return resultado;
}

/** A dónde ir después de elegir: el primer paso que falta, o el resumen si está todo. */
export function pasoSiguiente(armado: Armado): Paso {
  return pasosQueFaltan(armado)[0] ?? "resumen";
}

/** Los pasos que todavía no se decidieron: ni elegidos ni salteados. */
function pasosQueFaltan(armado: Armado): TipoComponente[] {
  return PASOS.map((p) => p.tipo).filter((tipo) => {
    if (tipo === "gpu" && armado.sinPlacaDeVideo) return false;
    return !armado.piezas[tipo] && !estaSalteado(armado, tipo);
  });
}

// ─── Consumo, total y revisión final ──────────────────────────────────────────────────────

export function consumoEstimado(armado: Armado): number {
  const { cpu, gpu } = armado.piezas;
  return (cpu?.consumoW ?? 0) + (gpu?.consumoW ?? 0) + CONSUMO_DEL_RESTO_W;
}

/** Redondeada para arriba de a 50 W, con un mínimo: 277 W → 450 W; 585 W → 600 W. */
export function potenciaRecomendada(armado: Armado): number {
  const conMargen = Math.ceil((consumoEstimado(armado) * MARGEN_FUENTE) / 50) * 50;
  return Math.max(conMargen, FUENTE_MINIMA_W);
}

export type Revision = {
  /** Lo que falta decidir (elegir o saltear), por nombre: ["Mother", "Fuente"]. */
  faltan: string[];
  /** Advertencias que no impiden comprar: "sin placa de video, tu procesador no da imagen". */
  avisos: string[];
  /** Incompatibilidades (no debería haber, salvo una URL armada a mano). */
  problemas: string[];
  /** null si alguna pieza no tiene precio cargado. */
  total: number | null;
  /** Lista para comprar: no falta nada y no hay problemas. */
  lista: boolean;
};

export function revisarArmado(armado: Armado): Revision {
  const faltan = pasosQueFaltan(armado).map(nombreDelPaso);

  const problemas: string[] = [];
  for (const { tipo, nombre } of PASOS) {
    const pieza = armado.piezas[tipo];
    if (pieza && !esCompatible(pieza, armado)) problemas.push(`${nombre}: ${pieza.nombre} no es compatible con el resto.`);
  }
  if (armado.sinPlacaDeVideo && !puedeIrSinPlacaDeVideo(armado)) {
    problemas.push("El procesador no tiene gráficos integrados: necesitás una placa de video.");
  }

  const avisos: string[] = [];
  const { cpu } = armado.piezas;
  if (estaSalteado(armado, "gpu") && cpu && !cpu.graficosIntegrados) {
    avisos.push("Tu procesador no tiene gráficos integrados: sin placa de video la PC no da imagen.");
  }
  if (Object.keys(armado.piezas).length === 0 && faltan.length === 0) {
    problemas.push("Salteaste todos los pasos: elegí al menos una pieza.");
  }

  const piezas = Object.values(armado.piezas);
  const total = piezas.some((p) => p.precio === null) ? null : piezas.reduce((suma, p) => suma + (p.precio ?? 0), 0);

  return { faltan, avisos, problemas, total, lista: faltan.length === 0 && problemas.length === 0 && piezas.length > 0 };
}

export function nombreDelPaso(tipo: TipoComponente): string {
  return PASOS.find((p) => p.tipo === tipo)?.nombre ?? tipo;
}
