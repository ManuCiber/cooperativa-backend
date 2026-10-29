package com.manudev.cooperativa.prestamo.PagoPrestamos.entities;

public enum EstadoPagosPrestamo {
    PENDIENTE,
    PAGADO,
    ELIMINADO // soft delete: la fila se conserva, solo cambia el estado
}