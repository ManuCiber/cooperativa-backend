package com.manudev.cooperativa.prestamo.PagoPrestamos.services;
import com.manudev.cooperativa.prestamo.PagoPrestamos.dto.PagoPrestamosRequest;
import com.manudev.cooperativa.prestamo.PagoPrestamos.dto.PagoPrestamosResponse;
import com.manudev.cooperativa.prestamo.PagoPrestamos.entities.EstadoPagosPrestamo;

import java.util.List;

public interface PagoPrestamosService {

    PagoPrestamosResponse crear(PagoPrestamosRequest request);

    PagoPrestamosResponse obtenerPorId(Long pagoId);

    /**
     * Lista las cuotas de un prestamo.
     * Si estado es null, devuelve todas excepto las ELIMINADO.
     * Si estado viene, filtra por ese estado exacto (incluido ELIMINADO).
     */
    List<PagoPrestamosResponse> listarPorPrestamo(Long prestamoId, EstadoPagosPrestamo estado);

    List<PagoPrestamosResponse> listarVencidas();

    /** Marca una cuota como pagada y registra la fecha del pago. */
    PagoPrestamosResponse registrarPago(Long pagoId);

    /** Soft delete: cambia el estado a ELIMINADO sin borrar la fila. */
    void eliminarPago(Long pagoId);
}