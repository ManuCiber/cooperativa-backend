package com.manudev.cooperativa.prestamo.prestamo.service;

import com.manudev.cooperativa.prestamo.prestamo.dto.*;
import com.manudev.cooperativa.prestamo.prestamo.entities.*;
import com.manudev.cooperativa.prestamo.prestamo.mapper.PrestamosMapper;
import com.manudev.cooperativa.prestamo.prestamo.repository.PrestamosRepository;
import com.manudev.cooperativa.prestamo.prestamo.service.impl.PrestamosServiceImpl;
import com.manudev.cooperativa.prestamo.productoFinanciero.entity.EstadoProducto;
import com.manudev.cooperativa.prestamo.productoFinanciero.entity.ProductoFinanciero;
import com.manudev.cooperativa.prestamo.productoFinanciero.repository.ProductoFinancieroRepository;
import jakarta.validation.*;
import org.junit.jupiter.api.*;
import org.springframework.web.server.ResponseStatusException;
import java.math.BigDecimal;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PrestamosServiceImplTest {
    static ValidatorFactory factory;
    PrestamosRepository repository;
    ProductoFinancieroRepository productoFinancieroRepository;
    IPrestamosService service;
    PrestamosMapper mapper = new PrestamosMapper();

    @BeforeAll static void open() { factory = Validation.buildDefaultValidatorFactory(); }
    @AfterAll static void close() { factory.close(); }

    @BeforeEach void setup() {
        repository = mock(PrestamosRepository.class);
        productoFinancieroRepository = mock(ProductoFinancieroRepository.class);
        service = new PrestamosServiceImpl(repository, mapper, factory.getValidator(), productoFinancieroRepository);

        var producto = ProductoFinanciero.builder()
                .id(2L)
                .nombre("Credito Personal")
                .estado(EstadoProducto.ACTIVO)
                .montoMinimo(new BigDecimal("100.00"))
                .montoMaximo(new BigDecimal("50000.00"))
                .plazoMinimo(1)
                .plazoMaximo(60)
                .build();
        when(productoFinancieroRepository.findById(2L)).thenReturn(Optional.of(producto));
    }

    PrestamosRequest request() {
        return PrestamosRequest.builder().socioId(1L).productoId(2L)
                .monto_solicitado(new BigDecimal("1000.25")).tasa_interes(BigDecimal.ZERO)
                .plazo_meses(12).build();
    }

    Prestamos stored() {
        var p = mapper.toEntity(request());
        p.setPrestamo_id(3L);
        p.prePersist();
        return p;
    }

    void status(int expected, Runnable action) {
        assertEquals(expected, assertThrows(ResponseStatusException.class, action::run).getStatusCode().value());
    }

    @Test void createsRequestedLoanAndMapsGeneratedValues() {
        when(repository.saveAndFlush(any())).thenAnswer(i -> {
            Prestamos p = i.getArgument(0);
            assertEquals(EstadoPrestamos.SOLICITADO, p.getEstado());
            p.setPrestamo_id(3L);
            p.prePersist();
            return p;
        });
        var result = service.createPrestamo(request());
        assertEquals(3L, result.getPrestamoId());
        assertEquals(new BigDecimal("1000.25"), result.getMonto_solicitado());
        assertNotNull(result.getCreatedAt());
        assertNull(result.getMonto_aprobado());
    }

    @Test void rejectsInvalidRequestsBeforePersistence() {
        status(400, () -> service.createPrestamo(null));
        var request = request();
        request.setMonto_solicitado(BigDecimal.ZERO);
        status(400, () -> service.createPrestamo(request));
        status(400, () -> service.updatePrestamo(3L, request));
        verifyNoInteractions(repository);
    }

    @Test void rejectsLoanWhenProductNotFoundOrInactive() {
        when(productoFinancieroRepository.findById(99L)).thenReturn(Optional.empty());
        var reqNotFound = request();
        reqNotFound.setProductoId(99L);
        status(404, () -> service.createPrestamo(reqNotFound));

        var inactiveProduct = ProductoFinanciero.builder()
                .id(2L)
                .nombre("Credito Personal")
                .estado(EstadoProducto.INACTIVO)
                .montoMinimo(new BigDecimal("100.00"))
                .montoMaximo(new BigDecimal("50000.00"))
                .plazoMinimo(1)
                .plazoMaximo(60)
                .build();
        when(productoFinancieroRepository.findById(2L)).thenReturn(Optional.of(inactiveProduct));
        status(409, () -> service.createPrestamo(request()));
    }

    @Test void rejectsLoanWhenAmountOrTermExceedsLimits() {
        var reqLowAmount = request();
        reqLowAmount.setMonto_solicitado(new BigDecimal("50.00"));
        status(400, () -> service.createPrestamo(reqLowAmount));

        var reqHighAmount = request();
        reqHighAmount.setMonto_solicitado(new BigDecimal("60000.00"));
        status(400, () -> service.createPrestamo(reqHighAmount));

        var reqLowTerm = request();
        reqLowTerm.setPlazo_meses(0);
        status(400, () -> service.createPrestamo(reqLowTerm));

        var reqHighTerm = request();
        reqHighTerm.setPlazo_meses(72);
        status(400, () -> service.createPrestamo(reqHighTerm));
    }

    @Test void handlesMissingAndInvalidIds() {
        status(400, () -> service.getPrestamoById(null));
        status(400, () -> service.getPrestamoById(0L));
        when(repository.findById(3L)).thenReturn(Optional.empty());
        status(404, () -> service.getPrestamoById(3L));
    }

    @Test void updatesRequestWithoutReplacingIdentityOrAudit() {
        var p = stored();
        var created = p.getCreatedAt();
        when(repository.findById(3L)).thenReturn(Optional.of(p));
        when(repository.saveAndFlush(any())).thenAnswer(i -> i.getArgument(0));
        var request = request();
        request.setPlazo_meses(24);
        var result = service.updatePrestamo(3L, request);
        assertEquals(3L, result.getPrestamoId());
        assertEquals(created, result.getCreatedAt());
        assertEquals(24, result.getPlazo_meses());
        assertEquals(EstadoPrestamos.SOLICITADO, result.getEstado());
    }

    @Test void rejectsChangesForEveryNonRequestedState() {
        var p = stored();
        when(repository.findById(3L)).thenReturn(Optional.of(p));
        for (var estado : EstadoPrestamos.values()) {
            if (estado == EstadoPrestamos.SOLICITADO) continue;
            p.setEstado(estado);
            status(409, () -> service.updatePrestamo(3L, request()));
            status(409, () -> service.cancelarPrestamo(3L));
        }
        verify(repository, never()).saveAndFlush(any());
    }

    @Test void cancellationRetainsRecordAndAmounts() {
        var p = stored();
        when(repository.findById(3L)).thenReturn(Optional.of(p));
        when(repository.saveAndFlush(any())).thenAnswer(i -> i.getArgument(0));
        var result = service.cancelarPrestamo(3L);
        assertEquals(EstadoPrestamos.CANCELADO, result.getEstado());
        assertEquals(new BigDecimal("1000.25"), result.getMonto_solicitado());
        verify(repository, never()).delete(any());
        verify(repository, never()).deleteById(any());
    }

    @Test void listsAndRetrievesMappedLoans() {
        var p = stored();
        when(repository.findAll()).thenReturn(List.of(p));
        when(repository.findById(3L)).thenReturn(Optional.of(p));
        assertEquals(3L, service.getPrestamos().get(0).getPrestamoId());
        assertEquals(2L, service.getPrestamoById(3L).getProductoId());
    }
}
