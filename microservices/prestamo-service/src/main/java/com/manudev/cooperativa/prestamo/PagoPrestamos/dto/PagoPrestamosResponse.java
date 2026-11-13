package com.manudev.cooperativa.prestamo.PagoPrestamos.dto;

import com.manudev.cooperativa.prestamo.PagoPrestamos.entities.EstadoPagosPrestamo;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
/** Datos que devuelve la API. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PagoPrestamosResponse {
    private Long pagoId;
    private Long prestamoId;
    private Integer numeroCuota;
    private BigDecimal montoCuota;
    private BigDecimal montoCapital;
    private BigDecimal montoInteres;
    private LocalDate fechaVencimiento;
    private LocalDateTime fechaPago;
    private EstadoPagosPrestamo estado;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}