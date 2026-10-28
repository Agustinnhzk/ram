-- Registro de eventos entre módulos de Spring Modulith (ADR-001 regla 3, ADR-004).
--
-- Cuando un módulo publica un evento (por ejemplo, "orden pagada") y otro lo escucha
-- ("descontar stock"), Spring Modulith guarda acá cada entrega pendiente. Si la aplicación
-- se cae antes de que el que escucha termine, el evento no se pierde: queda registrado
-- y se puede reintentar.
--
-- Esta tabla es de la infraestructura, no de un taller: no lleva taller_id.
-- Esquema tomado de Spring Modulith 2.x (schemas/v2/schema-postgresql.sql).

CREATE TABLE event_publication (
    id                      UUID                     NOT NULL,
    listener_id             TEXT                     NOT NULL,
    event_type              TEXT                     NOT NULL,
    serialized_event        TEXT                     NOT NULL,
    publication_date        TIMESTAMP WITH TIME ZONE NOT NULL,
    completion_date         TIMESTAMP WITH TIME ZONE,
    status                  TEXT,
    completion_attempts     INT,
    last_resubmission_date  TIMESTAMP WITH TIME ZONE,
    PRIMARY KEY (id)
);

CREATE INDEX event_publication_serialized_event_hash_idx ON event_publication USING hash (serialized_event);
CREATE INDEX event_publication_by_completion_date_idx ON event_publication (completion_date);