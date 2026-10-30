package com.ram.pedidos.dominio;

import com.ram.catalogo.DatosProducto;
import com.ram.catalogo.DatosTecnicos;
import com.ram.catalogo.TipoComponente;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Las reglas de Armá tu PC, del lado del servidor (todos los pasos se pueden saltear, ADR-013). Son las MISMAS que la tienda (web/src/lib/arma-tu-pc.ts):
 * la tienda las usa para guiar a la persona; acá se vuelven a revisar antes de aceptar el pedido, porque
 * lo que llega del navegador se puede armar a mano.
 *
 * <p>Si cambia una regla, hay que cambiarla en los dos lados (los tests de cada lado lo recuerdan).
 */
public final class ArmadoDePc {

    /** Lo que consume el resto (mother, memorias, disco, ventiladores), estimado. */
    static final int CONSUMO_DEL_RESTO_W = 50;
    static final int FUENTE_MINIMA_W = 450;

    private static final Map<TipoComponente, String> NOMBRES = Map.of(
            TipoComponente.CPU, "Procesador",
            TipoComponente.MOTHER, "Mother",
            TipoComponente.RAM, "Memoria RAM",
            TipoComponente.GPU, "Placa de video",
            TipoComponente.ALMACENAMIENTO, "Almacenamiento",
            TipoComponente.FUENTE, "Fuente",
            TipoComponente.GABINETE, "Gabinete");

    private final Map<TipoComponente, DatosProducto> piezas;
    private final boolean sinPlacaDeVideo;

    private ArmadoDePc(Map<TipoComponente, DatosProducto> piezas, boolean sinPlacaDeVideo) {
        this.piezas = piezas;
        this.sinPlacaDeVideo = sinPlacaDeVideo;
    }

    /**
     * Revisa que lo elegido sea compatible ENTRE SÍ. Cualquier paso se puede saltear (ADR-013): la persona puede
     * tener algunas piezas, o comprar solo una parte. Lo único que se exige es que haya al menos una pieza.
     *
     * @param piezas          cada componente según su tipo (ya buscado en el catálogo)
     * @param sinPlacaDeVideo eligió usar los gráficos del procesador (solo cambia el detalle del pedido)
     * @throws IllegalArgumentException con un mensaje para el cliente
     */
    public static ArmadoDePc revisar(Map<TipoComponente, DatosProducto> piezas, boolean sinPlacaDeVideo) {
        Map<TipoComponente, DatosProducto> copia = new EnumMap<>(TipoComponente.class);
        copia.putAll(piezas);
        if (copia.isEmpty()) {
            throw new IllegalArgumentException("Elegí al menos una pieza para tu PC");
        }
        for (Map.Entry<TipoComponente, DatosProducto> e : copia.entrySet()) {
            DatosTecnicos tecnicos = e.getValue().tecnicos();
            if (tecnicos == null || tecnicos.tipo() != e.getKey()) {
                throw new IllegalArgumentException(e.getValue().nombre() + " no es un componente de tipo " + nombre(e.getKey()));
            }
        }
        ArmadoDePc armado = new ArmadoDePc(copia, sinPlacaDeVideo && !copia.containsKey(TipoComponente.GPU));
        List<String> problemas = armado.problemas();
        if (!problemas.isEmpty()) {
            throw new IllegalArgumentException("La PC armada no es válida: " + String.join(" ", problemas));
        }
        return armado;
    }

