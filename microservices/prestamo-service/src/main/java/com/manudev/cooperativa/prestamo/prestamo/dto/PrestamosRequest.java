package com.manudev.cooperativa.prestamo.prestamo.dto;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PrestamosRequest {
    @NotNull(message = "El ID del socio es obligatorio")
    @Positive(message = "El ID del socio debe ser positivo")
    private Long socioId;

    @NotNull(message = "El ID del producto es obligatorio")
    @Positive(message = "El ID del producto debe ser positivo")
    private Long productoId;

    @NotNull(message = "El monto solicitado es obligatorio")
    @Positive(message = "El monto solicitado debe ser mayor que cero")
    @Digits(integer = 16, fraction = 2,
            message = "El monto admite hasta 16 enteros y 2 decimales")
    private BigDecimal monto_solicitado;

    @NotNull(message = "La tasa de interes es obligatoria")
    @PositiveOrZero(message = "La tasa de interes no puede ser negativa")
    @Digits(integer = 5, fraction = 4,
            message = "La tasa admite hasta 5 enteros y 4 decimales")
    private BigDecimal tasa_interes;

    @NotNull(message = "El plazo en meses es obligatorio")
    @Positive(message = "El plazo en meses debe ser mayor que cero")
    private Integer plazo_meses;
}
