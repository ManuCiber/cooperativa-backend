package com.manudev.microservices.cuentas.cuentas.dto;

import com.manudev.microservices.cuentas.cuentas.entities.EstadoCuenta;
import com.manudev.microservices.cuentas.cuentas.entities.TipoCuenta;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.*;
import com.fasterxml.jackson.annotation.JsonCreator;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor(onConstructor_ = @JsonCreator)
@AllArgsConstructor
@Builder
public class CuentasRequest {
    @NotNull(message = "El socio es obligatorio")
    @Positive(message = "El ID del socio debe ser positivo")
    private Long socioId;

    @NotBlank(message = "El numero de cuenta es obligatorio")
    @Size(max = 255, message = "El numero de cuenta no debe superar los 255 caracteres")
    private String numeroCuenta;

    @NotNull(message = "El tipo de cuenta es obligatorio")
    @Builder.Default
    private TipoCuenta tipoCuenta = TipoCuenta.AHORRO_ORDINARIO;

    @NotNull(message = "La fecha de apertura es obligatoria")
    private LocalDate fecha_apertura;

    @NotNull(message = "El saldo es obligatorio")
    @Digits(integer = 16, fraction = 2, message = "El saldo admite hasta 16 enteros y 2 decimales")
    private BigDecimal saldo_actual;

    @NotNull(message = "El estado es obligatorio")
    private EstadoCuenta estado;
}
