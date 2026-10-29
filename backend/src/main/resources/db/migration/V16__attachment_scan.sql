ALTER TABLE adjuntos
    ADD COLUMN scan_status VARCHAR(20) NULL AFTER estado,
    ADD COLUMN scan_engine VARCHAR(50) NULL AFTER scan_status,
    ADD COLUMN scan_error VARCHAR(500) NULL AFTER scan_engine,
    ADD COLUMN scanned_at DATETIME(6) NULL AFTER scan_error,
    ADD KEY idx_adjuntos_scan_status (scan_status);
