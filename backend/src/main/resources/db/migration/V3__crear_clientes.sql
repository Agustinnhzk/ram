-- Clientes de cada taller. Primera tabla con tenant_id (taller_id). Ver ADR-005.

CREATE TABLE clientes (
    id               UUID         PRIMARY KEY,
    taller_id        UUID         NOT NULL REFERENCES talleres (id),
    nombre_completo  VARCHAR(150) NOT NULL,
    -- "Agustín  Pérez" → "agustin perez": lo calcula la aplicación, para buscar sin tildes ni mayúsculas.
    nombre_busqueda  VARCHAR(150) NOT NULL,
    telefono         VARCHAR(25)  NOT NULL,
    correo           VARCHAR(254),
    creado_en        TIMESTAMPTZ  NOT NULL DEFAULT now()
);

-- Toda búsqueda filtra primero por taller y después por nombre: el índice sigue ese orden.
CREATE INDEX ix_clientes_taller_nombre ON clientes (taller_id, nombre_busqueda);
