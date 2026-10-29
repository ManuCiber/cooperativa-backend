CREATE TABLE cuentas (
    id BIGSERIAL PRIMARY KEY,
    socio_id BIGINT NOT NULL,
    no_cuenta VARCHAR(255) NOT NULL UNIQUE,
    tipo_cuenta VARCHAR(255) NOT NULL DEFAULT 'AHORRO_ORDINARIO',
    fecha_apertura DATE NOT NULL,
    saldo NUMERIC(18, 2) NOT NULL,
    estado_cuenta VARCHAR(255) NOT NULL
);

CREATE TABLE transacciones (
    transaccion_id BIGSERIAL PRIMARY KEY,
    cuenta_id BIGINT NOT NULL REFERENCES cuentas (id),
    tipo_transaccion VARCHAR(255) NOT NULL,
    monto NUMERIC(18, 2) NOT NULL,
    fecha TIMESTAMP NOT NULL,
    descripcion VARCHAR(255),
    cuenta_destino_id BIGINT REFERENCES cuentas (id)
);
