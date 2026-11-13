package com.manudev.cooperativa.prestamo.prestamo.service.impl;

import com.manudev.cooperativa.prestamo.PagoPrestamos.entities.PagoPrestamos;
import com.manudev.cooperativa.prestamo.PagoPrestamos.repositories.PagoPrestamosRepository;
import com.manudev.cooperativa.prestamo.prestamo.entities.Prestamos;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CronogramaPagosServiceTest {
    private final PagoPrestamosRepository repository = mock(PagoPrestamosRepository.class);
    private final CronogramaPagosService service = new CronogramaPagosService(repository);

    @Test
    void generatesZeroRateScheduleAndAnchorsMonthEndDates() {
        when(repository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));
        Prestamos prestamo = prestamo("1000.01", "0", 3, 1, 31);

        List<PagoPrestamos> cuotas = service.generar(prestamo);

        assertEquals(3, cuotas.size());
        assertEquals(new BigDecimal("333.34"), cuotas.get(0).getMonto_cuota());
        assertEquals(new BigDecimal("333.34"), cuotas.get(1).getMonto_cuota());
        assertEquals(new BigDecimal("333.33"), cuotas.get(2).getMonto_cuota());
        assertEquals(new BigDecimal("1000.01"),
                cuotas.stream().map(PagoPrestamos::getMonto_capital).reduce(BigDecimal.ZERO, BigDecimal::add));
        assertEquals("2025-02-28", cuotas.get(0).getFecha_vencimiento().toString());
        assertEquals("2025-03-31", cuotas.get(1).getFecha_vencimiento().toString());
        assertEquals("2025-04-30", cuotas.get(2).getFecha_vencimiento().toString());
    }

    @Test
    void calculatesFrenchInterestOnDecliningBalanceAndSettlesFinalPrincipal() {
        when(repository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));
        Prestamos prestamo = prestamo("1000.00", "12", 12, 3, 15);

        List<PagoPrestamos> cuotas = service.generar(prestamo);

        assertEquals(new BigDecimal("10.00"), cuotas.get(0).getMonto_interes());
        assertEquals(new BigDecimal("78.85"), cuotas.get(0).getMonto_capital());
        assertEquals(new BigDecimal("1000.00"),
                cuotas.stream().map(PagoPrestamos::getMonto_capital).reduce(BigDecimal.ZERO, BigDecimal::add));
        assertEquals(0, cuotas.get(11).getMonto_cuota()
                .compareTo(cuotas.get(11).getMonto_capital().add(cuotas.get(11).getMonto_interes())));
    }

    private Prestamos prestamo(String capital, String tasaAnual, int plazo, int month, int day) {
        return Prestamos.builder()
                .prestamo_id(10L)
                .monto_aprobado(new BigDecimal(capital))
                .tasa_interes(new BigDecimal(tasaAnual))
                .plazo_meses(plazo)
                .fecha_desembolso(LocalDateTime.of(2025, month, day, 12, 0))
                .build();
    }
}
