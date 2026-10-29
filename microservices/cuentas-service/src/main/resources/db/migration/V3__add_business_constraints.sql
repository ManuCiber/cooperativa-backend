ALTER TABLE cuentas
    ADD CONSTRAINT chk_cuentas_tipo
        CHECK (tipo_cuenta IN ('AHORRO_ORDINARIO', 'AHORRO_PROGRAMADO')),
    ADD CONSTRAINT chk_cuentas_estado
        CHECK (estado_cuenta IN ('ACTIVO', 'INACTIVO')),
    ADD CONSTRAINT chk_cuentas_saldo_no_negativo
        CHECK (saldo >= 0);

ALTER TABLE transacciones
    ADD CONSTRAINT chk_transacciones_tipo
        CHECK (tipo_transaccion IN (
            'DEPOSITO',
            'RETIRO',
            'TRANSFERENCIA',
            'AJUSTE_CREDITO',
            'AJUSTE_DEBITO'
        )),
    ADD CONSTRAINT chk_transacciones_monto_positivo
        CHECK (monto > 0),
    ADD CONSTRAINT chk_transacciones_cuenta_destino_distinta
        CHECK (cuenta_destino_id IS NULL OR cuenta_destino_id <> cuenta_id),
    ADD CONSTRAINT chk_transacciones_destino_transferencia
        CHECK (
            (tipo_transaccion = 'TRANSFERENCIA' AND cuenta_destino_id IS NOT NULL)
            OR
            (tipo_transaccion <> 'TRANSFERENCIA' AND cuenta_destino_id IS NULL)
        );
