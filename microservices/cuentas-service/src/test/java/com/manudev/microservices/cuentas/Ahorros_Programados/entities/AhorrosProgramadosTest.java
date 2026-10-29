package com.manudev.microservices.cuentas.Ahorros_Programados.entities;

import com.manudev.microservices.cuentas.Ahorros_Programados.dto.AhorrosProgramadosRequest;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

class AhorrosProgramadosTest {
    @Test void initializesAuditAndDefaultsOnCreation() {
        var ahorro = new AhorrosProgramados();
        ahorro.prePersist();
        assertNotNull(ahorro.getCreatedAt());
        assertEquals(ahorro.getCreatedAt(), ahorro.getUpdatedAt());
        assertEquals(ahorro.getCreatedAt(), ahorro.getFecha_inicio());
        assertEquals(EstadoAhorros.ACTIVO, ahorro.getEstado());
    }

    @Test void preservesExplicitValuesOnCreation() {
        var inicio = LocalDateTime.of(2026, 10, 1, 0, 0);
        var ahorro = AhorrosProgramados.builder()
                .fecha_inicio(inicio).estado(EstadoAhorros.PAUSADO).build();
        ahorro.prePersist();
        assertEquals(inicio, ahorro.getFecha_inicio());
        assertEquals(EstadoAhorros.PAUSADO, ahorro.getEstado());
    }

    @Test void updatePreservesCreationAndBusinessDates() {
        var original = LocalDateTime.of(2020, 1, 1, 0, 0);
        var ahorro = AhorrosProgramados.builder().createdAt(original)
                .updatedAt(original).fecha_inicio(original).build();
        ahorro.preUpdate();
        assertEquals(original, ahorro.getCreatedAt());
        assertEquals(original, ahorro.getFecha_inicio());
        assertTrue(ahorro.getUpdatedAt().isAfter(original));
    }

    @Test void requestDefaultsWorkWithConstructorAndBuilder() {
        var direct = new AhorrosProgramadosRequest();
        var built = AhorrosProgramadosRequest.builder().build();
        assertNotNull(direct.getFecha_inicio());
        assertNotNull(built.getFecha_inicio());
        assertEquals(EstadoAhorros.ACTIVO, direct.getEstado());
        assertEquals(EstadoAhorros.ACTIVO, built.getEstado());
    }
}
