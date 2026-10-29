CREATE TABLE tags (
    id BIGINT NOT NULL AUTO_INCREMENT,
    usuario_id BIGINT NOT NULL,
    nombre VARCHAR(50) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    deleted_at DATETIME(6) NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_tags_usuario_nombre (usuario_id, nombre),
    KEY idx_tags_usuario (usuario_id),
    CONSTRAINT fk_tags_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios (id) ON DELETE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE nota_tag (
    nota_id BIGINT NOT NULL,
    tag_id BIGINT NOT NULL,
    PRIMARY KEY (nota_id, tag_id),
    KEY idx_nota_tag_tag (tag_id),
    CONSTRAINT fk_nota_tag_nota FOREIGN KEY (nota_id) REFERENCES notas (id) ON DELETE CASCADE,
    CONSTRAINT fk_nota_tag_tag FOREIGN KEY (tag_id) REFERENCES tags (id) ON DELETE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;
