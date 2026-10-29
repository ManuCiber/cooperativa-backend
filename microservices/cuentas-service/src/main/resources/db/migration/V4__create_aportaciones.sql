CREATE TABLE aportaciones (
    aportacion_id BIGSERIAL PRIMARY KEY,
    socio_id BIGINT NOT NULL,
    monto_aportado NUMERIC(18, 2) NOT NULL,
    fecha_aporte TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    tipo_aporte VARCHAR(20) NOT NULL,
    estado VARCHAR(10) NOT NULL DEFAULT 'PENDIENTE',

    CONSTRAINT chk_aportaciones_monto_positivo
        CHECK (monto_aportado > 0),
    CONSTRAINT chk_aportaciones_tipo
        CHECK (tipo_aporte IN ('INICIAL', 'ORDINARIA', 'EXTRAORDINARIA', 'VOLUNTARIA')),
    CONSTRAINT chk_aportaciones_estado
        CHECK (estado IN ('PENDIENTE', 'APLICADA', 'ANULADA'))
);

CREATE INDEX idx_aportaciones_socio_fecha
    ON aportaciones (socio_id, fecha_aporte);
