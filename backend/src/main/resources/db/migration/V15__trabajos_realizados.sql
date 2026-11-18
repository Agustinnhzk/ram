-- Trabajos realizados (ADR-018): casos reales que se publican en la tienda (/trabajos) para mostrar
-- qué hacemos y en qué barrios. Se cargan a mano desde el sistema.

CREATE TABLE trabajos (
    id               UUID           PRIMARY KEY,
    taller_id        UUID           NOT NULL REFERENCES talleres (id),
    -- La parte de la URL: /trabajos/notebook-que-no-encendia-abasto. No cambia al editar (no rompe links).
    slug             VARCHAR(100)   NOT NULL,
    tipo             VARCHAR(15)    NOT NULL CHECK (tipo IN ('NOTEBOOK', 'PC', 'CAMARAS', 'ALARMAS', 'OTRO')),
    titulo           VARCHAR(120)   NOT NULL,
    -- El barrio o la localidad como se muestra ("Alberdi", "Funes"), sin calle ni altura.
    barrio           VARCHAR(60)    NOT NULL,
    -- El mismo barrio normalizado ("alberdi"): "B° Alberdi" y "alberdi" son la misma zona.
    barrio_clave     VARCHAR(60)    NOT NULL,
    a_domicilio      BOOLEAN        NOT NULL DEFAULT FALSE,
    problema         VARCHAR(1000)  NOT NULL,
    diagnostico      VARCHAR(1000),
    solucion         VARCHAR(1000)  NOT NULL,
    -- Cuándo se hizo el trabajo.
    fecha            DATE           NOT NULL,
    -- Solo para el taller (no se publica): el número de la orden de reparación o instalación, si hubo.
    numero_de_orden  INT            CHECK (numero_de_orden > 0),
    -- El cliente aceptó que se publique. Sin esto no se puede publicar.
    consentimiento   BOOLEAN        NOT NULL DEFAULT FALSE,
    publicado        BOOLEAN        NOT NULL DEFAULT FALSE,
    creado_en        TIMESTAMPTZ    NOT NULL,
    CONSTRAINT uq_trabajos_slug UNIQUE (taller_id, slug),
    CONSTRAINT ck_trabajos_publicado_con_permiso CHECK (NOT publicado OR consentimiento)
);

CREATE INDEX ix_trabajos_taller_fecha ON trabajos (taller_id, fecha DESC);

-- Fotos: como las de productos (V11), los datos por un lado y los bytes por otro.
CREATE TABLE fotos_trabajos (
    id          UUID         PRIMARY KEY,
    taller_id   UUID         NOT NULL REFERENCES talleres (id),
    trabajo_id  UUID         NOT NULL REFERENCES trabajos (id),
    -- 0 = la principal.
    posicion    INT          NOT NULL CHECK (posicion >= 0),
    ancho       INT          NOT NULL,
    alto        INT          NOT NULL,
    subida_en   TIMESTAMPTZ  NOT NULL
);

CREATE INDEX ix_fotos_trabajo ON fotos_trabajos (taller_id, trabajo_id, posicion);

-- Dos tamaños en JPEG (1600 px y 600 px). Al prepararlas se vuelven a guardar sin el EXIF del celular:
-- no queda la ubicación GPS de la casa del cliente.
CREATE TABLE archivos_fotos_trabajos (
    foto_id    UUID   PRIMARY KEY REFERENCES fotos_trabajos (id),
    taller_id  UUID   NOT NULL REFERENCES talleres (id),
    grande     BYTEA  NOT NULL,
    chica      BYTEA  NOT NULL
);
