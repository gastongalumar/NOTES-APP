ALTER TABLE notas
    CHANGE COLUMN fecha_creacion created_at DATETIME(6) NOT NULL,
    CHANGE COLUMN fecha_actualizacion updated_at DATETIME(6) NOT NULL,
    ADD COLUMN version BIGINT NOT NULL DEFAULT 0 AFTER archivada,
    ADD COLUMN deleted_at DATETIME(6) NULL AFTER version;

CREATE INDEX idx_notas_usuario_archivada_deleted ON notas (usuario_id, archivada, deleted_at);
CREATE INDEX idx_notas_usuario_updated ON notas (usuario_id, updated_at);
