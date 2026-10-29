CREATE TABLE audit_log (
    id BIGINT NOT NULL AUTO_INCREMENT,
    usuario_id BIGINT NULL,
    accion VARCHAR(50) NOT NULL,
    entidad VARCHAR(50) NULL,
    entidad_id BIGINT NULL,
    ip VARCHAR(45) NULL,
    user_agent VARCHAR(255) NULL,
    detalle JSON NULL,
    created_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    KEY idx_audit_usuario_created (usuario_id, created_at),
    KEY idx_audit_entidad (entidad, entidad_id),
    KEY idx_audit_accion_created (accion, created_at),
    CONSTRAINT fk_audit_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios (id) ON DELETE SET NULL
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;
