CREATE TABLE pagos_prestamos (
    pago_id BIGSERIAL PRIMARY KEY,
    prestamo_id BIGINT NOT NULL,
    numero_cuota INTEGER NOT NULL,
    monto_cuota NUMERIC(18, 2) NOT NULL,
    monto_interes NUMERIC(18, 2) NOT NULL,
    fecha_vencimiento DATE NOT NULL,
    fecha_pago TIMESTAMP,
    estado VARCHAR(20) NOT NULL DEFAULT 'PENDIENTE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_pagos_prestamos_prestamo
        FOREIGN KEY (prestamo_id) REFERENCES prestamos (prestamo_id),
    CONSTRAINT chk_pagos_prestamos_numero_cuota
        CHECK (numero_cuota > 0),
    CONSTRAINT chk_pagos_prestamos_monto_cuota
        CHECK (monto_cuota > 0),
    CONSTRAINT chk_pagos_prestamos_monto_interes
        CHECK (monto_interes >= 0),
    CONSTRAINT chk_pagos_prestamos_estado
        CHECK (estado IN ('PENDIENTE', 'PAGADO', 'ELIMINADO'))
);

CREATE UNIQUE INDEX uq_pagos_prestamos_cuota_activa
    ON pagos_prestamos (prestamo_id, numero_cuota)
    WHERE estado <> 'ELIMINADO';

CREATE INDEX idx_pagos_prestamos_estado_vencimiento
    ON pagos_prestamos (estado, fecha_vencimiento);
