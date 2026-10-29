CREATE TABLE shared_links (
    id BIGINT NOT NULL AUTO_INCREMENT,
    nota_id BIGINT NOT NULL,
    token_hash CHAR(64) NOT NULL,
    expires_at DATETIME(6) NOT NULL,
    revoked_at DATETIME(6) NULL,
    created_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_shared_token_hash (token_hash),
    KEY idx_shared_nota (nota_id),
    CONSTRAINT fk_shared_nota FOREIGN KEY (nota_id) REFERENCES notas (id) ON DELETE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;
