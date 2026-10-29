package com.manudev.cooperativa.prestamo.prestamo.dto;

import com.manudev.cooperativa.prestamo.prestamo.entities.EstadoPrestamos;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PrestamosResponse {
    private Long prestamoId;
    private Long socioId;
    private Long productoId;
    private BigDecimal monto_solicitado;
    private BigDecimal monto_aprobado;
    private BigDecimal tasa_interes;
    private Integer plazo_meses;
    private LocalDateTime fecha_solicitud;
    private LocalDateTime fecha_aprobacion;
    private LocalDateTime fecha_desembolso;
    private EstadoPrestamos estado;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
