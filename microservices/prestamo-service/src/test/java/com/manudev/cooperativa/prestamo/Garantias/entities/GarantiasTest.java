package com.manudev.cooperativa.prestamo.Garantias.entities;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class GarantiasTest {

    @Test
    void prePersistSetsDefaultsAndAuditTimestamps() {
        Garantias garantia = Garantias.builder().build();

        garantia.prePersist();

        assertEquals(TipoGarantia.APORTACIONES, garantia.getTipo_garantia());
        assertEquals(EstadoGarantia.ACTIVO, garantia.getEstado());
        assertNotNull(garantia.getCreateAt());
        assertEquals(garantia.getCreateAt(), garantia.getUpdatedAt());
    }

    @Test
    void prePersistPreservesExplicitTypeAndState() {
        Garantias garantia = Garantias.builder()
                .tipo_garantia(TipoGarantia.HIPOTECA_INMUEBLE)
                .estado(EstadoGarantia.INACTIVO)
                .build();

        garantia.prePersist();

        assertEquals(TipoGarantia.HIPOTECA_INMUEBLE, garantia.getTipo_garantia());
        assertEquals(EstadoGarantia.INACTIVO, garantia.getEstado());
    }

    @Test
    void preUpdateChangesOnlyUpdatedTimestamp() {
        LocalDateTime createdAt = LocalDateTime.of(2025, 1, 1, 0, 0);
        Garantias garantia = Garantias.builder()
                .createAt(createdAt)
                .updatedAt(createdAt)
                .estado(EstadoGarantia.ACTIVO)
                .build();

        garantia.preUpdate();

        assertEquals(createdAt, garantia.getCreateAt());
        assertNotEquals(createdAt, garantia.getUpdatedAt());
        assertEquals(EstadoGarantia.ACTIVO, garantia.getEstado());
    }
}
