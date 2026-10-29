package com.manudev.microservices.cuentas.transacciones.dto;

import com.manudev.microservices.cuentas.transacciones.entities.TipoTransacciones;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransaccionesResponse {
    private Long transaccionId;
    private Long cuentaId;
    private Long socioId;
    private Long socioDestinoId;
    private Long realizadaPorUsuarioId;
    private TipoTransacciones tipo_transaccion;
    private BigDecimal monto;
    private LocalDateTime fecha;
    private String descripcion;
    private Long cuentaDestinoId;
}
