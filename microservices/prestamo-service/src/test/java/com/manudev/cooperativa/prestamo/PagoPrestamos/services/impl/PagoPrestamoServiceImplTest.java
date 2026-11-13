package com.manudev.cooperativa.prestamo.PagoPrestamos.services.impl;

import com.manudev.cooperativa.prestamo.PagoPrestamos.entities.EstadoPagosPrestamo;
import com.manudev.cooperativa.prestamo.PagoPrestamos.entities.PagoPrestamos;
import com.manudev.cooperativa.prestamo.PagoPrestamos.mapper.PagoPrestamosMapper;
import com.manudev.cooperativa.prestamo.PagoPrestamos.repositories.PagoPrestamosRepository;
import com.manudev.cooperativa.prestamo.prestamo.entities.EstadoPrestamos;
import com.manudev.cooperativa.prestamo.prestamo.entities.Prestamos;
import com.manudev.cooperativa.prestamo.prestamo.repository.PrestamosRepository;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PagoPrestamoServiceImplTest {
    private final PagoPrestamosRepository pagosRepository = mock(PagoPrestamosRepository.class);
    private final PrestamosRepository prestamosRepository = mock(PrestamosRepository.class);
    private final PagoPrestamoServiceImpl service = new PagoPrestamoServiceImpl(
            pagosRepository, new PagoPrestamosMapper(), prestamosRepository,
            Clock.fixed(LocalDate.of(2025, 6, 15).atStartOfDay().toInstant(ZoneOffset.UTC), ZoneOffset.UTC));

    @Test
    void marksLoanPaidAfterItsFinalInstallmentIsPaid() {
        PagoPrestamos cuota = cuotaPendiente();
        Prestamos prestamo = prestamo(EstadoPrestamos.ACTIVO);
        when(pagosRepository.findById(20L)).thenReturn(Optional.of(cuota));
        when(prestamosRepository.findById(10L)).thenReturn(Optional.of(prestamo));
        when(pagosRepository.save(any(PagoPrestamos.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(pagosRepository.existsByPrestamoAndEstado(10L, EstadoPagosPrestamo.PENDIENTE)).thenReturn(false);
        when(pagosRepository.existsByPrestamoAndEstado(10L, EstadoPagosPrestamo.PAGADO)).thenReturn(true);

        service.registrarPago(20L);

        assertEquals(EstadoPagosPrestamo.PAGADO, cuota.getEstado());
        assertEquals(EstadoPrestamos.PAGADO, prestamo.getEstado());
    }

    @Test
    void keepsLoanInMoraWhileAnotherOverdueInstallmentRemains() {
        PagoPrestamos cuota = cuotaPendiente();
        Prestamos prestamo = prestamo(EstadoPrestamos.EN_MORA);
        when(pagosRepository.findById(20L)).thenReturn(Optional.of(cuota));
        when(prestamosRepository.findById(10L)).thenReturn(Optional.of(prestamo));
        when(pagosRepository.save(any(PagoPrestamos.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(pagosRepository.existsByPrestamoAndEstado(10L, EstadoPagosPrestamo.PENDIENTE)).thenReturn(true);
        when(pagosRepository.existsVencidaByPrestamo(
                10L, EstadoPagosPrestamo.PENDIENTE, LocalDate.of(2025, 6, 15)))
                .thenReturn(true);

        service.registrarPago(20L);

        assertEquals(EstadoPrestamos.EN_MORA, prestamo.getEstado());
    }

    @Test
    void restoresActiveStatusWhenNoInstallmentIsOverdue() {
        Prestamos prestamo = prestamo(EstadoPrestamos.EN_MORA);
        when(prestamosRepository.findByEstadoIn(
                org.mockito.ArgumentMatchers.eq(java.util.List.of(EstadoPrestamos.ACTIVO, EstadoPrestamos.EN_MORA))))
                .thenReturn(java.util.List.of(prestamo));
        when(pagosRepository.existsByPrestamoAndEstado(10L, EstadoPagosPrestamo.PENDIENTE)).thenReturn(true);
        when(pagosRepository.existsVencidaByPrestamo(
                10L, EstadoPagosPrestamo.PENDIENTE, LocalDate.of(2025, 6, 15))).thenReturn(false);

        service.revisarEstadosMora();

        assertEquals(EstadoPrestamos.ACTIVO, prestamo.getEstado());
        verify(pagosRepository).existsVencidaByPrestamo(
                10L, EstadoPagosPrestamo.PENDIENTE, LocalDate.of(2025, 6, 15));
    }

    private PagoPrestamos cuotaPendiente() {
        return PagoPrestamos.builder()
                .pago_id(20L)
                .prestamo_id(10L)
                .numero_cuota(1)
                .monto_cuota(new BigDecimal("100.00"))
                .monto_capital(new BigDecimal("90.00"))
                .monto_interes(new BigDecimal("10.00"))
                .fecha_vencimiento(LocalDate.now())
                .estado(EstadoPagosPrestamo.PENDIENTE)
                .build();
    }

    private Prestamos prestamo(EstadoPrestamos estado) {
        return Prestamos.builder().prestamo_id(10L).estado(estado).build();
    }
}
