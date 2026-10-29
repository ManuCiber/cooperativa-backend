CREATE TABLE ahorros_programados (
    ahorro_id BIGSERIAL PRIMARY KEY,
    socio_id BIGINT NOT NULL CHECK (socio_id > 0),
    cuenta_id BIGINT NOT NULL REFERENCES cuentas (id),
    monto_periodico NUMERIC(18, 2) NOT NULL CHECK (monto_periodico > 0),
    frecuencia_ahorro VARCHAR(255) NOT NULL
        CHECK (frecuencia_ahorro IN ('SEMANAL', 'QUINCENAL', 'MENSUAL')),
    fecha_inicio TIMESTAMP NOT NULL,
    fecha_fin TIMESTAMP,
    estado VARCHAR(255) NOT NULL
        CHECK (estado IN ('ACTIVO', 'PAUSADO', 'COMPLETADO', 'CANCELADO')),
    CONSTRAINT chk_ahorros_programados_fechas
        CHECK (fecha_fin IS NULL OR fecha_fin >= fecha_inicio)
);

CREATE INDEX idx_ahorros_programados_cuenta ON ahorros_programados (cuenta_id);
CREATE INDEX idx_ahorros_programados_socio ON ahorros_programados (socio_id);
