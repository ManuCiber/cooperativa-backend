package com.manudev.microservices.cuentas.Ahorros_Programados.entities;

import com.manudev.microservices.cuentas.cuentas.entities.Cuentas;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "ahorros_programados")
@Builder
public class AhorrosProgramados {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ahorro_id", nullable = false)
    private Long ahorro_id;
    @NotNull
    @Positive
    @Column(name = "socio_id", nullable = false)
    private Long socio_id;
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cuenta_id", nullable = false)
    private Cuentas cuenta_id;
    @NotNull
    @Positive(message = "El monto periodico debe ser mayor que cero")
    @Digits(integer = 16, fraction = 2)
    @Column(name = "monto_periodico", nullable = false, precision = 18, scale = 2)
    private BigDecimal monto_periodico;
    @Enumerated(EnumType.STRING)
    @NotNull
    @Column(name = "frecuencia_ahorro", nullable = false)
    private FrecuenciaAhorro periodicidad;
    @NotNull
    @Column(name = "fecha_inicio", nullable = false)
    private LocalDateTime fecha_inicio;
    @Column(name = "fecha_fin")
    private LocalDateTime fecha_fin;
    @Enumerated(EnumType.STRING)
    @NotNull
    @Column(name = "estado", nullable = false)
    private EstadoAhorros estado;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
        if (fecha_inicio == null) {
            fecha_inicio = now;
        }
        if (estado == null) {
            estado = EstadoAhorros.ACTIVO;
        }
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }

    @JsonIgnore
    @AssertTrue(message = "La fecha fin debe ser posterior o igual a la fecha inicio")
    public boolean isPeriodoValido() {
        return fecha_inicio == null || fecha_fin == null || !fecha_fin.isBefore(fecha_inicio);
    }
}
