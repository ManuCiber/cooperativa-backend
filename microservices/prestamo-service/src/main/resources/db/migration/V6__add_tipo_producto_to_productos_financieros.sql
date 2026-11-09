ALTER TABLE productos_financieros
    ADD COLUMN tipo_producto VARCHAR(20) NOT NULL DEFAULT 'PRESTAMOS',
    ADD CONSTRAINT chk_productos_tipo_producto
        CHECK (tipo_producto IN ('AHORROS', 'PRESTAMOS'));
