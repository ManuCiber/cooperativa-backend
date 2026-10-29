package com.manudev.microservices.cuentas.cuentas.dto;

import com.manudev.microservices.cuentas.cuentas.entities.EstadoCuenta;
import com.manudev.microservices.cuentas.cuentas.entities.TipoCuenta;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CuentasResponse {
    private Long id;
    private Long socioId;
    private String numeroCuenta;
    private TipoCuenta tipoCuenta;
    private LocalDate fecha_apertura;
    private BigDecimal saldo_actual;
    private EstadoCuenta estado;
}
