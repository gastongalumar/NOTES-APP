CREATE TABLE roles (
    id SMALLINT NOT NULL,
    nombre VARCHAR(20) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_roles_nombre (nombre)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

INSERT INTO roles (id, nombre) VALUES (1, 'USER'), (2, 'ADMIN');

CREATE TABLE usuario_roles (
    usuario_id BIGINT NOT NULL,
    role_id SMALLINT NOT NULL,
    PRIMARY KEY (usuario_id, role_id),
    KEY idx_usuario_roles_role (role_id),
    CONSTRAINT fk_usuario_roles_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios (id) ON DELETE CASCADE,
    CONSTRAINT fk_usuario_roles_role FOREIGN KEY (role_id) REFERENCES roles (id) ON DELETE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

INSERT INTO usuario_roles (usuario_id, role_id)
SELECT u.id, 1 FROM usuarios u
WHERE NOT EXISTS (SELECT 1 FROM usuario_roles ur WHERE ur.usuario_id = u.id AND ur.role_id = 1);

INSERT INTO usuario_roles (usuario_id, role_id)
SELECT u.id, 2 FROM usuarios u
WHERE u.username = 'admin'
  AND NOT EXISTS (SELECT 1 FROM usuario_roles ur WHERE ur.usuario_id = u.id AND ur.role_id = 2);
