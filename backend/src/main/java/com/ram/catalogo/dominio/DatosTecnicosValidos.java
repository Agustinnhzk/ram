package com.ram.catalogo.dominio;

import com.ram.catalogo.DatosTecnicos;

import java.util.List;

/**
 * Revisa que un componente traiga los datos que necesita la compatibilidad de Armá tu PC (ADR-008),
 * y descarta los que no le corresponden a su tipo (el socket de una fuente, por ejemplo).
 */
final class DatosTecnicosValidos {

    private DatosTecnicosValidos() {
    }

    static DatosTecnicos revisar(DatosTecnicos t) {
        if (t == null) {
            return null;
        }
        if (t.tipo() == null) {
            throw new IllegalArgumentException("Elegí qué tipo de componente es");
        }
        return switch (t.tipo()) {
            case CPU -> new DatosTecnicos(t.tipo(), socket(t.socket()), null, null, List.of(), watts(t.consumoW(), "el consumo"),
                    null, t.graficosIntegrados());
            case MOTHER -> {
                if (t.tipoRam() == null) {
                    throw new IllegalArgumentException("Falta el tipo de memoria que acepta la mother");
                }
                if (t.formato() == null) {
                    throw new IllegalArgumentException("Falta el formato de la mother (ATX o Micro ATX)");
                }
                yield new DatosTecnicos(t.tipo(), socket(t.socket()), t.tipoRam(), t.formato(), List.of(), null, null, false);
            }
            case RAM -> {
                if (t.tipoRam() == null) {
                    throw new IllegalArgumentException("Falta el tipo de memoria (DDR4 o DDR5)");
                }
                yield new DatosTecnicos(t.tipo(), null, t.tipoRam(), null, List.of(), null, null, false);
            }
            case GPU -> new DatosTecnicos(t.tipo(), null, null, null, List.of(), watts(t.consumoW(), "el consumo"), null, false);
            case FUENTE -> new DatosTecnicos(t.tipo(), null, null, null, List.of(), null, watts(t.potenciaW(), "la potencia"), false);
            case GABINETE -> {
                if (t.formatosSoportados().isEmpty()) {
                    throw new IllegalArgumentException("Falta qué mothers entran en el gabinete");
                }
                yield new DatosTecnicos(t.tipo(), null, null, null, List.copyOf(new java.util.LinkedHashSet<>(t.formatosSoportados())),
                        null, null, false);
            }
            case ALMACENAMIENTO -> new DatosTecnicos(t.tipo(), null, null, null, List.of(), null, null, false);
        };
    }

    private static String socket(String valor) {
        String limpio = valor == null ? "" : valor.strip().toUpperCase(java.util.Locale.ROOT);
        if (limpio.isEmpty()) {
            throw new IllegalArgumentException("Falta el socket (AM4, AM5, LGA1700...)");
        }
        if (!limpio.matches("[A-Z0-9-]{2,20}")) {
            throw new IllegalArgumentException("El socket solo puede tener letras, números y guiones");
        }
        return limpio;
    }

    private static Integer watts(Integer valor, String campo) {
        if (valor == null || valor <= 0 || valor > 3000) {
            throw new IllegalArgumentException("Falta " + campo + " en watts (entre 1 y 3000)");
        }
        return valor;
    }
}
