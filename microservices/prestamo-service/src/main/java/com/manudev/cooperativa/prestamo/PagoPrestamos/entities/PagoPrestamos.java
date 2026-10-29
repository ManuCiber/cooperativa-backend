package com.manudev.cooperativa.prestamo.PagoPrestamos.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "pagos_prestamos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PagoPrestamos {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pago_id")
    private Long pago_id;

    @NotNull(message = "El ID del prestamo es obligatorio")
    @Positive(message = "El ID del prestamo debe ser positivo")
    @Column(name = "prestamo_id", nullable = false)
    private Long prestamo_id;

    @NotNull(message = "El numero de cuota es obligatorio")
    @Positive(message = "El numero de cuota debe ser mayor que cero")
    @Column(name = "numero_cuota", nullable = false)
    private Integer numero_cuota;

    @NotNull(message = "El monto de la cuota es obligatorio")
    @Positive(message = "El monto de la cuota debe ser mayor que cero")
    @Digits(integer = 16, fraction = 2, message = "El monto de la cuota admite hasta 16 enteros y 2 decimales")
    @Column(name = "monto_cuota", nullable = false, precision = 18, scale = 2)
    private BigDecimal monto_cuota;

    @NotNull(message = "El monto de interes es obligatorio")
    @PositiveOrZero(message = "El monto de interes no puede ser negativo")
    @Digits(integer = 16, fraction = 2, message = "El monto de interes admite hasta 16 enteros y 2 decimales")
    @Column(name = "monto_interes", nullable = false, precision = 18, scale = 2)
    private BigDecimal monto_interes;

    @NotNull(message = "La fecha de vencimiento es obligatoria")
    @Column(name = "fecha_vencimiento", nullable = false)
    private LocalDate fecha_vencimiento;

    @Column(name = "fecha_pago")
    private LocalDateTime fecha_pago;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    private EstadoPagosPrestamo estado;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
        if (estado == null) {
            estado = EstadoPagosPrestamo.PENDIENTE;
        }
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}