    /** Cada regla se revisa solo si están las DOS piezas que compara (las mismas reglas que la tienda). */
    private List<String> problemas() {
        List<String> problemas = new ArrayList<>();
        DatosTecnicos cpu = tecnicos(TipoComponente.CPU);
        DatosTecnicos mother = tecnicos(TipoComponente.MOTHER);
        DatosTecnicos ram = tecnicos(TipoComponente.RAM);
        DatosTecnicos fuente = tecnicos(TipoComponente.FUENTE);
        DatosTecnicos gabinete = tecnicos(TipoComponente.GABINETE);
        if (cpu != null && mother != null && !Objects.equals(cpu.socket(), mother.socket())) {
            problemas.add("La mother no tiene el socket del procesador.");
        }
        if (mother != null && ram != null && ram.tipoRam() != mother.tipoRam()) {
            problemas.add("La memoria no es del tipo que acepta la mother.");
        }
        if (fuente != null && (fuente.potenciaW() == null || fuente.potenciaW() < potenciaRecomendada())) {
            problemas.add("La fuente no alcanza: hace falta una de " + potenciaRecomendada() + " W o más.");
        }
        if (mother != null && gabinete != null
                && (mother.formato() == null || !gabinete.formatosSoportados().contains(mother.formato()))) {
            problemas.add("La mother no entra en el gabinete.");
        }
        return problemas;
    }

    /** Consumo de procesador + placa + resto, con 50% de margen, redondeado para arriba de a 50 W y con un mínimo. */
    public int potenciaRecomendada() {
        int consumo = consumo(TipoComponente.CPU) + consumo(TipoComponente.GPU) + CONSUMO_DEL_RESTO_W;
        int conMargen = (int) Math.ceil(consumo * 1.5 / 50) * 50;
        return Math.max(conMargen, FUENTE_MINIMA_W);
    }

    /** null si alguna pieza no tiene precio. */
    public Long precio() {
        long total = 0;
        for (DatosProducto pieza : piezas.values()) {
            if (pieza.precioVigente() == null) {
                return null;
            }
            total = Math.addExact(total, pieza.precioVigente());
        }
        return total;
    }

    /** "Procesador: AMD Ryzen 5 5600", en el orden de los pasos. Lo salteado se aclara, para que nadie espere esa pieza. */
    public List<String> detalle() {
        List<String> lineas = new ArrayList<>();
        for (TipoComponente tipo : TipoComponente.values()) {
            DatosProducto pieza = piezas.get(tipo);
            if (pieza != null) {
                lineas.add(nombre(tipo) + ": " + pieza.nombre());
            } else if (tipo == TipoComponente.GPU && sinPlacaDeVideo) {
                lineas.add(nombre(tipo) + ": gráficos integrados del procesador");
            } else {
                lineas.add(nombre(tipo) + ": no incluida");
            }
        }
        return lineas;
    }

    /** El link para volver a ver este armado en la tienda (mismo formato que la web). */
    public String enlace() {
        StringBuilder url = new StringBuilder("/arma-tu-pc?paso=resumen");
        for (TipoComponente tipo : TipoComponente.values()) {
            DatosProducto pieza = piezas.get(tipo);
            // Lo salteado va como "no" (o "integrada" para la placa de video): el link reabre el mismo resumen.
            String valor = pieza != null ? pieza.slug() : (tipo == TipoComponente.GPU && sinPlacaDeVideo ? "integrada" : "no");
            url.append('&').append(tipo.name().toLowerCase()).append('=').append(valor);
        }
        return url.toString();
    }

    /** Las piezas que no tienen precio (para decirle al cliente cuál). */
    public List<String> piezasSinPrecio() {
        return piezas.values().stream().filter(p -> p.precioVigente() == null).map(DatosProducto::nombre).toList();
    }

    /** null si esa pieza no se eligió. */
    private DatosTecnicos tecnicos(TipoComponente tipo) {
        DatosProducto pieza = piezas.get(tipo);
        return pieza == null ? null : pieza.tecnicos();
    }

    private int consumo(TipoComponente tipo) {
        DatosProducto pieza = piezas.get(tipo);
        return pieza == null || pieza.tecnicos().consumoW() == null ? 0 : pieza.tecnicos().consumoW();
    }

    static String nombre(TipoComponente tipo) {
        return NOMBRES.get(tipo);
    }
}
