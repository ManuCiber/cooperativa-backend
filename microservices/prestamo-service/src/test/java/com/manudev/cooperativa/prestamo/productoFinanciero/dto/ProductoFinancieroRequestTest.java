package com.manudev.cooperativa.prestamo.productoFinanciero.dto;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ProductoFinancieroRequestTest {
    private static ValidatorFactory factory;
    private static Validator validator;

    @BeforeAll
    static void setUp() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void tearDown() {
        factory.close();
    }

    @Test
    void acceptsValuesAtDatabasePrecisionLimits() {
        var request = validRequest();
        request.setTasaInteres(new BigDecimal("999.99"));
        request.setMontoMinimo(new BigDecimal("9999999999.99"));
        request.setMontoMaximo(new BigDecimal("9999999999.99"));

        assertTrue(validator.validate(request).isEmpty());
    }

    @Test
    void rejectsInterestRateOutsideDatabasePrecision() {
        var tooManyIntegerDigits = validRequest();
        tooManyIntegerDigits.setTasaInteres(new BigDecimal("1000.00"));
        assertHasViolation(tooManyIntegerDigits, "tasaInteres");

        var tooManyFractionDigits = validRequest();
        tooManyFractionDigits.setTasaInteres(new BigDecimal("1.001"));
        assertHasViolation(tooManyFractionDigits, "tasaInteres");
    }

    @Test
    void rejectsAmountsOutsideDatabasePrecision() {
        var minimumTooLarge = validRequest();
        minimumTooLarge.setMontoMinimo(new BigDecimal("10000000000.00"));
        assertHasViolation(minimumTooLarge, "montoMinimo");

        var maximumTooLarge = validRequest();
        maximumTooLarge.setMontoMaximo(new BigDecimal("10000000000.00"));
        assertHasViolation(maximumTooLarge, "montoMaximo");

        var amountWithTooManyFractionDigits = validRequest();
        amountWithTooManyFractionDigits.setMontoMinimo(new BigDecimal("100.001"));
        assertHasViolation(amountWithTooManyFractionDigits, "montoMinimo");
    }

    private static ProductoFinancieroRequest validRequest() {
        return ProductoFinancieroRequest.builder()
                .nombre("Credito Personal")
                .tasaInteres(new BigDecimal("12.50"))
                .plazoMinimo(1)
                .plazoMaximo(60)
                .montoMinimo(new BigDecimal("100.00"))
                .montoMaximo(new BigDecimal("50000.00"))
                .build();
    }

    private static void assertHasViolation(ProductoFinancieroRequest request, String property) {
        assertTrue(validator.validate(request).stream()
                .anyMatch(violation -> violation.getPropertyPath().toString().equals(property)));
    }
}
