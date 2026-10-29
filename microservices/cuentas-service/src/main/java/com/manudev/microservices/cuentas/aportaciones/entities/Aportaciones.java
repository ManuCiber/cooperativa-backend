package com.manudev.microservices.cuentas.aportaciones.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;


@Entity
@Table(name = "aportaciones")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class Aportaciones {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "aportacion_id")
    private Long aportacion_id;
    @NotNull(message = "El ID del socio es obligatorio")
    @Positive(message = "El ID del socio debe ser positivo")
    @Column(name = "socio_id", nullable = false)
    private Long socio_id;
    @NotNull(message = "El monto aportado es obligatorio")
    @Positive(message = "El monto aportado debe ser mayor que cero")
    @Digits(integer = 16, fraction = 2, message = "El monto admite hasta 16 enteros y 2 decimales")
    @Column(name = "monto_aportado", nullable = false, precision = 18, scale = 2)
    private BigDecimal monto;
    @NotNull(message = "La fecha de aportación es obligatoria")
    @Column(name = "fecha_aporte", nullable = false)
    private LocalDateTime fecha_aportacion;
    @NotNull(message = "El tipo de aportación es obligatorio")
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_aporte", nullable = false,length = 20)
    private TipoAportaciones tipo_aportacion;
    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 10)
    @Builder.Default
    private EstadoAportes estado = EstadoAportes.PENDIENTE;

    @PrePersist
    public void prePersist() {
        if (fecha_aportacion == null) {
            fecha_aportacion = LocalDateTime.now();
        }
        if (estado == null) {
            estado = EstadoAportes.PENDIENTE;
        }
    }
}
