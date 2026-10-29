ALTER TABLE garantias
    ADD COLUMN estado VARCHAR(20) NOT NULL DEFAULT 'ACTIVO',
    ADD CONSTRAINT chk_garantias_estado
        CHECK (estado IN ('ACTIVO', 'INACTIVO'));
