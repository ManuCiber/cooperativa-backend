package com.manudev.cooperativa.prestamo.PagoPrestamos.mapper;

import com.manudev.cooperativa.prestamo.PagoPrestamos.dto.PagoPrestamosResponse;
import com.manudev.cooperativa.prestamo.PagoPrestamos.entities.PagoPrestamos;
import org.springframework.stereotype.Component;

@Component
public class PagoPrestamosMapper {

    public PagoPrestamosResponse toResponse(PagoPrestamos e) {
        return new PagoPrestamosResponse(
                e.getPago_id(),
                e.getPrestamo_id(),
                e.getNumero_cuota(),
                e.getMonto_cuota(),
                e.getMonto_capital(),
                e.getMonto_interes(),
                e.getFecha_vencimiento(),
                e.getFecha_pago(),
                e.getEstado(),
                e.getCreatedAt(),
                e.getUpdatedAt()
        );
    }
}