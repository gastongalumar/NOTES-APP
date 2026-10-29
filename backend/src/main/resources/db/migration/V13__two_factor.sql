CREATE TABLE two_factor (
    usuario_id BIGINT NOT NULL,
    secret_encrypted VARCHAR(255) NOT NULL,
    recovery_codes_hash JSON NULL,
    enabled_at DATETIME(6) NULL,
    PRIMARY KEY (usuario_id),
    CONSTRAINT fk_two_factor_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios (id) ON DELETE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;
