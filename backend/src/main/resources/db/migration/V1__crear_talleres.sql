-- Talleres (tenants) de la plataforma. Ver ADR-002.
-- Esta tabla NO lleva tenant_id: es la que define a los tenants.

CREATE TABLE talleres (
    id         UUID         PRIMARY KEY,
    nombre     VARCHAR(120) NOT NULL,
    slug       VARCHAR(63)  NOT NULL UNIQUE,
    estado     VARCHAR(20)  NOT NULL CHECK (estado IN ('ACTIVO', 'SUSPENDIDO')),
    creado_en  TIMESTAMPTZ  NOT NULL DEFAULT now()
);

-- Dominios propios de cada taller (ramservicios.com.ar).
-- El dominio es la clave primaria: un mismo dominio nunca puede apuntar a dos talleres.
CREATE TABLE talleres_dominios (
    dominio    VARCHAR(253) PRIMARY KEY,
    taller_id  UUID         NOT NULL REFERENCES talleres (id) ON DELETE CASCADE
);

CREATE INDEX ix_talleres_dominios_taller ON talleres_dominios (taller_id);
