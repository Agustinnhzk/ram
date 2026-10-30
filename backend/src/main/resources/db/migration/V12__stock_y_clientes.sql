-- Bloque 3 (ADR-014): stock con reservas, ficha de cliente vinculada a la cuenta de la tienda.

-- ─── Stock ─────────────────────────────────────────────────────────────────────────────────

-- Una fila por producto CON control de stock. Sin fila = se vende sin límite (como hasta ahora).
CREATE TABLE existencias (
    producto_id     UUID         PRIMARY KEY REFERENCES productos (id),
    taller_id       UUID         NOT NULL REFERENCES talleres (id),
    -- Nunca negativa: lo vendido sin stock queda en reservas_stock.
    cantidad        INT          NOT NULL CHECK (cantidad BETWEEN 0 AND 100000),
    actualizado_en  TIMESTAMPTZ  NOT NULL
);

CREATE INDEX ix_existencias_taller ON existencias (taller_id);

-- El historial: cada cambio de stock, con quién, cuándo y por qué. Solo se insertan filas.
CREATE TABLE movimientos_stock (
    id             UUID          PRIMARY KEY,
    taller_id      UUID          NOT NULL REFERENCES talleres (id),
    producto_id    UUID          NOT NULL REFERENCES productos (id),
    tipo           VARCHAR(20)   NOT NULL CHECK (tipo IN ('INICIAL', 'INGRESO', 'AJUSTE', 'VENTA', 'RESERVA_CUBIERTA', 'DEVOLUCION')),
    -- Cuánto cambió (+ entra, − sale) y cuánto quedó.
    cantidad       INT           NOT NULL,
    saldo          INT           NOT NULL CHECK (saldo >= 0),
    nota           VARCHAR(300),
    -- Si lo causó un pedido. Sin FOREIGN KEY a pedidos a propósito: stock no depende de pedidos (al revés sí).
    pedido_id      UUID,
    pedido_numero  INT,
    usuario        VARCHAR(120),
    fecha          TIMESTAMPTZ   NOT NULL
);

CREATE INDEX ix_movimientos_stock_producto ON movimientos_stock (taller_id, producto_id, fecha DESC);
CREATE INDEX ix_movimientos_stock_pedido ON movimientos_stock (taller_id, pedido_id) WHERE pedido_id IS NOT NULL;

-- Unidades pagadas que no había en stock: esperan mercadería.
CREATE TABLE reservas_stock (
    id             UUID          PRIMARY KEY,
    taller_id      UUID          NOT NULL REFERENCES talleres (id),
    producto_id    UUID          NOT NULL REFERENCES productos (id),
    pedido_id      UUID          NOT NULL,
    pedido_numero  INT           NOT NULL,
    cantidad       INT           NOT NULL CHECK (cantidad > 0),
    estado         VARCHAR(12)   NOT NULL CHECK (estado IN ('PENDIENTE', 'CUBIERTA', 'CANCELADA')),
    creada_en      TIMESTAMPTZ   NOT NULL,
    resuelta_en    TIMESTAMPTZ,
    CONSTRAINT ck_reservas_resuelta CHECK ((estado = 'PENDIENTE') = (resuelta_en IS NULL))
);

CREATE INDEX ix_reservas_stock_estado ON reservas_stock (taller_id, estado, creada_en);
CREATE INDEX ix_reservas_stock_pedido ON reservas_stock (taller_id, pedido_id);

-- Qué productos lleva cada línea de pedido (para descontar el stock) y si al pedirla faltaba stock.
-- Para una PC: sus componentes. Para el armado: ninguno. Los pedidos viejos quedan vacíos (no descuentan).
ALTER TABLE lineas_pedido
    ADD COLUMN productos  UUID[]   NOT NULL DEFAULT '{}',
    ADD COLUMN sin_stock  BOOLEAN  NOT NULL DEFAULT FALSE;

-- ─── Clientes ──────────────────────────────────────────────────────────────────────────────

-- Notas internas del taller (nunca las ve el cliente) y la cuenta de la tienda vinculada a la ficha.
ALTER TABLE clientes
    ADD COLUMN notas             VARCHAR(2000),
    ADD COLUMN cuenta_tienda_id  UUID REFERENCES cuentas_clientes (id);

-- Una cuenta, una ficha.
CREATE UNIQUE INDEX uq_clientes_cuenta_tienda ON clientes (taller_id, cuenta_tienda_id) WHERE cuenta_tienda_id IS NOT NULL;
-- Para reconocer a quien compra por primera vez: por correo o por teléfono.
CREATE INDEX ix_clientes_taller_telefono ON clientes (taller_id, telefono);
CREATE INDEX ix_clientes_taller_correo ON clientes (taller_id, correo) WHERE correo IS NOT NULL;
