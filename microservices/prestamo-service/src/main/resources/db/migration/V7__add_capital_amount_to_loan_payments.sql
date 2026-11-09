ALTER TABLE pagos_prestamos
    ADD COLUMN monto_capital NUMERIC(18, 2);

UPDATE pagos_prestamos
SET monto_capital = monto_cuota - monto_interes;

ALTER TABLE pagos_prestamos
    ALTER COLUMN monto_capital SET NOT NULL,
    ADD CONSTRAINT chk_pagos_prestamos_monto_capital
        CHECK (monto_capital >= 0);
