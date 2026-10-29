package com.manudev.cooperativa.prestamo.prestamo.mapper;

import com.manudev.cooperativa.prestamo.prestamo.dto.*;
import com.manudev.cooperativa.prestamo.prestamo.entities.Prestamos;
import org.springframework.stereotype.Component;

@Component
public class PrestamosMapper {
    public Prestamos toEntity(PrestamosRequest request) {
        Prestamos prestamo = new Prestamos();
        updateEntity(prestamo, request);
        return prestamo;
    }

    public void updateEntity(Prestamos prestamo, PrestamosRequest request) {
        prestamo.setSocio_id(request.getSocioId());
        prestamo.setProducto_id(request.getProductoId());
        prestamo.setMonto_solicitado(request.getMonto_solicitado());
        prestamo.setTasa_interes(request.getTasa_interes());
        prestamo.setPlazo_meses(request.getPlazo_meses());
    }

    public PrestamosResponse toResponse(Prestamos prestamo) {
        return PrestamosResponse.builder()
                .prestamoId(prestamo.getPrestamo_id())
                .socioId(prestamo.getSocio_id())
                .productoId(prestamo.getProducto_id())
                .monto_solicitado(prestamo.getMonto_solicitado())
                .monto_aprobado(prestamo.getMonto_aprobado())
                .tasa_interes(prestamo.getTasa_interes())
                .plazo_meses(prestamo.getPlazo_meses())
                .fecha_solicitud(prestamo.getFecha_solicitud())
                .fecha_aprobacion(prestamo.getFecha_aprobacion())
                .fecha_desembolso(prestamo.getFecha_desembolso())
                .estado(prestamo.getEstado())
                .createdAt(prestamo.getCreatedAt())
                .updatedAt(prestamo.getUpdatedAt())
                .build();
    }
}
