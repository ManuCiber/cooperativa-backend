package com.manudev.microservices.cuentas.aportaciones.dto;

import com.manudev.microservices.cuentas.aportaciones.entities.EstadoAportes;
import com.manudev.microservices.cuentas.aportaciones.entities.TipoAportaciones;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AportacionesResponse {
    private Long aportacionId;
    private Long socioId;
    private BigDecimal monto;
    private LocalDateTime fecha_aportacion;
    private TipoAportaciones tipo_aportacion;
    private EstadoAportes estado;
}
