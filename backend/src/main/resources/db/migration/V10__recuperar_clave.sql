-- "Olvidé mi contraseña" de la tienda. Ver ADR-012.

CREATE TABLE recuperaciones_de_clave (
    id          UUID         PRIMARY KEY,
    taller_id   UUID         NOT NULL REFERENCES talleres (id),
    cuenta_id   UUID         NOT NULL REFERENCES cuentas_clientes (id),
    -- SHA-256 del token, en hexadecimal. El token en sí viaja solo en el mail: si alguien lee esta tabla
    -- (o un backup), no puede usar los links.
    token_hash  CHAR(64)     NOT NULL UNIQUE,
    creada_en   TIMESTAMPTZ  NOT NULL,
    vence_en    TIMESTAMPTZ  NOT NULL,
    -- Se usa una sola vez. También se marca al pedir un link nuevo (el anterior deja de servir).
    usada_en    TIMESTAMPTZ
);

CREATE INDEX ix_recuperaciones_cuenta ON recuperaciones_de_clave (taller_id, cuenta_id);
