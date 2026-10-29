package com.manudev.microservices.directiva;

import com.manudev.microservices.directiva.entities.Directiva;
import com.manudev.microservices.directiva.entities.EstadoDirectiva;
import jakarta.validation.Validation;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class DirectivaEntityTest {
    private static ValidatorFactory factory;

    @BeforeAll
    static void setUpValidator() {
        factory = Validation.buildDefaultValidatorFactory();
    }

    @AfterAll
    static void closeValidator() {
        factory.close();
    }

    @Test
    void defaultsStateToActive() {
        Directiva directiva = Directiva.builder().build();

        assertEquals(EstadoDirectiva.ACTIVO, directiva.getEstado());
        directiva.setEstado(null);
        directiva.prePersist();
        assertEquals(EstadoDirectiva.ACTIVO, directiva.getEstado());
    }

    @Test
    void acceptsOpenPeriod() {
        Directiva directiva = Directiva.builder()
                .fecha_inicio(LocalDate.of(2026, 1, 1))
                .build();

        assertTrue(directiva.isPeriodoValido());
        assertTrue(factory.getValidator().validate(directiva).stream()
                .noneMatch(v -> v.getPropertyPath().toString().equals("periodoValido")));
    }

    @Test
    void acceptsFinalDateEqualToStartDate() {
        LocalDate date = LocalDate.of(2026, 1, 1);
        Directiva directiva = Directiva.builder()
                .fecha_inicio(date)
                .fecha_final(date)
                .build();

        assertTrue(directiva.isPeriodoValido());
    }

    @Test
    void rejectsFinalDateBeforeStartDate() {
        Directiva directiva = Directiva.builder()
                .fecha_inicio(LocalDate.of(2026, 2, 1))
                .fecha_final(LocalDate.of(2026, 1, 31))
                .build();

        assertFalse(directiva.isPeriodoValido());
        assertTrue(factory.getValidator().validate(directiva).stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("periodoValido")));
    }

    @Test
    void rejectsCargoLongerThanDatabaseColumn() {
        Directiva directiva = Directiva.builder()
                .cargo("x".repeat(256))
                .build();

        assertTrue(factory.getValidator().validate(directiva).stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("cargo")));
    }
}
