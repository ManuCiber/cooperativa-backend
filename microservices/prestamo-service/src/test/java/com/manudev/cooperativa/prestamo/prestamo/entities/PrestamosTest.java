package com.manudev.cooperativa.prestamo.prestamo.entities;

import jakarta.validation.Validation;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

class PrestamosTest {
    static ValidatorFactory factory;
    @BeforeAll static void setup() { factory = Validation.buildDefaultValidatorFactory(); }
    @AfterAll static void close() { factory.close(); }

    Prestamos solicitud() {
        var prestamo = Prestamos.builder().socio_id(1L).producto_id(2L)
                .monto_solicitado(new BigDecimal("1000.00"))
                .tasa_interes(new BigDecimal("12.5000")).plazo_meses(12).build();
        prestamo.prePersist();
        return prestamo;
    }

    @Test void acceptsNewRequestWithoutApprovalOrDisbursement() {
        var prestamo = solicitud();
        assertTrue(factory.getValidator().validate(prestamo).isEmpty());
        assertNull(prestamo.getMonto_aprobado());
        assertNull(prestamo.getFecha_aprobacion());
        assertNull(prestamo.getFecha_desembolso());
        assertEquals(EstadoPrestamos.SOLICITADO, prestamo.getEstado());
        assertEquals(prestamo.getCreatedAt(), prestamo.getUpdatedAt());
        assertEquals(prestamo.getCreatedAt(), prestamo.getFecha_solicitud());
    }

    @Test void rejectsInvalidAmountsRateTermAndIds() {
        var prestamo = solicitud();
        prestamo.setSocio_id(0L);
        prestamo.setProducto_id(-1L);
        prestamo.setMonto_solicitado(BigDecimal.ZERO);
        prestamo.setMonto_aprobado(new BigDecimal("-1"));
        prestamo.setTasa_interes(new BigDecimal("-0.01"));
        prestamo.setPlazo_meses(0);
        var paths = factory.getValidator().validate(prestamo).stream()
                .map(v -> v.getPropertyPath().toString()).toList();
        for (var field : new String[]{"socio_id", "producto_id", "monto_solicitado",
                "monto_aprobado", "tasa_interes", "plazo_meses"}) {
            assertTrue(paths.contains(field), field);
        }
    }

    @Test void rejectsExcessMonetaryPrecision() {
        var prestamo = solicitud();
        prestamo.setMonto_solicitado(new BigDecimal("1000.001"));
        assertFalse(factory.getValidator().validate(prestamo).isEmpty());
        prestamo.setMonto_solicitado(new BigDecimal("10000000000000000"));
        assertFalse(factory.getValidator().validate(prestamo).isEmpty());
    }

    @Test void enforcesChronologicalDates() {
        var prestamo = solicitud();
        var start = prestamo.getFecha_solicitud();
        prestamo.setFecha_aprobacion(start.minusSeconds(1));
        assertFalse(factory.getValidator().validate(prestamo).isEmpty());
        prestamo.setFecha_aprobacion(null);
        prestamo.setFecha_desembolso(start);
        assertFalse(factory.getValidator().validate(prestamo).isEmpty());
        prestamo.setFecha_aprobacion(start.plusDays(1));
        assertFalse(factory.getValidator().validate(prestamo).isEmpty());
        prestamo.setFecha_desembolso(start.plusDays(1));
        assertTrue(factory.getValidator().validate(prestamo).isEmpty());
    }

    @Test void callbacksPreserveExplicitValuesAndCreationTimestamp() {
        var past = LocalDateTime.of(2020, 1, 1, 0, 0);
        var prestamo = Prestamos.builder().fecha_solicitud(past)
                .estado(EstadoPrestamos.EN_REVISION).build();
        prestamo.prePersist();
        assertEquals(past, prestamo.getFecha_solicitud());
        assertEquals(EstadoPrestamos.EN_REVISION, prestamo.getEstado());
        prestamo.setCreatedAt(past);
        prestamo.setUpdatedAt(past);
        prestamo.preUpdate();
        assertEquals(past, prestamo.getCreatedAt());
        assertTrue(prestamo.getUpdatedAt().isAfter(past));
    }
}
