package com.manudev.microservices.cuentas.transacciones.entities;

import com.manudev.microservices.cuentas.cuentas.entities.Cuentas;
import jakarta.validation.Validation;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

class TransaccionesTest {
    static ValidatorFactory factory;
    @BeforeAll static void setup() { factory = Validation.buildDefaultValidatorFactory(); }
    @AfterAll static void close() { factory.close(); }

    Transacciones valid() {
        return Transacciones.builder().cuenta(Cuentas.builder().id(1L).build())
                .tipo_transaccion(TipoTransacciones.DEPOSITO)
                .monto(new BigDecimal("10.00")).build();
    }
    boolean invalidField(Transacciones transaction, String field) {
        return factory.getValidator().validate(transaction).stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals(field));
    }
    @Test void assignsDateBeforePersistence() {
        var transaction = valid();
        var before = LocalDateTime.now();
        transaction.prePersist();
        var after = LocalDateTime.now();
        assertFalse(transaction.getFecha().isBefore(before));
        assertFalse(transaction.getFecha().isAfter(after));
        assertTrue(factory.getValidator().validate(transaction).isEmpty());
    }
    @Test void preservesExplicitDate() {
        var transaction = valid();
        var date = LocalDateTime.of(2026, 1, 1, 12, 30);
        transaction.setFecha(date);
        transaction.prePersist();
        assertEquals(date, transaction.getFecha());
    }
    @ParameterizedTest @NullSource
    @ValueSource(strings = {"0", "-0.01", "1.001", "10000000000000000.00"})
    void rejectsInvalidAmount(String amount) {
        var transaction = valid();
        transaction.setMonto(amount == null ? null : new BigDecimal(amount));
        assertTrue(invalidField(transaction, "monto"));
    }
    @ParameterizedTest @ValueSource(strings = {"0.01", "9999999999999999.99"})
    void acceptsAmountBoundaries(String amount) {
        var transaction = valid();
        transaction.setMonto(new BigDecimal(amount));
        transaction.prePersist();
        assertTrue(factory.getValidator().validate(transaction).isEmpty());
    }
    @Test void requiresAccountAndType() {
        var transaction = valid();
        transaction.setCuenta(null);
        transaction.setTipo_transaccion(null);
        assertTrue(invalidField(transaction, "cuenta"));
        assertTrue(invalidField(transaction, "tipo_transaccion"));
    }
    @Test void transferRequiresDestination() {
        var transaction = valid();
        transaction.setTipo_transaccion(TipoTransacciones.TRANSFERENCIA);
        assertTrue(invalidField(transaction, "transferenciaValida"));
    }
    @Test void transferRejectsSameAccountEvenWithDifferentObjects() {
        var transaction = valid();
        transaction.setTipo_transaccion(TipoTransacciones.TRANSFERENCIA);
        transaction.setCuenta_destino(Cuentas.builder().id(1L).build());
        assertTrue(invalidField(transaction, "transferenciaValida"));
        transaction.setCuenta_destino(transaction.getCuenta());
        assertTrue(invalidField(transaction, "transferenciaValida"));
    }
    @Test void transferAcceptsDifferentAccounts() {
        var transaction = valid();
        transaction.setTipo_transaccion(TipoTransacciones.TRANSFERENCIA);
        transaction.setCuenta_destino(Cuentas.builder().id(2L).build());
        transaction.prePersist();
        assertTrue(factory.getValidator().validate(transaction).isEmpty());
    }
    @ParameterizedTest
    @EnumSource(value = TipoTransacciones.class, names = {"TRANSFERENCIA"}, mode = EnumSource.Mode.EXCLUDE)
    void otherTypesDoNotRequireDestination(TipoTransacciones type) {
        var transaction = valid();
        transaction.setTipo_transaccion(type);
        transaction.prePersist();
        assertTrue(factory.getValidator().validate(transaction).isEmpty());
    }
    @Test void rejectsDescriptionLongerThanColumn() {
        var transaction = valid();
        transaction.setDescripcion("x".repeat(256));
        assertTrue(invalidField(transaction, "descripcion"));
    }
}
