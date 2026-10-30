-- Catálogo editable desde el sistema: ofertas, fotos de productos y banners de la portada. Ver ADR-013.

-- Oferta: un precio más bajo hasta un día (inclusive, fecha de Argentina). Los dos juntos o ninguno.
ALTER TABLE productos
    ADD COLUMN precio_oferta BIGINT CHECK (precio_oferta > 0),
    ADD COLUMN oferta_hasta  DATE,
    ADD CONSTRAINT ck_productos_oferta CHECK ((precio_oferta IS NULL) = (oferta_hasta IS NULL));

-- Fotos de productos. Como los comprobantes (V8): los datos por un lado y los bytes por otro,
-- así el catálogo lista las fotos sin cargar imágenes.
CREATE TABLE fotos_productos (
    id           UUID         PRIMARY KEY,
    taller_id    UUID         NOT NULL REFERENCES talleres (id),
    producto_id  UUID         NOT NULL REFERENCES productos (id),
    -- 0 = la principal.
    posicion     INT          NOT NULL CHECK (posicion >= 0),
    ancho        INT          NOT NULL,
    alto         INT          NOT NULL,
    subida_en    TIMESTAMPTZ  NOT NULL
);

CREATE INDEX ix_fotos_producto ON fotos_productos (taller_id, producto_id, posicion);

-- Dos tamaños ya preparados (JPEG): la grande (1600 px) para la página del producto, la chica (600 px) para las tarjetas.
CREATE TABLE archivos_fotos (
    foto_id    UUID   PRIMARY KEY REFERENCES fotos_productos (id),
    taller_id  UUID   NOT NULL REFERENCES talleres (id),
    grande     BYTEA  NOT NULL,
    chica      BYTEA  NOT NULL
);

-- Banners (flyers) del carrusel de la portada.
CREATE TABLE banners (
    id                 UUID          PRIMARY KEY,
    taller_id          UUID          NOT NULL REFERENCES talleres (id),
    -- Lo que dice el flyer, en palabras (para lectores de pantalla y Google).
    texto_alternativo  VARCHAR(200)  NOT NULL,
    -- Una ruta de la tienda: /productos?categoria=camaras. Nunca otro sitio (lo controla el dominio).
    enlace             VARCHAR(300)  NOT NULL,
    -- Una imagen que ya está en la web (los datos de ejemplo). NULL = se subió una desde el sistema.
    imagen_heredada    VARCHAR(300),
    -- Cambia al subir otra imagen; va en la URL para que el navegador no muestre la vieja.
    imagen_version     BIGINT        NOT NULL DEFAULT 0,
    activo             BOOLEAN       NOT NULL DEFAULT TRUE,
    desde              DATE,
    hasta              DATE,
    orden              INT           NOT NULL DEFAULT 0,
    CONSTRAINT ck_banners_fechas CHECK (desde IS NULL OR hasta IS NULL OR desde <= hasta)
);

CREATE INDEX ix_banners_taller_orden ON banners (taller_id, orden);

CREATE TABLE imagenes_banners (
    banner_id  UUID   PRIMARY KEY REFERENCES banners (id),
    taller_id  UUID   NOT NULL REFERENCES talleres (id),
    contenido  BYTEA  NOT NULL
);

-- Los flyers que ya tiene RAM (las imágenes están en la web, en /promos). Son reales, no de ejemplo:
-- van en la migración. Desde el sistema se pueden reemplazar, editar, ordenar o borrar.
INSERT INTO banners (id, taller_id, texto_alternativo, enlace, imagen_heredada, orden) VALUES
    ('6f0c1a52-3d1e-4b7a-9c55-0000000000b1', '00000000-0000-0000-0000-000000000001',
     'Cámaras y alarmas con stock inmediato, de exterior e interior, inalámbricas y cableadas', '/productos?categoria=camaras', '/promos/camaras-y-alarmas.jpg', 0),
    ('6f0c1a52-3d1e-4b7a-9c55-0000000000b2', '00000000-0000-0000-0000-000000000001',
     'Reparación de notebooks y PC a domicilio, con retiro y entrega, y sin costo si no se logra reparar', '/servicios#reparacion', '/promos/reparacion.jpg', 1),
    ('6f0c1a52-3d1e-4b7a-9c55-0000000000b3', '00000000-0000-0000-0000-000000000001',
     'Armado de PC a medida para gaming, trabajo o estudio, lista para usar', '/arma-tu-pc', '/promos/armado-de-pc.jpg', 2),
    ('6f0c1a52-3d1e-4b7a-9c55-0000000000b4', '00000000-0000-0000-0000-000000000001',
     'Alarmas inalámbricas X28 y DSC: protección sin cables', '/productos?categoria=alarmas', '/promos/alarmas-inalambricas.jpg', 3),
    ('6f0c1a52-3d1e-4b7a-9c55-0000000000b5', '00000000-0000-0000-0000-000000000001',
     'Kit de 4 cámaras Full HD con visión nocturna y acceso desde el celular', '/productos?categoria=camaras', '/promos/kit-4-camaras.jpg', 4),
    ('6f0c1a52-3d1e-4b7a-9c55-0000000000b6', '00000000-0000-0000-0000-000000000001',
     'Seguridad inteligente para hogar, empresa y tus datos, con monitoreo en tiempo real', '/servicios#instalaciones', '/promos/seguridad-inteligente.jpg', 5);
