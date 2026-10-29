package com.manudev.microservices.cuentas.Ahorros_Programados.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.manudev.microservices.cuentas.Ahorros_Programados.entities.EstadoAhorros;
import com.manudev.microservices.cuentas.Ahorros_Programados.entities.FrecuenciaAhorro;
import jakarta.validation.constraints.AssertTrue;
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
public class AhorrosProgramadosRequest {
    @NotNull(message = "El ID del socio es obligatorio")
    @Positive(message = "El ID del socio debe ser positivo")
    private Long socioId;

    @NotNull(message = "El ID de la cuenta es obligatorio")
    @Positive(message = "El ID de la cuenta debe ser positivo")
    private Long cuentaId;

    @NotNull(message = "El monto periodico es obligatorio")
    @Positive(message = "El monto periodico debe ser mayor que cero")
    @Digits(integer = 16, fraction = 2,
            message = "El monto admite hasta 16 enteros y 2 decimales")
    private BigDecimal monto_periodico;

    @NotNull(message = "La periodicidad es obligatoria")
    private FrecuenciaAhorro periodicidad;

    @NotNull(message = "La fecha de inicio es obligatoria")
    @Builder.Default
    private LocalDateTime fecha_inicio = LocalDateTime.now();

    private LocalDateTime fecha_fin;

    @NotNull(message = "El estado es obligatorio")
    @Builder.Default
    private EstadoAhorros estado = EstadoAhorros.ACTIVO;

    @JsonIgnore
    @AssertTrue(message = "La fecha fin debe ser posterior o igual a la fecha inicio")
    public boolean isPeriodoValido() {
        return fecha_inicio == null || fecha_fin == null || !fecha_fin.isBefore(fecha_inicio);
    }
}
