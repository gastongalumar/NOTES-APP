ALTER TABLE usuarios ADD COLUMN email_verified BIT(1) NOT NULL DEFAULT b'0' AFTER enabled;
UPDATE usuarios SET email_verified = b'1' WHERE username IN ('admin', 'usuario');
