package com.manudev.microservices.cuentas.transacciones.dto;

import com.manudev.microservices.cuentas.transacciones.entities.TipoTransacciones;
import jakarta.validation.Validation;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;
import tools.jackson.databind.json.JsonMapper;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;

class TransaccionesRequestTest {
    static ValidatorFactory factory;
    @BeforeAll static void setup() { factory = Validation.buildDefaultValidatorFactory(); }
    @AfterAll static void close() { factory.close(); }

    TransaccionesRequest request() {
        return TransaccionesRequest.builder().cuentaId(1L).tipo_transaccion(TipoTransacciones.DEPOSITO)
                .monto(new BigDecimal("10.25")).build();
    }
    @Test void jsonDepositAllowsOmittedDateAndDestination() throws Exception {
        var request = JsonMapper.builder().build().readValue(
                "{\"cuentaId\":1,\"tipo_transaccion\":\"DEPOSITO\",\"monto\":10.25}",
                TransaccionesRequest.class);
        assertTrue(factory.getValidator().validate(request).isEmpty());
        assertNull(request.getFecha());
        assertNull(request.getCuentaDestinoId());
        assertEquals(new BigDecimal("10.25"), request.getMonto());
    }
    @ParameterizedTest @NullSource @ValueSource(longs = {1, 0, -1})
    void rejectsMissingSameOrInvalidTransferDestination(Long destination) {
        var request = request();
        request.setTipo_transaccion(TipoTransacciones.TRANSFERENCIA);
        request.setCuentaDestinoId(destination);
        assertFalse(factory.getValidator().validate(request).isEmpty());
    }
    @Test void acceptsTransferBetweenDifferentAccounts() {
        var request = request();
        request.setTipo_transaccion(TipoTransacciones.TRANSFERENCIA);
        request.setCuentaDestinoId(2L);
        assertTrue(factory.getValidator().validate(request).isEmpty());
    }
    @ParameterizedTest @NullSource @ValueSource(strings = {"0", "-1", "0.001", "10000000000000000"})
    void rejectsInvalidAmount(String value) {
        var request = request();
        request.setMonto(value == null ? null : new BigDecimal(value));
        assertFalse(factory.getValidator().validate(request).isEmpty());
    }
    @Test void requiresAccountAndType() {
        var request = request();
        request.setCuentaId(null);
        request.setTipo_transaccion(null);
        var violations = factory.getValidator().validate(request);
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("cuentaId")));
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("tipo_transaccion")));
    }
    @Test void validationPropertyIsNotExposedInJson() throws Exception {
        assertFalse(JsonMapper.builder().build().writeValueAsString(request()).contains("transferenciaValida"));
    }
}
