-- Formas de entrega con envío dentro y fuera de Rosario, y el precio del servicio de armado de PC. Ver ADR-011.

-- RETIRO: lo busca en el local. ENVIO_ROSARIO: gratis. ENVIO_OTRA_LOCALIDAD: el envío se cotiza por WhatsApp y se paga aparte.
-- (El CHECK de V8 se llama pedidos_entrega_check: es el nombre que PostgreSQL le pone a un CHECK de una columna.)
ALTER TABLE pedidos DROP CONSTRAINT pedidos_entrega_check;
ALTER TABLE pedidos ALTER COLUMN entrega TYPE VARCHAR(25);
UPDATE pedidos SET entrega = 'ENVIO_ROSARIO' WHERE entrega = 'ENVIO';
ALTER TABLE pedidos ADD CONSTRAINT ck_pedidos_entrega
    CHECK (entrega IN ('RETIRO', 'ENVIO_ROSARIO', 'ENVIO_OTRA_LOCALIDAD'));

-- Ajustes de la tienda de cada taller. Por ahora, el precio del armado y prueba de una PC de Armá tu PC
-- (NULL = el taller no ofrece el armado). Se va a poder editar desde el sistema.
CREATE TABLE ajustes_tienda (
    id                UUID    PRIMARY KEY,
    taller_id         UUID    NOT NULL UNIQUE REFERENCES talleres (id),
    precio_armado_pc  BIGINT  CHECK (precio_armado_pc > 0)
);

INSERT INTO ajustes_tienda (id, taller_id, precio_armado_pc)
VALUES ('6f0c1a52-3d1e-4b7a-9c55-000000000002', '00000000-0000-0000-0000-000000000001', 30000);
