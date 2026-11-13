package com.manudev.cooperativa.prestamo.integration;

import com.manudev.cooperativa.prestamo.PagoPrestamos.entities.EstadoPagosPrestamo;
import com.manudev.cooperativa.prestamo.PagoPrestamos.repositories.PagoPrestamosRepository;
import com.manudev.cooperativa.prestamo.PagoPrestamos.services.PagoPrestamosService;
import com.manudev.cooperativa.prestamo.prestamo.entities.EstadoPrestamos;
import com.manudev.cooperativa.prestamo.prestamo.entities.Prestamos;
import com.manudev.cooperativa.prestamo.prestamo.repository.PrestamosRepository;
import com.manudev.cooperativa.prestamo.prestamo.service.IPrestamosService;
import com.manudev.cooperativa.prestamo.productoFinanciero.entity.EstadoProducto;
import com.manudev.cooperativa.prestamo.productoFinanciero.entity.ProductoFinanciero;
import com.manudev.cooperativa.prestamo.productoFinanciero.entity.TipoProducto;
import com.manudev.cooperativa.prestamo.productoFinanciero.repository.ProductoFinancieroRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(properties = {
        "spring.cloud.config.enabled=false",
        "eureka.client.enabled=false"
})
class PrestamoWorkflowIntegrationTest {
    @Autowired
    private IPrestamosService prestamosService;

    @Autowired
    private PagoPrestamosService pagosService;

    @Autowired
    private PrestamosRepository prestamosRepository;

    @Autowired
    private ProductoFinancieroRepository productoRepository;

    @Autowired
    private PagoPrestamosRepository pagosRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private final List<Long> loanIds = new ArrayList<>();
    private final List<Long> productIds = new ArrayList<>();

    @AfterEach
    void cleanUp() {
        for (Long loanId : loanIds) {
            jdbcTemplate.update("DELETE FROM prestamos.pagos_prestamos WHERE prestamo_id = ?", loanId);
            jdbcTemplate.update("DELETE FROM prestamos.prestamos WHERE prestamo_id = ?", loanId);
        }
        for (Long productId : productIds) {
            jdbcTemplate.update("DELETE FROM prestamos.productos_financieros WHERE producto_id = ?", productId);
        }
        loanIds.clear();
        productIds.clear();
    }

    @Test
    void rollsBackDisbursementWhenScheduleGenerationFails() {
        Prestamos loan = createApprovedLoan("0.01", "12", 60);

        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> prestamosService.desembolsarPrestamo(loan.getPrestamo_id()));

        assertEquals(409, error.getStatusCode().value());
        Prestamos persisted = prestamosRepository.findById(loan.getPrestamo_id()).orElseThrow();
        assertEquals(EstadoPrestamos.APROBADO, persisted.getEstado());
        assertNull(persisted.getFecha_desembolso());
        assertFalse(pagosRepository.existsAnyByPrestamo(loan.getPrestamo_id()));
    }

    @Test
    void concurrentDisbursementCreatesOnlyOneSchedule() throws Exception {
        Prestamos loan = createApprovedLoan("1000.00", "0", 3);
        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            Future<Boolean> first = executor.submit(() -> tryDisburse(loan.getPrestamo_id(), ready, start));
            Future<Boolean> second = executor.submit(() -> tryDisburse(loan.getPrestamo_id(), ready, start));
            assertTrue(ready.await(10, java.util.concurrent.TimeUnit.SECONDS));
            start.countDown();

            List<Boolean> results = List.of(first.get(), second.get());
            assertEquals(1, results.stream().filter(Boolean::booleanValue).count());
            assertEquals(1, results.stream().filter(result -> !result).count());
        } finally {
            executor.shutdownNow();
        }

        Prestamos persisted = prestamosRepository.findById(loan.getPrestamo_id()).orElseThrow();
        assertEquals(EstadoPrestamos.ACTIVO, persisted.getEstado());
        assertEquals(3, pagosRepository.findByPrestamoAndEstadoNot(
                loan.getPrestamo_id(), EstadoPagosPrestamo.ELIMINADO).size());
    }

    @Test
    void onlyListsInstallmentsWhoseDueDateIsBeforeToday() {
        Prestamos loan = createApprovedLoan("100.00", "0", 3);
        loan.setEstado(EstadoPrestamos.ACTIVO);
        loan.setFecha_desembolso(LocalDateTime.now());
        prestamosRepository.saveAndFlush(loan);

        LocalDate today = LocalDate.now();
        insertInstallment(loan.getPrestamo_id(), 1, today.minusDays(1));
        insertInstallment(loan.getPrestamo_id(), 2, today);
        insertInstallment(loan.getPrestamo_id(), 3, today.plusDays(1));

        List<Long> overdueIds = pagosService.listarVencidas().stream()
                .map(com.manudev.cooperativa.prestamo.PagoPrestamos.dto.PagoPrestamosResponse::getPagoId)
                .toList();
        List<Long> testInstallmentIds = jdbcTemplate.queryForList(
                "SELECT pago_id FROM prestamos.pagos_prestamos WHERE prestamo_id = ? AND fecha_vencimiento < ?",
                Long.class, loan.getPrestamo_id(), today);

        assertEquals(1, testInstallmentIds.size());
        assertTrue(overdueIds.contains(testInstallmentIds.get(0)));
        assertNotNull(pagosRepository.findByPrestamoAndEstadoNot(
                loan.getPrestamo_id(), EstadoPagosPrestamo.ELIMINADO).get(1).getFecha_vencimiento());
    }

    private boolean tryDisburse(Long loanId, CountDownLatch ready, CountDownLatch start) throws Exception {
        ready.countDown();
        start.await();
        try {
            prestamosService.desembolsarPrestamo(loanId);
            return true;
        } catch (ResponseStatusException exception) {
            if (exception.getStatusCode().value() != 409) {
                throw exception;
            }
            return false;
        }
    }

    private Prestamos createApprovedLoan(String amount, String annualRate, int months) {
        LocalDateTime now = LocalDateTime.now();
        ProductoFinanciero product = productoRepository.saveAndFlush(ProductoFinanciero.builder()
                .nombre("integration-loan-" + java.util.UUID.randomUUID())
                .tasaInteres(BigDecimal.ZERO)
                .plazoMinimo(1)
                .plazoMaximo(120)
                .montoMinimo(new BigDecimal("0.01"))
                .montoMaximo(new BigDecimal("1000000.00"))
                .estado(EstadoProducto.ACTIVO)
                .tipoProducto(TipoProducto.PRESTAMOS)
                .build());
        productIds.add(product.getId());
        Prestamos loan = prestamosRepository.saveAndFlush(Prestamos.builder()
                .socio_id(99L)
                .producto_id(product.getId())
                .monto_solicitado(new BigDecimal(amount))
                .monto_aprobado(new BigDecimal(amount))
                .tasa_interes(new BigDecimal(annualRate))
                .plazo_meses(months)
                .fecha_solicitud(now.minusDays(2))
                .fecha_aprobacion(now.minusDays(1))
                .estado(EstadoPrestamos.APROBADO)
                .build());
        loanIds.add(loan.getPrestamo_id());
        return loan;
    }

    private void insertInstallment(Long loanId, int number, LocalDate dueDate) {
        jdbcTemplate.update("""
                INSERT INTO prestamos.pagos_prestamos
                    (prestamo_id, numero_cuota, monto_cuota, monto_capital, monto_interes, fecha_vencimiento)
                VALUES (?, ?, 100.00, 90.00, 10.00, ?)
                """, loanId, number, dueDate);
    }
}
