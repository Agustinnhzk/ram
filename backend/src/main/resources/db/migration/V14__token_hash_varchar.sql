-- Corrige V10: token_hash era CHAR(64) y la entidad (RecuperacionJpaEntity) lo mapea como VARCHAR(64).
-- Hibernate valida el esquema al arrancar (ddl-auto: validate) y no acepta CHAR por VARCHAR.
-- No se toca V10: ya corrió en las bases existentes y Flyway detectaría el cambio (checksum distinto).
-- El hash SHA-256 en hexadecimal mide siempre 64 caracteres: el cambio no altera ningún dato.
ALTER TABLE recuperaciones_de_clave ALTER COLUMN token_hash TYPE VARCHAR(64);
