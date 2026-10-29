ALTER TABLE transacciones
    ADD COLUMN socio_id BIGINT,
    ADD COLUMN socio_destino_id BIGINT,
    ADD COLUMN realizada_por_usuario_id BIGINT;

-- No inferir titulares historicos a partir del propietario actual.
-- Los movimientos anteriores conservan NULL cuando el titular no se conoce.
-- El autor queda NULL hasta integrar una fuente de autenticacion confiable.
CREATE INDEX idx_transacciones_socio_fecha ON transacciones (socio_id, fecha);
CREATE INDEX idx_transacciones_socio_destino_fecha ON transacciones (socio_destino_id, fecha);
CREATE INDEX idx_transacciones_tipo_fecha ON transacciones (tipo_transaccion, fecha);
