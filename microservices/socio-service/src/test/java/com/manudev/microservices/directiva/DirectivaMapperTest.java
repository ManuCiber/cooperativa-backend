package com.manudev.microservices.directiva;

import com.manudev.microservices.directiva.dto.DirectivaRequest;
import com.manudev.microservices.directiva.entities.*;
import com.manudev.microservices.directiva.mapper.DirectivaMapper;
import com.manudev.microservices.socio.entities.Socio;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;

class DirectivaMapperTest {
    final DirectivaMapper mapper = new DirectivaMapper();

    @Test void mapsAllFieldsAndEnumInBothDirections() {
        var socio = Socio.builder().socio_id(7L).build();
        var request = DirectivaRequest.builder().socioId(7L).cargo("Secretario")
                .fecha_inicio(LocalDate.of(2026, 1, 1)).fecha_final(LocalDate.of(2027, 1, 1))
                .estado("INACTIVO").build();
        var entity = mapper.toEntity(request, socio);
        entity.setDirectiva_id(4L);
        assertSame(socio, entity.getSocio());
        assertEquals(EstadoDirectiva.INACTIVO, entity.getEstado());
        var response = mapper.toResponse(entity);
        assertEquals(4L, response.getDirectivaId());
        assertEquals(7L, response.getSocioId());
        assertEquals(request.getCargo(), response.getCargo());
        assertEquals(request.getFecha_inicio(), response.getFecha_inicio());
        assertEquals(request.getFecha_final(), response.getFecha_final());
        assertEquals("INACTIVO", response.getEstado());
    }
    @Test void nullStateUsesActive() {
        var request = DirectivaRequest.builder().estado(null).build();
        assertEquals(EstadoDirectiva.ACTIVO, mapper.toEntity(request, new Socio()).getEstado());
    }
    @Test void invalidStateDoesNotPartiallyUpdateEntity() {
        var entity = Directiva.builder().directiva_id(8L).cargo("Presidente").build();
        var request = DirectivaRequest.builder().cargo("Secretario").estado("INVALIDO").build();
        assertThrows(IllegalArgumentException.class, () -> mapper.updateEntity(entity, request, new Socio()));
        assertEquals("Presidente", entity.getCargo());
        assertEquals(8L, entity.getDirectiva_id());
    }
    @Test void lifecycleDefaultsOnlyNullState() {
        var entity = Directiva.builder().estado(null).build();
        entity.prePersist();
        assertEquals(EstadoDirectiva.ACTIVO, entity.getEstado());
        entity.setEstado(EstadoDirectiva.INACTIVO);
        entity.prePersist();
        assertEquals(EstadoDirectiva.INACTIVO, entity.getEstado());
        assertEquals(EstadoDirectiva.ACTIVO, new Directiva().getEstado());
        assertEquals(EstadoDirectiva.ACTIVO, Directiva.builder().build().getEstado());
    }
}
