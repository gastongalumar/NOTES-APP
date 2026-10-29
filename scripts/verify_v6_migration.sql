-- Verificación de invariantes de V6 (categorías por usuario).
-- Ejecutar contra la DB migrada. Falla con SIGNAL si encuentra datos inconsistentes.
DELIMITER //

CREATE PROCEDURE verify_v6_migration()
BEGIN
    IF EXISTS (SELECT 1 FROM categorias WHERE usuario_id IS NULL) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'V6: categorías sin usuario_id';
    END IF;

    IF EXISTS (
        SELECT 1
        FROM nota_categoria nc
        JOIN notas n ON n.id = nc.nota_id
        JOIN categorias c ON c.id = nc.categoria_id
        WHERE n.usuario_id <> c.usuario_id
    ) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'V6: nota_categoria cruza dueños distintos';
    END IF;

    IF EXISTS (
        SELECT 1
        FROM categorias
        WHERE deleted_at IS NULL
        GROUP BY usuario_id, nombre
        HAVING COUNT(*) > 1
    ) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'V6: categorías duplicadas por usuario';
    END IF;

    IF EXISTS (
        SELECT 1
        FROM nota_categoria nc
        LEFT JOIN notas n ON n.id = nc.nota_id
        LEFT JOIN categorias c ON c.id = nc.categoria_id
        WHERE n.id IS NULL OR c.id IS NULL
    ) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'V6: nota_categoria con referencias huérfanas';
    END IF;
END//

DELIMITER ;

CALL verify_v6_migration();
DROP PROCEDURE verify_v6_migration;
