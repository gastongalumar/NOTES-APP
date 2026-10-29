-- Solo se carga desde application-dev.yml (classpath:db/seed). Nunca en prod.
INSERT INTO usuarios (username, password_hash, email, nombre, enabled, failed_attempts, created_at, updated_at)
VALUES ('admin', '$2b$10$FMe70a5m5tW4vPVOqiQNl.k1pkZ.D9NistcZwk9jEIpq3h6ypqFna', 'admin@notasapp.local', 'Administrador', b'1', 0, CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6))
ON DUPLICATE KEY UPDATE username = VALUES(username);

INSERT INTO usuarios (username, password_hash, email, nombre, enabled, failed_attempts, created_at, updated_at)
VALUES ('usuario', '$2b$10$9vtU.vlDNpFlsWVlLbYkHuLq5NBdnSwW2W5Sb9g5yam491rRFMJxG', 'usuario@notasapp.local', 'Usuario Demo', b'1', 0, CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6))
ON DUPLICATE KEY UPDATE username = VALUES(username);

INSERT INTO usuario_roles (usuario_id, role_id)
SELECT u.id, r.id FROM usuarios u JOIN roles r ON r.nombre = 'ADMIN'
WHERE u.username = 'admin'
  AND NOT EXISTS (SELECT 1 FROM usuario_roles ur WHERE ur.usuario_id = u.id AND ur.role_id = r.id);

INSERT INTO usuario_roles (usuario_id, role_id)
SELECT u.id, r.id FROM usuarios u JOIN roles r ON r.nombre = 'USER'
WHERE u.username IN ('admin', 'usuario')
  AND NOT EXISTS (SELECT 1 FROM usuario_roles ur WHERE ur.usuario_id = u.id AND ur.role_id = r.id);

INSERT INTO categorias (usuario_id, nombre, color, descripcion, created_at, updated_at)
SELECT u.id, 'General', '#b31224', 'Notas de carácter general', CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6)
FROM usuarios u
WHERE u.username IN ('admin', 'usuario')
  AND NOT EXISTS (SELECT 1 FROM categorias c WHERE c.usuario_id = u.id AND c.nombre = 'General');

INSERT INTO categorias (usuario_id, nombre, color, descripcion, created_at, updated_at)
SELECT u.id, 'Personal', '#1f6f4a', 'Notas personales', CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6)
FROM usuarios u
WHERE u.username IN ('admin', 'usuario')
  AND NOT EXISTS (SELECT 1 FROM categorias c WHERE c.usuario_id = u.id AND c.nombre = 'Personal');

INSERT INTO notas (titulo, contenido, created_at, updated_at, archivada, usuario_id, version, prioridad, estado)
SELECT 'Bienvenido a NotasApp', 'Esta es una nota de demostración del entorno de desarrollo.', CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6), b'0', u.id, 0, 2, 'PENDIENTE'
FROM usuarios u
WHERE u.username = 'admin'
  AND NOT EXISTS (SELECT 1 FROM notas n WHERE n.usuario_id = u.id AND n.titulo = 'Bienvenido a NotasApp');

UPDATE usuarios SET email_verified = b'1' WHERE username IN ('admin', 'usuario');

INSERT INTO nota_categoria (nota_id, categoria_id)
SELECT n.id, c.id
FROM notas n
JOIN usuarios u ON u.id = n.usuario_id
JOIN categorias c ON c.usuario_id = u.id AND c.nombre = 'General'
WHERE n.titulo = 'Bienvenido a NotasApp'
  AND NOT EXISTS (SELECT 1 FROM nota_categoria nc WHERE nc.nota_id = n.id AND nc.categoria_id = c.id);
