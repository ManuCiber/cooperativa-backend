package com.manudev.microservices.cuentas.Ahorros_Programados.dto;

import com.manudev.microservices.cuentas.Ahorros_Programados.entities.EstadoAhorros;
import com.manudev.microservices.cuentas.Ahorros_Programados.entities.FrecuenciaAhorro;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AhorrosProgramadosResponse {
    private Long ahorroId;
    private Long socioId;
    private Long cuentaId;
    private BigDecimal monto_periodico;
    private FrecuenciaAhorro periodicidad;
    private LocalDateTime fecha_inicio;
    private LocalDateTime fecha_fin;
    private EstadoAhorros estado;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
