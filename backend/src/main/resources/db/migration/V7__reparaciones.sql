-- Reparaciones: órdenes y su bitácora de movimientos. Ver ADR-009.

CREATE TABLE ordenes_reparacion (
    id                UUID          PRIMARY KEY,
    taller_id         UUID          NOT NULL REFERENCES talleres (id),
    -- El número que escribe el taller (900, 901...). Único dentro de cada taller.
    numero            INT           NOT NULL CHECK (numero > 0),
    cliente_id        UUID          NOT NULL REFERENCES clientes (id),
    equipo            VARCHAR(150)  NOT NULL,
    con_cargador      BOOLEAN       NOT NULL DEFAULT FALSE,
    procesador        VARCHAR(100),
    -- Riesgo aceptado (ADR-009): en texto. Solo Dueño y Técnico lo ven desde la API.
    password_equipo   VARCHAR(100),
    problema          VARCHAR(1000) NOT NULL,
    -- Internas del taller (estado físico, rayones...): nunca salen al cliente.
    observaciones     VARCHAR(1000),
    estado            VARCHAR(30)   NOT NULL CHECK (estado IN (
                          'INGRESADO', 'EN_DIAGNOSTICO', 'PRESUPUESTADO', 'APROBADO', 'RECHAZADO', 'EN_REPARACION',
                          'ESPERANDO_REPUESTO', 'EN_PROVEEDOR', 'SIN_REPARACION', 'LISTO_PARA_RETIRAR', 'ENTREGADO')),
    -- Presupuesto: en pesos, sin centavos (ADR-008).
    presupuesto_monto        BIGINT CHECK (presupuesto_monto >= 0),
    presupuesto_descripcion  VARCHAR(1000),
    ingresado_en      TIMESTAMPTZ   NOT NULL,
    entregado_en      TIMESTAMPTZ,
    garantia_dias     INT           CHECK (garantia_dias >= 0),
    -- Hasta qué día (inclusive) tiene garantía, en la fecha de Argentina. "En garantía" se calcula con esto.
    garantia_hasta    DATE,
    CONSTRAINT uq_ordenes_taller_numero UNIQUE (taller_id, numero)
);

CREATE INDEX ix_ordenes_taller_estado ON ordenes_reparacion (taller_id, estado);
CREATE INDEX ix_ordenes_taller_cliente ON ordenes_reparacion (taller_id, cliente_id);

-- Bitácora: solo se agrega, nunca se modifica.
CREATE TABLE movimientos_reparacion (
    id                    UUID          PRIMARY KEY,
    taller_id             UUID          NOT NULL REFERENCES talleres (id),
    orden_id              UUID          NOT NULL REFERENCES ordenes_reparacion (id),
    -- El lugar en la bitácora (0, 1, 2...): el orden exacto, aunque dos movimientos tengan la misma hora.
    posicion              INT           NOT NULL,
    -- NULL si el movimiento es solo una nota (sin cambio de estado).
    estado                VARCHAR(30),
    nota                  VARCHAR(1000),
    -- TRUE = aparece en el seguimiento del cliente; FALSE = interno del taller.
    visible_para_cliente  BOOLEAN       NOT NULL,
    usuario_id            UUID          NOT NULL,
    usuario_nombre        VARCHAR(120)  NOT NULL,
    ocurrido_en           TIMESTAMPTZ   NOT NULL
);

CREATE UNIQUE INDEX uq_movimientos_orden_posicion ON movimientos_reparacion (taller_id, orden_id, posicion);
