package com.manudev.microservices.cuentas.aportaciones.dto;

import com.manudev.microservices.cuentas.aportaciones.entities.TipoAportaciones;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AportacionesRequest {
    @NotNull(message = "El ID del socio es obligatorio")
    @Positive(message = "El ID del socio debe ser positivo")
    private Long socioId;

    @NotNull(message = "El monto aportado es obligatorio")
    @Positive(message = "El monto aportado debe ser mayor que cero")
    @Digits(integer = 16, fraction = 2,
            message = "El monto admite hasta 16 enteros y 2 decimales")
    private BigDecimal monto;

    @NotNull(message = "El tipo de aportación es obligatorio")
    private TipoAportaciones tipo_aportacion;

    // Si se omite, la entidad asigna la fecha actual al persistir.
    private LocalDateTime fecha_aportacion;
}
