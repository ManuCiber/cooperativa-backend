CREATE TABLE garantias (
    garantia_id BIGSERIAL PRIMARY KEY,
    prestamo_id BIGINT NOT NULL,
    tipo_garantia VARCHAR(50) NOT NULL DEFAULT 'APORTACIONES',
    valor_estimado NUMERIC(16, 2) NOT NULL CHECK (valor_estimado > 0),
    documentos TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_garantias_prestamo
        FOREIGN KEY (prestamo_id) REFERENCES prestamos (prestamo_id)
);

CREATE INDEX idx_garantia_prestamo_id ON garantias (prestamo_id);
