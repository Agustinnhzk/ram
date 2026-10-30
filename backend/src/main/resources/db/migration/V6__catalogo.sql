-- Catálogo: categorías, productos (incluidos los componentes de Armá tu PC) e historial de precios.
-- Una sola tabla de productos: un componente es un producto con algunos datos técnicos más.

CREATE TABLE categorias (
    id               UUID         PRIMARY KEY,
    taller_id        UUID         NOT NULL REFERENCES talleres (id),
    -- Lo que va en la URL de la tienda: /productos?categoria=camaras
    slug             VARCHAR(60)  NOT NULL,
    nombre           VARCHAR(80)  NOT NULL,
    -- Si los productos se instalan a domicilio (cámaras, alarmas): la tienda lo muestra.
    con_instalacion  BOOLEAN      NOT NULL DEFAULT FALSE,
    orden            INT          NOT NULL DEFAULT 0,
    CONSTRAINT uq_categorias_taller_slug UNIQUE (taller_id, slug)
);

CREATE TABLE productos (
    id                   UUID          PRIMARY KEY,
    taller_id            UUID          NOT NULL REFERENCES talleres (id),
    categoria_id         UUID          NOT NULL REFERENCES categorias (id),
    slug                 VARCHAR(120)  NOT NULL,
    nombre               VARCHAR(150)  NOT NULL,
    condicion            VARCHAR(20)   NOT NULL CHECK (condicion IN ('NUEVO', 'REACONDICIONADO')),
    -- En pesos, sin centavos. NULL = "precio a consultar".
    precio               BIGINT        CHECK (precio >= 0),
    imagen               VARCHAR(300),
    resumen              VARCHAR(300)  NOT NULL DEFAULT '',
    descripcion          TEXT          NOT NULL DEFAULT '',
    caracteristicas      TEXT[]        NOT NULL DEFAULT '{}',
    garantia_meses       INT           CHECK (garantia_meses > 0),
    -- false = no se muestra en la tienda (pero sigue en el sistema, por ejemplo para actualizarle el precio).
    publicado            BOOLEAN       NOT NULL DEFAULT TRUE,
    orden                INT           NOT NULL DEFAULT 0,

    -- Solo para componentes de Armá tu PC (NULL en el resto). Son los datos que necesita
    -- la compatibilidad básica: socket, tipo de memoria, formato de mother, consumo y potencia.
    tipo_componente      VARCHAR(20)   CHECK (tipo_componente IN
                             ('CPU', 'MOTHER', 'RAM', 'GPU', 'ALMACENAMIENTO', 'FUENTE', 'GABINETE')),
    socket               VARCHAR(20),
    tipo_ram             VARCHAR(4)    CHECK (tipo_ram IN ('DDR4', 'DDR5')),
    formato              VARCHAR(4)    CHECK (formato IN ('ATX', 'MATX')),
    formatos_soportados  TEXT[]        NOT NULL DEFAULT '{}',
    consumo_w            INT           CHECK (consumo_w >= 0),
    potencia_w           INT           CHECK (potencia_w > 0),
    graficos_integrados  BOOLEAN       NOT NULL DEFAULT FALSE,

    creado_en            TIMESTAMPTZ   NOT NULL DEFAULT now(),
    CONSTRAINT uq_productos_taller_slug UNIQUE (taller_id, slug)
);

CREATE INDEX ix_productos_taller_categoria ON productos (taller_id, categoria_id);

-- Cada cambio de precio, para siempre: cuánto era, cuánto pasó a ser, por qué, quién y cuándo.
CREATE TABLE historial_precios (
    id               UUID          PRIMARY KEY,
    taller_id        UUID          NOT NULL REFERENCES talleres (id),
    producto_id      UUID          NOT NULL REFERENCES productos (id),
    precio_anterior  BIGINT,
    precio_nuevo     BIGINT,
    motivo           VARCHAR(20)   NOT NULL CHECK (motivo IN ('MANUAL', 'PORCENTAJE')),
    -- Solo si el motivo es PORCENTAJE: por ejemplo 8.50 (= +8,5%) o -10.00.
    porcentaje       NUMERIC(6, 2),
    -- Quién lo cambió. Sin FOREIGN KEY a propósito: el catálogo no depende de las tablas de usuarios.
    -- El nombre se guarda tal como era en ese momento, para leer el historial sin consultar otro módulo.
    usuario_id       UUID          NOT NULL,
    usuario_nombre   VARCHAR(120)  NOT NULL,
    cambiado_en      TIMESTAMPTZ   NOT NULL
);

CREATE INDEX ix_historial_precios_producto ON historial_precios (taller_id, producto_id, cambiado_en DESC);
