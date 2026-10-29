package com.manudev.microservices.usuarios_service.empleados.dto;

import com.manudev.microservices.usuarios_service.empleados.entities.EstadoEmpleado;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmpleadoRequest {
    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 255, message = "El nombre no puede superar 255 caracteres")
    private String nombre;

    @NotBlank(message = "Los apellidos son obligatorios")
    @Size(max = 255, message = "Los apellidos no pueden superar 255 caracteres")
    private String apellidos;

    @NotBlank(message = "El DNI o cédula es obligatorio")
    @Size(max = 255, message = "El DNI o cédula no puede superar 255 caracteres")
    private String dniCedula;

    @NotBlank(message = "El cargo es obligatorio")
    @Size(max = 255, message = "El cargo no puede superar 255 caracteres")
    private String cargo;

    @NotBlank(message = "El departamento es obligatorio")
    @Size(max = 255, message = "El departamento no puede superar 255 caracteres")
    private String departamento;

    private LocalDateTime fechaContratacion;

    @NotNull(message = "El salario es obligatorio")
    @PositiveOrZero(message = "El salario no puede ser negativo")
    @Digits(integer = 14, fraction = 2,
            message = "El salario admite hasta 14 enteros y 2 decimales")
    private BigDecimal salario;

    private EstadoEmpleado estado;
}
