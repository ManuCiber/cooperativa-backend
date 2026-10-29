package com.manudev.cooperativa.prestamo.productoFinanciero.dto;

import com.manudev.cooperativa.prestamo.productoFinanciero.entity.EstadoProducto;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductoFinancieroResponse {
    private Long id;
    private String nombre;
    private String descripcion;
    private BigDecimal tasaInteres;
    private Integer plazoMinimo;
    private Integer plazoMaximo;
    private BigDecimal montoMinimo;
    private BigDecimal montoMaximo;
    private EstadoProducto estado;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
