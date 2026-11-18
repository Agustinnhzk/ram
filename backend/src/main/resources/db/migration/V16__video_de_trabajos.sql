-- Un video opcional por trabajo realizado (ADR-018): el link de un reel de Instagram o de un video de YouTube.
-- El video lo aloja Instagram o YouTube; acá se guarda solo el link, ya limpio (ver dominio/Video.java).
ALTER TABLE trabajos ADD COLUMN video VARCHAR(200);
