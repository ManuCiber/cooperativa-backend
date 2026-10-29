CREATE TABLE productos_financieros (
    producto_id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL UNIQUE,
    descripcion VARCHAR(255),
    tasa_interes NUMERIC(5, 2) NOT NULL,
    plazo_minimo INTEGER NOT NULL,
    plazo_maximo INTEGER NOT NULL,
    monto_minimo NUMERIC(12, 2) NOT NULL,
    monto_maximo NUMERIC(12, 2) NOT NULL,
    estado VARCHAR(20) NOT NULL DEFAULT 'ACTIVO',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_productos_tasa_no_negativa
        CHECK (tasa_interes >= 0),
    CONSTRAINT chk_productos_plazo_minimo_positivo
        CHECK (plazo_minimo > 0),
    CONSTRAINT chk_productos_plazo_rango
        CHECK (plazo_maximo >= plazo_minimo),
    CONSTRAINT chk_productos_monto_minimo_positivo
        CHECK (monto_minimo > 0),
    CONSTRAINT chk_productos_monto_rango
        CHECK (monto_maximo >= monto_minimo),
    CONSTRAINT chk_productos_estado
        CHECK (estado IN ('ACTIVO', 'INACTIVO'))
);

CREATE INDEX idx_productos_financieros_estado
    ON productos_financieros (estado);

ALTER TABLE prestamos
    ADD CONSTRAINT fk_prestamos_producto
        FOREIGN KEY (producto_id)
        REFERENCES productos_financieros (producto_id);
