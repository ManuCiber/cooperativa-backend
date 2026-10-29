package com.manudev.cooperativa.prestamo.PagoPrestamos.dto;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Datos que envia el cliente para registrar una cuota.
 * No incluye pago_id, estado, fecha_pago ni auditoria: los controla el servidor.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PagoPrestamosRequest {
    @NotNull(message = "El ID del prestamo es obligatorio")
    @Positive(message = "El ID del prestamo debe ser positivo")
    private Long prestamoId;

    @NotNull(message = "El numero de cuota es obligatorio")
    @Positive(message = "El numero de cuota debe ser mayor que cero")
    private Integer numeroCuota;

    @NotNull(message = "El monto de la cuota es obligatorio")
    @Positive(message = "El monto de la cuota debe ser mayor que cero")
    @Digits(integer = 16, fraction = 2, message = "El monto de la cuota admite hasta 16 enteros y 2 decimales")
    private BigDecimal montoCuota;

    @NotNull(message = "El monto de interes es obligatorio")
    @PositiveOrZero(message = "El monto de interes no puede ser negativo")
    @Digits(integer = 16, fraction = 2, message = "El monto de interes admite hasta 16 enteros y 2 decimales")
    private BigDecimal montoInteres;

    @NotNull(message = "La fecha de vencimiento es obligatoria")
    private LocalDate fechaVencimiento;
}