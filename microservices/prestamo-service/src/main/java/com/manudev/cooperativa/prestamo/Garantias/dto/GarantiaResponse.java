package com.manudev.cooperativa.prestamo.Garantias.dto;

import com.manudev.cooperativa.prestamo.Garantias.entities.EstadoGarantia;
import com.manudev.cooperativa.prestamo.Garantias.entities.TipoGarantia;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GarantiaResponse {
    private Long garantia_id;
    private Long prestamo_id;
    private TipoGarantia tipo_garantia;
    private BigDecimal valor_estimado;
    private String documentos;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private EstadoGarantia estado;
}
