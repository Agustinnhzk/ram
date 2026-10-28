-- Usuarios y sesiones. Ver ADR-006.

-- Personal del taller (Dueño, Técnico, Mostrador).
CREATE TABLE usuarios_personal (
    id             UUID         PRIMARY KEY,
    taller_id      UUID         NOT NULL REFERENCES talleres (id),
    nombre         VARCHAR(120) NOT NULL,
    correo         VARCHAR(254) NOT NULL,
    -- Hash de BCrypt con prefijo, por ejemplo "{bcrypt}$2a$10$...": ~68 caracteres. Nunca la clave en claro.
    clave_cifrada  VARCHAR(100) NOT NULL,
    rol            VARCHAR(20)  NOT NULL CHECK (rol IN ('DUENO', 'TECNICO', 'MOSTRADOR')),
    activo         BOOLEAN      NOT NULL DEFAULT TRUE,
    creado_en      TIMESTAMPTZ  NOT NULL DEFAULT now(),
    -- El mismo correo puede trabajar en dos talleres, pero no estar dos veces en el mismo.
    CONSTRAINT uq_personal_taller_correo UNIQUE (taller_id, correo)
);

-- Cuentas de clientes en la tienda de cada taller (ADR-002: una cuenta por taller).
CREATE TABLE cuentas_clientes (
    id                 UUID         PRIMARY KEY,
    taller_id          UUID         NOT NULL REFERENCES talleres (id),
    nombre             VARCHAR(120) NOT NULL,
    correo             VARCHAR(254) NOT NULL,
    clave_cifrada      VARCHAR(100) NOT NULL,
    correo_verificado  BOOLEAN      NOT NULL DEFAULT FALSE,
    creado_en          TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT uq_cuentas_taller_correo UNIQUE (taller_id, correo)
);

-- Sesiones HTTP de Spring Session: así una sesión sobrevive a un reinicio de la aplicación,
-- y cerrar sesión o bloquear a alguien es inmediato. Esquema oficial de Spring Session JDBC
-- (org/springframework/session/jdbc/schema-postgresql.sql). Son de infraestructura: sin taller_id.
CREATE TABLE spring_session (
    primary_id             CHAR(36)     NOT NULL,
    session_id             CHAR(36)     NOT NULL,
    creation_time          BIGINT       NOT NULL,
    last_access_time       BIGINT       NOT NULL,
    max_inactive_interval  INT          NOT NULL,
    expiry_time            BIGINT       NOT NULL,
    principal_name         VARCHAR(100),
    CONSTRAINT spring_session_pk PRIMARY KEY (primary_id)
);

CREATE UNIQUE INDEX spring_session_ix1 ON spring_session (session_id);
CREATE INDEX spring_session_ix2 ON spring_session (expiry_time);
CREATE INDEX spring_session_ix3 ON spring_session (principal_name);

CREATE TABLE spring_session_attributes (
    session_primary_id  CHAR(36)     NOT NULL,
    attribute_name      VARCHAR(200) NOT NULL,
    attribute_bytes     BYTEA        NOT NULL,
    CONSTRAINT spring_session_attributes_pk PRIMARY KEY (session_primary_id, attribute_name),
    CONSTRAINT spring_session_attributes_fk FOREIGN KEY (session_primary_id)
        REFERENCES spring_session (primary_id) ON DELETE CASCADE
);
