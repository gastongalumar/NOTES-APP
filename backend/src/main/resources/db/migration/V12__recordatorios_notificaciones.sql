CREATE TABLE recordatorios (
    id BIGINT NOT NULL AUTO_INCREMENT,
    nota_id BIGINT NOT NULL,
    usuario_id BIGINT NOT NULL,
    recordar_at DATETIME(6) NOT NULL,
    enviado_at DATETIME(6) NULL,
    estado VARCHAR(20) NOT NULL DEFAULT 'PENDIENTE',
    created_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    KEY idx_recordatorios_pendientes (recordar_at, estado),
    KEY idx_recordatorios_usuario (usuario_id, recordar_at),
    CONSTRAINT fk_recordatorios_nota FOREIGN KEY (nota_id) REFERENCES notas (id) ON DELETE CASCADE,
    CONSTRAINT fk_recordatorios_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios (id) ON DELETE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE notificaciones (
    id BIGINT NOT NULL AUTO_INCREMENT,
    usuario_id BIGINT NOT NULL,
    tipo VARCHAR(40) NOT NULL,
    payload JSON NULL,
    leida_at DATETIME(6) NULL,
    created_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    KEY idx_notificaciones_usuario (usuario_id, leida_at),
    CONSTRAINT fk_notificaciones_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios (id) ON DELETE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;
