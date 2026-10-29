package com.manudev.cooperativa.prestamo.productoFinanciero.dto;

import com.manudev.cooperativa.prestamo.productoFinanciero.entity.EstadoProducto;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductoFinancieroRequest {

    @NotBlank(message = "El nombre del producto es obligatorio")
    @Size(max = 100, message = "El nombre admite hasta 100 caracteres")
    private String nombre;

    @Size(max = 255, message = "La descripción admite hasta 255 caracteres")
    private String descripcion;

    @NotNull(message = "La tasa de interés es obligatoria")
    @PositiveOrZero(message = "La tasa de interés no puede ser negativa")
    @Digits(integer = 5, fraction = 2, message = "La tasa admite hasta 5 enteros y 2 decimales")
    private BigDecimal tasaInteres;

    @NotNull(message = "El plazo mínimo es obligatorio")
    @Positive(message = "El plazo mínimo debe ser mayor que cero")
    private Integer plazoMinimo;

    @NotNull(message = "El plazo máximo es obligatorio")
    @Positive(message = "El plazo máximo debe ser mayor que cero")
    private Integer plazoMaximo;

    @NotNull(message = "El monto mínimo es obligatorio")
    @Positive(message = "El monto mínimo debe ser mayor que cero")
    @Digits(integer = 12, fraction = 2, message = "El monto mínimo admite hasta 12 enteros y 2 decimales")
    private BigDecimal montoMinimo;

    @NotNull(message = "El monto máximo es obligatorio")
    @Positive(message = "El monto máximo debe ser mayor que cero")
    @Digits(integer = 12, fraction = 2, message = "El monto máximo admite hasta 12 enteros y 2 decimales")
    private BigDecimal montoMaximo;

    @Builder.Default
    private EstadoProducto estado = EstadoProducto.ACTIVO;
}
