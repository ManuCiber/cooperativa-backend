package com.manudev.microservices.usuarios_service.empleados.dto;

import com.manudev.microservices.usuarios_service.empleados.entities.EstadoEmpleado;
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
public class EmpleadoResponse {
    private Long empleadoId;
    private String nombre;
    private String apellidos;
    private String dniCedula;
    private String cargo;
    private String departamento;
    private LocalDateTime fechaContratacion;
    private BigDecimal salario;
    private EstadoEmpleado estado;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
