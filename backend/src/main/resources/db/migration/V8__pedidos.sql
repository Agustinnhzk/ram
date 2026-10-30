-- Pedidos de la tienda con pago por transferencia y comprobante. Ver ADR-011.

-- Los datos para transferir de cada taller: se muestran en el checkout y en cada pedido.
CREATE TABLE datos_de_pago (
    id         UUID          PRIMARY KEY,
    taller_id  UUID          NOT NULL UNIQUE REFERENCES talleres (id),
    alias      VARCHAR(60)   NOT NULL,
    titular    VARCHAR(120)  NOT NULL,
    -- Banco o billetera: "Naranja X", "Mercado Pago", "Banco Galicia"...
    entidad    VARCHAR(60)   NOT NULL,
    -- Opcional: el CBU/CVU de 22 dígitos.
    cbu        VARCHAR(22)   CHECK (cbu ~ '^[0-9]{22}$')
);

-- Los de RAM. Son datos reales del negocio (no de ejemplo): van en la migración, no en db/ejemplos.
-- Cambiarlos más adelante es un UPDATE (o una pantalla en el sistema, cuando haga falta).
INSERT INTO datos_de_pago (id, taller_id, alias, titular, entidad)
VALUES ('6f0c1a52-3d1e-4b7a-9c55-000000000001', '00000000-0000-0000-0000-000000000001',
        'ram.nh', 'Agustin Negri Hrytezuk', 'Naranja X');

CREATE TABLE pedidos (
    id                  UUID          PRIMARY KEY,
    taller_id           UUID          NOT NULL REFERENCES talleres (id),
    -- Número para hablar del pedido ("tu pedido 15"): lo asigna el sistema, correlativo dentro de cada taller.
    numero              INT           NOT NULL CHECK (numero > 0),
    cuenta_cliente_id   UUID          NOT NULL REFERENCES cuentas_clientes (id),
    -- El nombre tal como estaba al comprar: el pedido se lee sin consultar otro módulo.
    cliente_nombre      VARCHAR(120)  NOT NULL,
    telefono            VARCHAR(30)   NOT NULL,
    entrega             VARCHAR(10)   NOT NULL CHECK (entrega IN ('RETIRO', 'ENVIO')),
    -- Solo con envío.
    direccion           VARCHAR(300),
    nota                VARCHAR(500),
    -- En pesos, sin centavos (ADR-008). Es la suma de las líneas al momento de comprar: no cambia si después
    -- cambian los precios del catálogo.
    total               BIGINT        NOT NULL CHECK (total > 0),
    estado              VARCHAR(20)   NOT NULL CHECK (estado IN
                            ('ESPERANDO_PAGO', 'PAGO_INFORMADO', 'PAGADO', 'ENTREGADO', 'CANCELADO')),
    creado_en           TIMESTAMPTZ   NOT NULL,
    pagado_en           TIMESTAMPTZ,
    entregado_en        TIMESTAMPTZ,
    cancelado_en        TIMESTAMPTZ,
    motivo_cancelacion  VARCHAR(300),
    CONSTRAINT uq_pedidos_taller_numero UNIQUE (taller_id, numero),
    CONSTRAINT ck_pedidos_direccion CHECK (entrega = 'RETIRO' OR direccion IS NOT NULL)
);

CREATE INDEX ix_pedidos_taller_estado ON pedidos (taller_id, estado);
CREATE INDEX ix_pedidos_taller_cuenta ON pedidos (taller_id, cuenta_cliente_id);

-- Qué se compró, con el precio de ese momento. Nunca se modifica.
CREATE TABLE lineas_pedido (
    id               UUID          PRIMARY KEY,
    taller_id        UUID          NOT NULL REFERENCES talleres (id),
    pedido_id        UUID          NOT NULL REFERENCES pedidos (id),
    posicion         INT           NOT NULL,
    -- "Kit 4 cámaras + DVR" o "PC armada a medida".
    descripcion      VARCHAR(200)  NOT NULL,
    -- Para una PC armada: cada componente ("Procesador: Ryzen 5 5600"...).
    detalle          TEXT[]        NOT NULL DEFAULT '{}',
    -- De dónde salió (el producto, o el link del armado), para poder volver a verlo.
    referencia       VARCHAR(400)  NOT NULL,
    cantidad         INT           NOT NULL CHECK (cantidad BETWEEN 1 AND 20),
    precio_unitario  BIGINT        NOT NULL CHECK (precio_unitario > 0),
    CONSTRAINT uq_lineas_pedido_posicion UNIQUE (pedido_id, posicion)
);

-- Comprobantes de transferencia que subió el cliente. Sin el archivo: los datos se leen seguido (listado, detalle)
-- y el archivo solo al abrirlo. Separados, abrir un pedido no carga 5 MB de imagen.
CREATE TABLE comprobantes_pago (
    id              UUID          PRIMARY KEY,
    taller_id       UUID          NOT NULL REFERENCES talleres (id),
    pedido_id       UUID          NOT NULL REFERENCES pedidos (id),
    nombre_archivo  VARCHAR(120)  NOT NULL,
    -- El tipo REAL, detectado por los primeros bytes del archivo (módulo archivos), no el que dijo el navegador.
    tipo            VARCHAR(10)   NOT NULL CHECK (tipo IN ('PDF', 'JPG', 'PNG')),
    tamano          INT           NOT NULL CHECK (tamano > 0 AND tamano <= 5242880),
    subido_en       TIMESTAMPTZ   NOT NULL,
    -- Si el taller lo rechazó (no llegó la plata, es de otro monto...), por qué.
    rechazado       BOOLEAN       NOT NULL DEFAULT FALSE,
    motivo_rechazo  VARCHAR(300)
);

CREATE INDEX ix_comprobantes_pedido ON comprobantes_pago (taller_id, pedido_id);

-- El archivo en sí. BYTEA: PostgreSQL lo guarda comprimido y fuera de la fila (TOAST) cuando es grande.
-- Se descarta guardarlo en disco o en un bucket: así entra en los backups de la base y no hay otro servicio que cuidar.
CREATE TABLE archivos_comprobantes (
    comprobante_id  UUID   PRIMARY KEY REFERENCES comprobantes_pago (id),
    taller_id       UUID   NOT NULL REFERENCES talleres (id),
    contenido       BYTEA  NOT NULL
);
