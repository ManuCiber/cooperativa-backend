package com.manudev.cooperativa.prestamo.PagoPrestamos.mapper;

import com.manudev.cooperativa.prestamo.PagoPrestamos.dto.PagoPrestamosRequest;
import com.manudev.cooperativa.prestamo.PagoPrestamos.dto.PagoPrestamosResponse;
import com.manudev.cooperativa.prestamo.PagoPrestamos.entities.PagoPrestamos;
import org.springframework.stereotype.Component;

@Component
public class PagoPrestamosMapper {

    public PagoPrestamos toEntity(PagoPrestamosRequest request) {
        return PagoPrestamos.builder()
                .prestamo_id(request.getPrestamoId())
                .numero_cuota(request.getNumeroCuota())
                .monto_cuota(request.getMontoCuota())
                .monto_interes(request.getMontoInteres())
                .fecha_vencimiento(request.getFechaVencimiento())
                .build(); // estado = PENDIENTE lo asigna @PrePersist
    }

    public PagoPrestamosResponse toResponse(PagoPrestamos e) {
        return new PagoPrestamosResponse(
                e.getPago_id(),
                e.getPrestamo_id(),
                e.getNumero_cuota(),
                e.getMonto_cuota(),
                e.getMonto_interes(),
                e.getFecha_vencimiento(),
                e.getFecha_pago(),
                e.getEstado(),
                e.getCreatedAt(),
                e.getUpdatedAt()
        );
    }
}