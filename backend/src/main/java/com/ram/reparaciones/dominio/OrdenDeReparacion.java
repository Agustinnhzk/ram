package com.ram.reparaciones.dominio;

import com.ram.clientes.ClienteId;
import com.ram.reparaciones.EstadoReparacion;
import com.ram.reparaciones.OrdenId;
import com.ram.reparaciones.TransicionInvalidaException;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Una orden de reparación. Es la única forma de cambiar una reparación, y se ocupa de que:
 * <ul>
 *   <li>solo se pueda pasar a los estados que permite el recorrido (ADR-009);</li>
 *   <li>cada cambio quede en la bitácora, con quién y cuándo;</li>
 *   <li>no se pueda presupuestar sin monto ni entregar sin decir la garantía.</li>
 * </ul>
 */
public class OrdenDeReparacion {

    static final int NUMERO_MAXIMO = 9_999_999;
    static final int GARANTIA_MAXIMA_DIAS = 3_650;
    static final long PRESUPUESTO_MAXIMO = 1_000_000_000_000L;

    private final OrdenId id;
    private final int numero;
    private final ClienteId cliente;
    private final Equipo equipo;
    private final String password;
    private final String problema;
    private final String observaciones;
    private final Instant ingresadoEn;
    private EstadoReparacion estado;
    private Long presupuestoMonto;
    private String presupuestoDescripcion;
    private Instant entregadoEn;
    private Integer garantiaDias;
    private LocalDate garantiaHasta;
    private final List<Movimiento> movimientos;

    public OrdenDeReparacion(OrdenId id, int numero, ClienteId cliente, Equipo equipo, String password, String problema,
                             String observaciones, Instant ingresadoEn, EstadoReparacion estado, Long presupuestoMonto,
                             String presupuestoDescripcion, Instant entregadoEn, Integer garantiaDias,
                             LocalDate garantiaHasta, List<Movimiento> movimientos) {
        if (numero <= 0 || numero > NUMERO_MAXIMO) {
            throw new IllegalArgumentException("El número de orden tiene que estar entre 1 y " + NUMERO_MAXIMO);
        }
        this.id = Objects.requireNonNull(id);
        this.numero = numero;
        this.cliente = Objects.requireNonNull(cliente, "Falta el cliente");
        this.equipo = Objects.requireNonNull(equipo, "Falta el equipo");
        this.password = Texto.opcional(password, "el password", 100);
        this.problema = Texto.obligatorio(problema, "el problema", 1000);
        this.observaciones = Texto.opcional(observaciones, "las observaciones", 1000);
        this.ingresadoEn = Objects.requireNonNull(ingresadoEn);
        this.estado = Objects.requireNonNull(estado);
        this.presupuestoMonto = presupuestoMonto;
        this.presupuestoDescripcion = presupuestoDescripcion;
        this.entregadoEn = entregadoEn;
        this.garantiaDias = garantiaDias;
        this.garantiaHasta = garantiaHasta;
        this.movimientos = new ArrayList<>(movimientos);
    }

    /** El ingreso: la orden nace en INGRESADO, con su primer movimiento (visible para el cliente). */
    public static OrdenDeReparacion ingresar(int numero, ClienteId cliente, Equipo equipo, String password, String problema,
                                             String observaciones, Autor autor, Instant cuando) {
        OrdenDeReparacion orden = new OrdenDeReparacion(OrdenId.nueva(), numero, cliente, equipo, password, problema,
                observaciones, cuando, EstadoReparacion.INGRESADO, null, null, null, null, null, List.of());
        orden.movimientos.add(Movimiento.nuevo(EstadoReparacion.INGRESADO, null, true, autor, cuando));
        return orden;
    }

    /**
     * Los cambios de estado simples. PRESUPUESTADO y ENTREGADO no van por acá: necesitan datos
     * (el monto, la garantía) y tienen su propio método.
     *
     * @param notaParaCliente si la NOTA la ve el cliente. El cambio de estado lo ve siempre.
     */
    public void cambiarEstado(EstadoReparacion destino, String nota, boolean notaParaCliente, Autor autor, Instant cuando) {
        Objects.requireNonNull(destino, "Falta el estado");
        if (destino == EstadoReparacion.PRESUPUESTADO) {
            throw new TransicionInvalidaException("Para presupuestar hay que cargar el monto");
        }
        if (destino == EstadoReparacion.ENTREGADO) {
            throw new TransicionInvalidaException("Para entregar hay que indicar la garantía");
        }
        pasarA(destino, Texto.opcional(nota, "la nota", 1000), notaParaCliente, autor, cuando);
    }

