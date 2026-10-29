ALTER TABLE categorias
    ADD COLUMN usuario_id BIGINT NOT NULL AFTER id,
    ADD COLUMN created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) AFTER descripcion,
    ADD COLUMN updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) AFTER created_at,
    ADD COLUMN deleted_at DATETIME(6) NULL AFTER updated_at,
    DROP INDEX uk_categorias_nombre,
    ADD UNIQUE KEY uk_categorias_usuario_nombre (usuario_id, nombre),
    ADD KEY idx_categorias_usuario (usuario_id),
    ADD CONSTRAINT fk_categorias_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios (id) ON DELETE CASCADE;
