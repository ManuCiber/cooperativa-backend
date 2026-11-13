ALTER TABLE cuentas DROP CONSTRAINT chk_cuentas_tipo;

ALTER TABLE cuentas
    ADD CONSTRAINT chk_cuentas_tipo
        CHECK (tipo_cuenta IN (
                               'AHORRO_ORDINARIO',
                               'AHORRO_PROGRAMADO',
                               'CAPITAL_SOCIAL'
            ));