ALTER TABLE directivas
    ALTER COLUMN estado TYPE VARCHAR(20),
    ALTER COLUMN estado SET DEFAULT 'ACTIVO',
    ADD CONSTRAINT chk_directivas_estado
        CHECK (estado IN ('ACTIVO', 'INACTIVO')),
    ADD CONSTRAINT chk_directivas_periodo
        CHECK (periodo_fin IS NULL OR periodo_fin >= periodo_inicio);
