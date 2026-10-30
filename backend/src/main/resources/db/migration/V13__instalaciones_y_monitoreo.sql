-- Bloque 4 (ADR-015): instalaciones de cámaras y alarmas, y abonos de monitoreo. Un registro simple.

CREATE TABLE instalaciones (
    id           UUID           PRIMARY KEY,
    taller_id    UUID           NOT NULL REFERENCES talleres (id),
    numero       INT            NOT NULL CHECK (numero > 0),
    cliente_id   UUID           NOT NULL REFERENCES clientes (id),
    direccion    VARCHAR(300)   NOT NULL,
    estado       VARCHAR(15)    NOT NULL CHECK (estado IN ('VISITA', 'PRESUPUESTO', 'INSTALACION', 'TERMINADA')),
    -- Qué se instala: "DVR 8 canales, 4 cámaras, disco 1 TB".
    equipos      VARCHAR(1000),
    camaras      INT            CHECK (camaras BETWEEN 0 AND 500),
    -- La fecha agendada (de la visita o de la instalación, según el estado).
    fecha        DATE,
    tecnico      VARCHAR(120),
    presupuesto  BIGINT         CHECK (presupuesto > 0),
    notas        VARCHAR(2000),
    creada_en    TIMESTAMPTZ    NOT NULL,
    CONSTRAINT uq_instalaciones_numero UNIQUE (taller_id, numero)
);

CREATE INDEX ix_instalaciones_taller_estado ON instalaciones (taller_id, estado);

CREATE TABLE abonados_monitoreo (
    id          UUID          PRIMARY KEY,
    taller_id   UUID          NOT NULL REFERENCES talleres (id),
    cliente_id  UUID          NOT NULL REFERENCES clientes (id),
    direccion   VARCHAR(300),
    -- El abono mensual en pesos (null = sin definir).
    monto       BIGINT        CHECK (monto > 0),
    activo      BOOLEAN       NOT NULL DEFAULT TRUE
);

CREATE INDEX ix_abonados_taller ON abonados_monitoreo (taller_id);

-- Una fila = pagó ese mes. No pagó = no hay fila.
CREATE TABLE pagos_abono (
    id          UUID      PRIMARY KEY,
    taller_id   UUID      NOT NULL REFERENCES talleres (id),
    abonado_id  UUID      NOT NULL REFERENCES abonados_monitoreo (id),
    anio        INT       NOT NULL CHECK (anio BETWEEN 2000 AND 2100),
    mes         INT       NOT NULL CHECK (mes BETWEEN 1 AND 12),
    CONSTRAINT uq_pagos_abono UNIQUE (abonado_id, anio, mes)
);

CREATE INDEX ix_pagos_abono_anio ON pagos_abono (taller_id, anio);
