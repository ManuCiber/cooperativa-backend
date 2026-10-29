package com.manudev.cooperativa.prestamo.prestamo.entities;


import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;


@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@Table(name = "prestamos")
@Entity
public class Prestamos {
    @Id
    @GeneratedValue(strategy =  GenerationType.IDENTITY)
    private Long prestamo_id;
    @NotNull
    @Positive
    @Column(name = "socio_id", nullable = false)
    private Long socio_id;
    @NotNull
    @Positive
    @Column(name = "producto_id", nullable = false)
    private Long producto_id;
    @NotNull
    @Positive
    @Digits(integer = 16, fraction = 2)
    @Column(name = "monto_solicitado", nullable = false, precision = 18, scale = 2)
    private BigDecimal monto_solicitado;
    @Positive
    @Digits(integer = 16, fraction = 2)
    @Column(name = "monto_aprobado", precision = 18, scale = 2)
    private BigDecimal monto_aprobado;
    @NotNull
    @PositiveOrZero
    @Digits(integer = 5, fraction = 4)
    @Column(name = "tasa_interes", nullable = false, precision = 9, scale = 4)
    private BigDecimal tasa_interes;
    @NotNull
    @Positive
    @Column(name = "plazo_meses", nullable = false)
    private Integer plazo_meses;
    @Column(name = "fecha_solicitud", nullable = false)
    private LocalDateTime fecha_solicitud;
    @Column(name = "fecha_aprobacion")
    private LocalDateTime fecha_aprobacion;
    @Column(name = "fecha_desembolso")
    private LocalDateTime fecha_desembolso;
    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false)
    private EstadoPrestamos estado;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
        if (fecha_solicitud == null) {
            fecha_solicitud = now;
        }
        if (estado == null) {
            estado = EstadoPrestamos.SOLICITADO;
        }
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }

    @JsonIgnore
    @AssertTrue(message = "Las fechas deben seguir el orden solicitud, aprobacion y desembolso")
    public boolean isFechasValidas() {
        if (fecha_aprobacion != null
                && (fecha_solicitud == null || fecha_aprobacion.isBefore(fecha_solicitud))) {
            return false;
        }
        return fecha_desembolso == null
                || (fecha_aprobacion != null && !fecha_desembolso.isBefore(fecha_aprobacion));
    }
}
