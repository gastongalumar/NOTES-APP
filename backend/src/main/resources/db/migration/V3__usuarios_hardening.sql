ALTER TABLE usuarios
    ADD COLUMN nombre VARCHAR(80) NULL AFTER email,
    ADD COLUMN failed_attempts SMALLINT NOT NULL DEFAULT 0 AFTER enabled,
    ADD COLUMN locked_until DATETIME(6) NULL AFTER failed_attempts,
    ADD COLUMN created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) AFTER locked_until,
    ADD COLUMN updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) AFTER created_at,
    ADD COLUMN deleted_at DATETIME(6) NULL AFTER updated_at;

ALTER TABLE usuarios CHANGE COLUMN password password_hash VARCHAR(100) NOT NULL;

CREATE INDEX idx_usuarios_deleted_at ON usuarios (deleted_at);