    /** Carga el presupuesto. También sirve para corregirlo mientras está PRESUPUESTADO (todavía sin respuesta). */
    public void presupuestar(long monto, String descripcion, Autor autor, Instant cuando) {
        if (monto < 0 || monto > PRESUPUESTO_MAXIMO) {
            throw new IllegalArgumentException("El monto del presupuesto no es válido");
        }
        String detalle = Texto.obligatorio(descripcion, "la descripción del presupuesto", 1000);
        if (estado == EstadoReparacion.PRESUPUESTADO) {
            registrar(null, "Se corrigió el presupuesto", true, autor, cuando);
        } else {
            pasarA(EstadoReparacion.PRESUPUESTADO, null, true, autor, cuando);
        }
        this.presupuestoMonto = monto;
        this.presupuestoDescripcion = detalle;
    }

    /**
     * La salida. La garantía es de ESTA reparación (0 = sin garantía) y vence al final del día
     * {@code entrega + días}, en la fecha del taller.
     */
    public void entregar(int dias, String nota, Autor autor, Instant cuando, ZoneId zonaDelTaller) {
        if (dias < 0 || dias > GARANTIA_MAXIMA_DIAS) {
            throw new IllegalArgumentException("La garantía tiene que estar entre 0 y " + GARANTIA_MAXIMA_DIAS + " días");
        }
        pasarA(EstadoReparacion.ENTREGADO, Texto.opcional(nota, "la nota", 1000), true, autor, cuando);
        this.entregadoEn = cuando;
        this.garantiaDias = dias;
        this.garantiaHasta = dias == 0 ? null : LocalDate.ofInstant(cuando, zonaDelTaller).plusDays(dias);
    }

    public void agregarNota(String texto, boolean paraCliente, Autor autor, Instant cuando) {
        registrar(null, Texto.obligatorio(texto, "la nota", 1000), paraCliente, autor, cuando);
    }

    /** "En garantía" se calcula (ADR-009): entregada y con la fecha de vencimiento todavía por delante. */
    public boolean enGarantia(LocalDate hoy) {
        return estado == EstadoReparacion.ENTREGADO && garantiaHasta != null && !hoy.isAfter(garantiaHasta);
    }

    /**
     * El cliente ve SIEMPRE los cambios de estado (es su línea de tiempo). Si la nota es interna,
     * se guardan dos movimientos: el cambio de estado (visible) y la nota (interna).
     */
    private void pasarA(EstadoReparacion destino, String nota, boolean notaParaCliente, Autor autor, Instant cuando) {
        if (!estado.puedePasarA(destino)) {
            throw new TransicionInvalidaException("No se puede pasar de " + estado + " a " + destino);
        }
        this.estado = destino;
        if (nota == null || notaParaCliente) {
            registrar(destino, nota, true, autor, cuando);
        } else {
            registrar(destino, null, true, autor, cuando);
            registrar(null, nota, false, autor, cuando);
        }
    }

    private void registrar(EstadoReparacion nuevoEstado, String nota, boolean paraCliente, Autor autor, Instant cuando) {
        movimientos.add(Movimiento.nuevo(nuevoEstado, nota, paraCliente, autor, cuando));
    }

    public OrdenId id() {
        return id;
    }

    public int numero() {
        return numero;
    }

    public ClienteId cliente() {
        return cliente;
    }

    public Equipo equipo() {
        return equipo;
    }

    /** Ojo: dato sensible. Quién puede verlo lo decide el servicio (Dueño y Técnico). */
    public String password() {
        return password;
    }

    public String problema() {
        return problema;
    }

    public String observaciones() {
        return observaciones;
    }

    public Instant ingresadoEn() {
        return ingresadoEn;
    }

    public EstadoReparacion estado() {
        return estado;
    }

    public Long presupuestoMonto() {
        return presupuestoMonto;
    }

    public String presupuestoDescripcion() {
        return presupuestoDescripcion;
    }

    public Instant entregadoEn() {
        return entregadoEn;
    }

    public Integer garantiaDias() {
        return garantiaDias;
    }

    public LocalDate garantiaHasta() {
        return garantiaHasta;
    }

    /** De la más vieja a la más nueva. */
    public List<Movimiento> movimientos() {
        return List.copyOf(movimientos);
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof OrdenDeReparacion otra && id.equals(otra.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    /** Sin el password (ADR-009). */
    @Override
    public String toString() {
        return "OrdenDeReparacion[numero=" + numero + ", estado=" + estado + "]";
    }
}
