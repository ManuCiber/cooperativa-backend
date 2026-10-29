package com.manudev.microservices.cuentas.cuentas.entities;


import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;


@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@Entity
@Table(name = "cuentas")
public class Cuentas {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "socio_id", nullable = false)
    private Long socio_id;
    @Column(name = "no_cuenta", nullable = false, unique = true)
    private String numeroCuenta;
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_cuenta", nullable = false)
    @Builder.Default
    private TipoCuenta tipoCuenta = TipoCuenta.AHORRO_ORDINARIO;
    @Column(name = "fecha_apertura", nullable = false)
    private LocalDate fecha_apertura;
    @Column(name = "saldo", nullable = false, precision = 18, scale = 2)
    private BigDecimal saldo_actual;
    @Enumerated(EnumType.STRING)
    @Column(name = "estado_cuenta", nullable = false)
    private EstadoCuenta estado;
}
