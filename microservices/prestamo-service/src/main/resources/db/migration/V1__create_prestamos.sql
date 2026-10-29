CREATE TABLE prestamos (
    prestamo_id BIGSERIAL PRIMARY KEY,
    socio_id BIGINT NOT NULL CHECK (socio_id > 0),
    producto_id BIGINT NOT NULL CHECK (producto_id > 0),
    monto_solicitado NUMERIC(18, 2) NOT NULL CHECK (monto_solicitado > 0),
    monto_aprobado NUMERIC(18, 2) CHECK (monto_aprobado > 0),
    tasa_interes NUMERIC(9, 4) NOT NULL CHECK (tasa_interes >= 0),
    plazo_meses INTEGER NOT NULL CHECK (plazo_meses > 0),
    fecha_solicitud TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_aprobacion TIMESTAMP,
    fecha_desembolso TIMESTAMP,
    estado VARCHAR(255) NOT NULL DEFAULT 'SOLICITADO'
        CHECK (estado IN ('SOLICITADO', 'EN_REVISION', 'APROBADO', 'RECHAZADO',
                         'DESEMBOLSADO', 'ACTIVO', 'EN_MORA', 'PAGADO', 'CANCELADO')),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_prestamos_fecha_aprobacion
        CHECK (fecha_aprobacion IS NULL OR fecha_aprobacion >= fecha_solicitud),
    CONSTRAINT chk_prestamos_fecha_desembolso
        CHECK (fecha_desembolso IS NULL
               OR (fecha_aprobacion IS NOT NULL AND fecha_desembolso >= fecha_aprobacion))
);

CREATE INDEX idx_prestamos_socio ON prestamos (socio_id);
CREATE INDEX idx_prestamos_producto ON prestamos (producto_id);
