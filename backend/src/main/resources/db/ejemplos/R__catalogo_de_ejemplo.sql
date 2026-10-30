-- DATOS DE EJEMPLO para desarrollo. Solo se cargan con el perfil "dev" (ver application.yml).
-- Son los mismos productos y componentes que usa la web mientras no está conectada a la API.
--
-- ¡LOS PRECIOS SON INVENTADOS! Sirven para ver todo funcionando. Los reales se cargan desde el sistema.
--
-- Es una migración "repetible" (R__): Flyway la vuelve a correr si el archivo cambia.
-- ON CONFLICT DO NOTHING hace que no pise nada que ya exista (por ejemplo, un precio que cambiaste).

INSERT INTO categorias (id, taller_id, slug, nombre, con_instalacion, orden) VALUES
    (gen_random_uuid(), '00000000-0000-0000-0000-000000000001', 'camaras', 'Cámaras', TRUE, 0),
    (gen_random_uuid(), '00000000-0000-0000-0000-000000000001', 'alarmas', 'Alarmas', TRUE, 1),
    (gen_random_uuid(), '00000000-0000-0000-0000-000000000001', 'notebooks', 'Notebooks', FALSE, 2),
    (gen_random_uuid(), '00000000-0000-0000-0000-000000000001', 'pc', 'PC armadas', FALSE, 3),
    (gen_random_uuid(), '00000000-0000-0000-0000-000000000001', 'componentes', 'Componentes', FALSE, 4)
ON CONFLICT (taller_id, slug) DO NOTHING;

INSERT INTO productos (id, taller_id, categoria_id, slug, nombre, condicion, precio, imagen, resumen, descripcion, caracteristicas, garantia_meses, orden)
SELECT gen_random_uuid(), '00000000-0000-0000-0000-000000000001', c.id, v.slug, v.nombre, v.condicion, v.precio, v.imagen, v.resumen, v.descripcion,
       v.caracteristicas, v.garantia_meses, v.orden
