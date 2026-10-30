package com.ram.catalogo.adaptadores.persistencia;

import com.ram.catalogo.Condicion;
import com.ram.catalogo.FormatoMother;
import com.ram.catalogo.TipoComponente;
import com.ram.catalogo.TipoRam;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.TenantId;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "productos")
class ProductoJpaEntity {

    @Id
    private UUID id;

    @TenantId
    @Column(name = "taller_id", nullable = false, updatable = false)
    private UUID tallerId;

    /** LAZY: la categoría se trae solo cuando la consulta lo pide (join fetch), nunca "de sorpresa". */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "categoria_id", nullable = false)
    private CategoriaJpaEntity categoria;

    @Column(nullable = false, length = 120)
    private String slug;

    @Column(nullable = false, length = 150)
    private String nombre;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Condicion condicion;

    /** En pesos, sin centavos. null = a consultar. */
    private Long precio;

    /** Oferta (V11): precio y último día. Los dos juntos o ninguno. */
    @Column(name = "precio_oferta")
    private Long precioOferta;

    @Column(name = "oferta_hasta")
    private LocalDate ofertaHasta;

    @Column(length = 300)
    private String imagen;

    @Column(nullable = false, length = 300)
    private String resumen;

    @Column(nullable = false)
    private String descripcion;

    /** Columna TEXT[] de PostgreSQL: una lista de textos en una sola columna, sin otra tabla. */
    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(nullable = false)
    private String[] caracteristicas;

    @Column(name = "garantia_meses")
    private Integer garantiaMeses;

    @Column(nullable = false)
    private boolean publicado;

    @Column(nullable = false)
    private int orden;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_componente", length = 20)
    private TipoComponente tipoComponente;

    @Column(length = 20)
    private String socket;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_ram", length = 4)
    private TipoRam tipoRam;

    @Enumerated(EnumType.STRING)
    @Column(length = 4)
    private FormatoMother formato;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "formatos_soportados", nullable = false)
    private String[] formatosSoportados;

    @Column(name = "consumo_w")
    private Integer consumoW;

    @Column(name = "potencia_w")
    private Integer potenciaW;

    @Column(name = "graficos_integrados", nullable = false)
    private boolean graficosIntegrados;

    protected ProductoJpaEntity() {
        // requerido por JPA
    }

    /** Un producto nuevo: va al final de su categoría. Los datos se completan con actualizar(). */
    ProductoJpaEntity(UUID id, String slug, int orden) {
        this.id = id;
        this.slug = slug;
        this.orden = orden;
        this.caracteristicas = new String[0];
        this.formatosSoportados = new String[0];
    }

    void actualizar(CategoriaJpaEntity categoria, String nombre, Condicion condicion, Long precio, Long precioOferta,
                    LocalDate ofertaHasta, String resumen, String descripcion, String[] caracteristicas,
                    Integer garantiaMeses, boolean publicado, TipoComponente tipoComponente, String socket,
                    TipoRam tipoRam, FormatoMother formato, String[] formatosSoportados, Integer consumoW,
                    Integer potenciaW, boolean graficosIntegrados) {
        this.categoria = categoria;
        this.nombre = nombre;
        this.condicion = condicion;
        this.precio = precio;
        this.precioOferta = precioOferta;
        this.ofertaHasta = ofertaHasta;
        this.resumen = resumen;
        this.descripcion = descripcion;
        this.caracteristicas = caracteristicas;
        this.garantiaMeses = garantiaMeses;
        this.publicado = publicado;
        this.tipoComponente = tipoComponente;
        this.socket = socket;
        this.tipoRam = tipoRam;
        this.formato = formato;
        this.formatosSoportados = formatosSoportados;
        this.consumoW = consumoW;
        this.potenciaW = potenciaW;
        this.graficosIntegrados = graficosIntegrados;
    }

    Long getPrecioOferta() {
        return precioOferta;
    }

    LocalDate getOfertaHasta() {
        return ofertaHasta;
    }

    UUID getId() {
        return id;
    }

    CategoriaJpaEntity getCategoria() {
        return categoria;
    }

    String getSlug() {
        return slug;
    }

    String getNombre() {
        return nombre;
    }

    Condicion getCondicion() {
        return condicion;
    }

    Long getPrecio() {
        return precio;
    }

    String getImagen() {
        return imagen;
    }

    String getResumen() {
        return resumen;
    }

    String getDescripcion() {
        return descripcion;
    }

    String[] getCaracteristicas() {
        return caracteristicas;
    }

    Integer getGarantiaMeses() {
        return garantiaMeses;
    }

    boolean isPublicado() {
        return publicado;
    }

    TipoComponente getTipoComponente() {
        return tipoComponente;
    }

    String getSocket() {
        return socket;
    }

    TipoRam getTipoRam() {
        return tipoRam;
    }

    FormatoMother getFormato() {
        return formato;
    }

    String[] getFormatosSoportados() {
        return formatosSoportados;
    }

    Integer getConsumoW() {
        return consumoW;
    }

    Integer getPotenciaW() {
        return potenciaW;
    }

    boolean isGraficosIntegrados() {
        return graficosIntegrados;
    }
}
