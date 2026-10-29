ALTER TABLE notas
    ADD COLUMN fecha_vencimiento DATETIME(6) NULL AFTER deleted_at,
    ADD COLUMN prioridad TINYINT NOT NULL DEFAULT 2 AFTER fecha_vencimiento,
    ADD COLUMN estado VARCHAR(20) NOT NULL DEFAULT 'PENDIENTE' AFTER prioridad,
    ADD KEY idx_notas_usuario_vencimiento (usuario_id, fecha_vencimiento);