FROM (VALUES
    ('kit-2-camaras-dvr', 'camaras', 'Kit 2 cámaras + DVR', 'NUEVO', 280000, '/productos/kit-2-camaras-dvr.jpg', 'Full HD, visión nocturna y acceso desde el celular.', 'Un kit para empezar a cuidar tu casa o tu local: dos cámaras y un grabador que guarda todo lo que pasa, de día y de noche.

Te lo instalamos, lo dejamos configurado en tu celular y te enseñamos a revisar las grabaciones.', ARRAY['2 cámaras Full HD', 'DVR con grabación continua', 'Visión nocturna', 'Acceso desde el celular', 'Instalación a domicilio'], NULL, 0),
    ('kit-4-camaras-dvr', 'camaras', 'Kit 4 cámaras + DVR', 'NUEVO', 420000, '/productos/kit-4-camaras-dvr.jpg', 'Para cubrir casa o negocio completo, de día y de noche.', 'Cuatro cámaras para cubrir el frente, el fondo y los accesos de una casa o un negocio, con un grabador que guarda las imágenes para revisarlas cuando quieras.

Incluye la instalación y la configuración en tu celular.', ARRAY['4 cámaras Full HD', 'DVR con grabación continua', 'Visión nocturna', 'Acceso desde el celular', 'Instalación a domicilio'], NULL, 1),
    ('kit-alarma-dsc', 'alarmas', 'Kit alarma DSC', 'NUEVO', 390000, '/productos/kit-alarma-dsc.jpg', 'Sensores, sirena y control desde el celular.', 'Una alarma DSC, de las más usadas en hogares y comercios, con sensores para puertas y ambientes, y sirena.

La instalamos y la dejamos lista para que la actives y la desactives desde el celular.', ARRAY['Panel DSC', 'Sensores de movimiento y de apertura', 'Sirena', 'Control desde el celular', 'Instalación a domicilio'], NULL, 2),
    ('kit-alarma-x28', 'alarmas', 'Kit alarma X28', 'NUEVO', 330000, '/productos/kit-alarma-x28.jpg', 'Instalación rápida, sin cables y con aviso inmediato.', 'Una alarma X28 para proteger tu casa o tu local con aviso inmediato ante cualquier intrusión.

La instalación es rápida y te dejamos todo configurado.', ARRAY['Panel X28', 'Sensores inalámbricos', 'Sirena', 'Aviso inmediato', 'Instalación a domicilio'], NULL, 3),
    ('kit-alarma-garnet', 'alarmas', 'Kit alarma inalámbrica Garnet', 'NUEVO', 360000, '/productos/kit-alarma-garnet.jpg', '100% inalámbrica, con controles remotos y sirena.', 'Una alarma totalmente inalámbrica: no hace falta pasar cables ni romper paredes, por eso se instala en poco tiempo y sin obra.

Viene con controles remotos para activarla y desactivarla.', ARRAY['100% inalámbrica', 'Controles remotos', 'Sirena', 'Sin obra ni cables', 'Instalación a domicilio'], NULL, 4),
    ('sistema-integral-sc', 'alarmas', 'Alarma y cámaras integradas', 'NUEVO', 850000, '/productos/sistema-integral-sc.jpg', 'Alarma DSC y cámaras Hikvision en un solo sistema.', 'Alarma y cámaras funcionando como un solo sistema: si se dispara la alarma, mirás en el momento qué está pasando.

Ideal para comercios y casas que quieren la protección más completa.', ARRAY['Alarma DSC', 'Cámaras Hikvision', 'Todo desde una sola app', 'Instalación a domicilio'], NULL, 5),
    ('notebook-gamer', 'notebooks', 'Notebook gamer', 'NUEVO', 1650000, '/productos/notebook-gamer.jpg', 'Para jugar y para diseño avanzado.', 'Una notebook con la potencia para jugar y para programas pesados de diseño o edición.

Te la entregamos revisada, con el sistema instalado y lista para usar.', ARRAY['Placa de video dedicada', 'Pantalla de alta frecuencia', 'Sistema instalado', 'Garantía de 12 meses'], 12, 6),
    ('notebook-nueva', 'notebooks', 'Notebook para trabajo', 'NUEVO', 950000, '/productos/notebook-nueva.jpg', 'Rendimiento para software profesional y multitarea.', 'Una notebook pensada para trabajar: rápida para el día a día, con varias aplicaciones abiertas a la vez.

Te la entregamos con el sistema instalado y lista para usar.', ARRAY['Ideal para trabajo y multitarea', 'Sistema instalado', 'Garantía de 12 meses'], 12, 7),
    ('notebook-reacondicionada', 'notebooks', 'Notebook reacondicionada', 'REACONDICIONADO', 450000, '/productos/notebook-reacondicionada.jpg', 'Ideal para estudiar, revisada y lista para usar.', 'Una notebook usada que revisamos por completo, limpiamos y probamos antes de venderla. Es la opción más económica para estudiar o para el uso de todos los días.

Tiene garantía, como un equipo nuevo.', ARRAY['Revisada y probada en nuestro taller', 'Limpieza completa', 'Sistema instalado', 'Garantía de 3 meses'], 3, 8),
    ('pc-gamer', 'pc', 'PC gamer', 'NUEVO', 1350000, '/productos/pc-gamer.jpg', 'Armada, probada y con el sistema instalado.', 'Una PC armada por nosotros, con componentes elegidos para jugar y para trabajar sin trabas.

Si querés elegir cada pieza, probá Armá tu PC.', ARRAY['Armada y probada en nuestro taller', 'Sistema instalado', 'Garantía de 12 meses'], 12, 9)
) AS v (slug, categoria, nombre, condicion, precio, imagen, resumen, descripcion, caracteristicas, garantia_meses, orden)
JOIN categorias c ON c.taller_id = '00000000-0000-0000-0000-000000000001' AND c.slug = v.categoria
ON CONFLICT (taller_id, slug) DO NOTHING;

-- Componentes de Armá tu PC: van en la categoría "componentes", no publicados como productos sueltos
-- todavía (publicado = FALSE), pero sí disponibles para Armá tu PC.
INSERT INTO productos (id, taller_id, categoria_id, slug, nombre, condicion, precio, caracteristicas, tipo_componente,
                       socket, tipo_ram, formato, formatos_soportados, consumo_w, potencia_w, graficos_integrados,
                       orden, publicado)
SELECT gen_random_uuid(), '00000000-0000-0000-0000-000000000001', c.id, v.slug, v.nombre, 'NUEVO', v.precio, v.caracteristicas, v.tipo_componente,
       v.socket, v.tipo_ram, v.formato, v.formatos_soportados, v.consumo_w, v.potencia_w, v.graficos_integrados,
       v.orden, FALSE
FROM (VALUES
    ('ryzen-5-5600', 'AMD Ryzen 5 5600', 165000, ARRAY['6 núcleos', '12 hilos', 'Socket AM4'], 'CPU', 'AM4', NULL, NULL, '{}', 65::int, NULL::int, FALSE, 100),
    ('ryzen-5-8600g', 'AMD Ryzen 5 8600G', 290000, ARRAY['6 núcleos', 'Gráficos integrados', 'Socket AM5'], 'CPU', 'AM5', NULL, NULL, '{}', 65::int, NULL::int, TRUE, 101),
    ('ryzen-7-7800x3d', 'AMD Ryzen 7 7800X3D', 620000, ARRAY['8 núcleos', 'Ideal para juegos', 'Socket AM5'], 'CPU', 'AM5', NULL, NULL, '{}', 120::int, NULL::int, FALSE, 102),
    ('core-i3-12100', 'Intel Core i3-12100', 150000, ARRAY['4 núcleos', 'Gráficos integrados', 'Socket LGA1700'], 'CPU', 'LGA1700', NULL, NULL, '{}', 60::int, NULL::int, TRUE, 103),
    ('core-i5-12400f', 'Intel Core i5-12400F', 190000, ARRAY['6 núcleos', '12 hilos', 'Socket LGA1700'], 'CPU', 'LGA1700', NULL, NULL, '{}', 65::int, NULL::int, FALSE, 104),
    ('a520m', 'Mother A520M', 115000, ARRAY['Socket AM4', 'DDR4', 'Micro ATX'], 'MOTHER', 'AM4', 'DDR4', 'MATX', '{}', NULL::int, NULL::int, FALSE, 105),
    ('b550', 'Mother B550', 185000, ARRAY['Socket AM4', 'DDR4', 'ATX'], 'MOTHER', 'AM4', 'DDR4', 'ATX', '{}', NULL::int, NULL::int, FALSE, 106),
    ('a620m', 'Mother A620M', 170000, ARRAY['Socket AM5', 'DDR5', 'Micro ATX'], 'MOTHER', 'AM5', 'DDR5', 'MATX', '{}', NULL::int, NULL::int, FALSE, 107),
    ('b650', 'Mother B650', 260000, ARRAY['Socket AM5', 'DDR5', 'ATX'], 'MOTHER', 'AM5', 'DDR5', 'ATX', '{}', NULL::int, NULL::int, FALSE, 108),
    ('h610m', 'Mother H610M', 120000, ARRAY['Socket LGA1700', 'DDR4', 'Micro ATX'], 'MOTHER', 'LGA1700', 'DDR4', 'MATX', '{}', NULL::int, NULL::int, FALSE, 109),
    ('b760m-ddr5', 'Mother B760M DDR5', 210000, ARRAY['Socket LGA1700', 'DDR5', 'Micro ATX'], 'MOTHER', 'LGA1700', 'DDR5', 'MATX', '{}', NULL::int, NULL::int, FALSE, 110),
    ('ddr4-8gb', 'Memoria 8 GB DDR4 3200', 35000, ARRAY['8 GB', 'DDR4', '3200 MHz'], 'RAM', NULL, 'DDR4', NULL, '{}', NULL::int, NULL::int, FALSE, 111),
    ('ddr4-16gb', 'Memoria 16 GB (2×8) DDR4 3200', 68000, ARRAY['16 GB', 'Doble canal', 'DDR4'], 'RAM', NULL, 'DDR4', NULL, '{}', NULL::int, NULL::int, FALSE, 112),
    ('ddr5-16gb', 'Memoria 16 GB DDR5 5600', 80000, ARRAY['16 GB', 'DDR5', '5600 MHz'], 'RAM', NULL, 'DDR5', NULL, '{}', NULL::int, NULL::int, FALSE, 113),
    ('ddr5-32gb', 'Memoria 32 GB (2×16) DDR5 6000', 160000, ARRAY['32 GB', 'Doble canal', 'DDR5'], 'RAM', NULL, 'DDR5', NULL, '{}', NULL::int, NULL::int, FALSE, 114),
    ('rtx-3050', 'Placa de video RTX 3050 6 GB', 280000, ARRAY['6 GB', 'Juegos en 1080p'], 'GPU', NULL, NULL, NULL, '{}', 70::int, NULL::int, FALSE, 115),
    ('rtx-4060', 'Placa de video RTX 4060 8 GB', 430000, ARRAY['8 GB', 'Juegos en 1080p alto'], 'GPU', NULL, NULL, NULL, '{}', 115::int, NULL::int, FALSE, 116),
    ('rx-7600', 'Placa de video RX 7600 8 GB', 400000, ARRAY['8 GB', 'Juegos en 1080p alto'], 'GPU', NULL, NULL, NULL, '{}', 165::int, NULL::int, FALSE, 117),
    ('rtx-4070-super', 'Placa de video RTX 4070 Super 12 GB', 950000, ARRAY['12 GB', 'Juegos en 1440p'], 'GPU', NULL, NULL, NULL, '{}', 220::int, NULL::int, FALSE, 118),
    ('ssd-nvme-500gb', 'Disco SSD NVMe 500 GB', 55000, ARRAY['500 GB', 'NVMe M.2'], 'ALMACENAMIENTO', NULL, NULL, NULL, '{}', NULL::int, NULL::int, FALSE, 119),
    ('ssd-nvme-1tb', 'Disco SSD NVMe 1 TB', 95000, ARRAY['1 TB', 'NVMe M.2'], 'ALMACENAMIENTO', NULL, NULL, NULL, '{}', NULL::int, NULL::int, FALSE, 120),
    ('ssd-sata-480gb', 'Disco SSD SATA 480 GB', 42000, ARRAY['480 GB', 'SATA'], 'ALMACENAMIENTO', NULL, NULL, NULL, '{}', NULL::int, NULL::int, FALSE, 121),
    ('fuente-500w', 'Fuente 500 W 80 Plus', 60000, ARRAY['500 W', '80 Plus'], 'FUENTE', NULL, NULL, NULL, '{}', NULL::int, 500::int, FALSE, 122),
    ('fuente-650w', 'Fuente 650 W 80 Plus Bronze', 90000, ARRAY['650 W', '80 Plus Bronze'], 'FUENTE', NULL, NULL, NULL, '{}', NULL::int, 650::int, FALSE, 123),
    ('fuente-750w', 'Fuente 750 W 80 Plus Gold', 140000, ARRAY['750 W', '80 Plus Gold', 'Modular'], 'FUENTE', NULL, NULL, NULL, '{}', NULL::int, 750::int, FALSE, 124),
    ('fuente-850w', 'Fuente 850 W 80 Plus Gold', 175000, ARRAY['850 W', '80 Plus Gold', 'Modular'], 'FUENTE', NULL, NULL, NULL, '{}', NULL::int, 850::int, FALSE, 125),
    ('gabinete-matx', 'Gabinete Micro ATX', 55000, ARRAY['Micro ATX', '2 ventiladores'], 'GABINETE', NULL, NULL, NULL, ARRAY['MATX'], NULL::int, NULL::int, FALSE, 126),
    ('gabinete-atx-vidrio', 'Gabinete ATX con vidrio templado', 95000, ARRAY['ATX y Micro ATX', 'Vidrio templado', '4 ventiladores RGB'], 'GABINETE', NULL, NULL, NULL, ARRAY['ATX', 'MATX'], NULL::int, NULL::int, FALSE, 127),
    ('gabinete-atx-flujo', 'Gabinete ATX alto flujo de aire', 130000, ARRAY['ATX y Micro ATX', 'Frente mallado', '6 ventiladores'], 'GABINETE', NULL, NULL, NULL, ARRAY['ATX', 'MATX'], NULL::int, NULL::int, FALSE, 128)
) AS v (slug, nombre, precio, caracteristicas, tipo_componente, socket, tipo_ram, formato, formatos_soportados,
        consumo_w, potencia_w, graficos_integrados, orden)
JOIN categorias c ON c.taller_id = '00000000-0000-0000-0000-000000000001' AND c.slug = 'componentes'
ON CONFLICT (taller_id, slug) DO NOTHING;
