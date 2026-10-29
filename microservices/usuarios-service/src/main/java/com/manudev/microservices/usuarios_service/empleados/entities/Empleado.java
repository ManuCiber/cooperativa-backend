package com.manudev.microservices.usuarios_service.empleados.entities;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;


@Entity
@Table(name = "Empleados")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class Empleado {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "empleado_id", nullable = false)
    private Long empleado_id;
    @Column(name = "nombre", nullable = false)
    private String nombre;
    @Column(name = "apellidos", nullable = false)
    private String apellidos;
    @Column(name = "dni_cedula", nullable = false, unique = true)
    private String dni_cedula;
    @Column(name = "cargo", nullable = false)
    private String cargo;
    @Column(name = "departamento", nullable = false)
    private String departamento;
    @Column(name = "fecha_contratacion")
    private LocalDateTime fecha_contratacion;
    @Column(name = "salario", nullable = false, precision = 16, scale = 2)
    private BigDecimal salario;
    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false)
    private EstadoEmpleado estado;
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
        if (fecha_contratacion == null) {
            fecha_contratacion = now;
        }
        if (estado == null) {
            estado = EstadoEmpleado.ACTIVO;
        }
    }

}