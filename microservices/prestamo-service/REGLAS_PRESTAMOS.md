# Reglas funcionales de préstamos

Especificación inicial para el cálculo y seguimiento de préstamos en `prestamo-service`.

## Condiciones financieras

- `tasa_interes` es una tasa nominal anual, expresada como porcentaje. Por ejemplo, `18.0000` representa 18 % anual.
- La tasa periódica mensual se obtiene como `tasa_interes / 100 / 12`. No se aplica capitalización diaria ni una conversión de tasa efectiva.
- La amortización es por cuota fija mensual, mediante el método francés, calculada sobre el capital aprobado y el plazo del préstamo.
- La cuota teórica se calcula como `P * i / (1 - (1 + i)^(-n))`, donde `P` es el capital, `i` la tasa mensual y `n` el plazo en meses. Si `i` es cero, la cuota es `P / n`.
- Los importes se expresan en unidades monetarias con dos decimales y se redondean con `HALF_UP`. En cada período, el interés es el saldo pendiente multiplicado por la tasa mensual y redondeado a dos decimales; el abono a capital es la cuota menos el interés.
- La última cuota se ajusta al saldo de capital pendiente más el interés del período, redondeados a dos decimales, para dejar el saldo en cero. Por ello, puede diferir de las cuotas anteriores.
- Seguros, comisiones, impuestos y otros cargos no forman parte del cálculo inicial.

## Fechas y vencimientos

- La fecha de desembolso es la fecha de inicio del cronograma.
- La primera cuota vence un mes después del desembolso; las cuotas siguientes vencen mensualmente.
- Los vencimientos se calculan cada uno desde la fecha ancla original, no desde el vencimiento anterior. Si el día ancla no existe en un mes, se usa el último día de ese mes; en meses posteriores se retoma el día ancla original.
- Una cuota pendiente se considera vencida desde el día siguiente a su fecha de vencimiento.

## Generación del cronograma y pagos

- El cronograma se genera al desembolsar el préstamo, no al crear la solicitud ni al aprobarla.
- La generación de cuotas y el cambio de estado asociado al desembolso deben ser atómicos. Reintentar el desembolso no debe crear cuotas duplicadas.
- En esta versión no se admiten pagos parciales: el registro de pago liquida la cuota completa.
- La aprobación se registra con `PATCH /api/prestamos/{id}/aprobar?montoAprobado=...`; el desembolso se registra con `PATCH /api/prestamos/{id}/desembolsar`. El estado final del préstamo desembolsado es `ACTIVO`.
- Las cuotas son registros financieros del cronograma: no se crean manualmente ni se eliminan por separado.

## Estados de mora

- Un préstamo pasa a `EN_MORA` cuando tiene al menos una cuota pendiente cuya fecha de vencimiento ya pasó.
- Vuelve a `ACTIVO` cuando no tiene cuotas pendientes vencidas.
- La evaluación se realiza al registrar un pago y periódicamente para detectar vencimientos aunque no haya operaciones.
- La actualización automática no debe sobrescribir estados terminales, como `PAGADO`, `CANCELADO` o `RECHAZADO`.
- Al pagar la última cuota pendiente, el préstamo pasa a `PAGADO`.
