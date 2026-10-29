package com.manudev.cooperativa.prestamo.productoFinanciero.mapper;

import com.manudev.cooperativa.prestamo.productoFinanciero.dto.ProductoFinancieroRequest;
import com.manudev.cooperativa.prestamo.productoFinanciero.dto.ProductoFinancieroResponse;
import com.manudev.cooperativa.prestamo.productoFinanciero.entity.ProductoFinanciero;
import org.springframework.stereotype.Component;

@Component
public class ProductoFinancieroMapper {

    public ProductoFinanciero toEntity(ProductoFinancieroRequest request) {
        ProductoFinanciero producto = new ProductoFinanciero();
        updateEntity(producto, request);
        return producto;
    }

    public void updateEntity(ProductoFinanciero producto, ProductoFinancieroRequest request) {
        producto.setNombre(request.getNombre());
        producto.setDescripcion(request.getDescripcion());
        producto.setTasaInteres(request.getTasaInteres());
        producto.setPlazoMinimo(request.getPlazoMinimo());
        producto.setPlazoMaximo(request.getPlazoMaximo());
        producto.setMontoMinimo(request.getMontoMinimo());
        producto.setMontoMaximo(request.getMontoMaximo());
        if (request.getEstado() != null) {
            producto.setEstado(request.getEstado());
        }
    }

    public ProductoFinancieroResponse toResponse(ProductoFinanciero producto) {
        return ProductoFinancieroResponse.builder()
                .id(producto.getId())
                .nombre(producto.getNombre())
                .descripcion(producto.getDescripcion())
                .tasaInteres(producto.getTasaInteres())
                .plazoMinimo(producto.getPlazoMinimo())
                .plazoMaximo(producto.getPlazoMaximo())
                .montoMinimo(producto.getMontoMinimo())
                .montoMaximo(producto.getMontoMaximo())
                .estado(producto.getEstado())
                .createdAt(producto.getCreatedAt())
                .updatedAt(producto.getUpdatedAt())
                .build();
    }
}
