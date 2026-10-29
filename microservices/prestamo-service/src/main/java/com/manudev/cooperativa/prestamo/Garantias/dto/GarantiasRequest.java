package com.manudev.cooperativa.prestamo.Garantias.dto;

import com.manudev.cooperativa.prestamo.Garantias.entities.TipoGarantia;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.math.BigDecimal;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GarantiasRequest {
    @NotNull(message = "Prestamo ID is required")
    @Positive(message = "Prestamo ID debe ser positivo")
    private Long prestamo_id;

    private TipoGarantia tipo_garantia;

    @NotNull(message = "El valor estimado es obligatorio")
    @Positive(message = "El valor estimado debe ser positivo")
    @Digits(integer = 16, fraction = 2, message = "El valor estimado admite hasta 16 enteros y 2 decimales")
    private BigDecimal valor_estimado;

    @NotBlank(message = "La documentacion es obligatoria")
    private String documentacion;
}
