package com.manudev.microservices.cuentas.transacciones.entities;

import com.manudev.microservices.cuentas.cuentas.entities.Cuentas;
import jakarta.persistence.*;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
@Entity
@Table(name = "transacciones")
public class Transacciones {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "transaccion_id")
    private Long transaccion_id;
    // Titulares al registrar el movimiento; no se recalculan al consultar.
    @Positive
    @Column(name = "socio_id", updatable = false)
    private Long socioId;

    @Positive
    @Column(name = "socio_destino_id", updatable = false)
    private Long socioDestinoId;

    // Se asignara exclusivamente desde la autenticacion, nunca desde el request.
    @Positive
    @Column(name = "realizada_por_usuario_id", updatable = false)
    private Long realizadaPorUsuarioId;
    @NotNull(message = "La cuenta es obligatoria")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cuenta_id", nullable = false)
    private Cuentas cuenta;
    @NotNull(message = "El tipo de transaccion es obligatorio")
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_transaccion", nullable = false)
    private TipoTransacciones tipo_transaccion;
    @NotNull(message = "El monto es obligatorio")
    @Positive
    @Digits(integer = 16, fraction = 2)
    @Column(name = "monto", nullable = false, precision = 18, scale = 2)
    private BigDecimal monto;
    @NotNull(message = "La fecha es obligatoria")
    @Column(name = "fecha", nullable = false)
    private LocalDateTime fecha;
    @Size(max = 255, message = "La descripcion no debe superar los 255 caracteres")
    @Column(name = "descripcion", length = 255)
    private String descripcion;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cuenta_destino_id")
    private Cuentas cuenta_destino;


    @PrePersist
    public void prePersist() {
        if (fecha == null) {
            fecha = LocalDateTime.now();
        }
    }

    @Transient
    @AssertTrue(message = "La transferencia requiere una cuenta destino distinta de la cuenta origen")
    public boolean isTransferenciaValida() {
        if (tipo_transaccion != TipoTransacciones.TRANSFERENCIA) {
            return true;
        }
        if (cuenta == null || cuenta_destino == null || cuenta == cuenta_destino) {
            return false;
        }
        return cuenta.getId() == null || !cuenta.getId().equals(cuenta_destino.getId());
    }
}
