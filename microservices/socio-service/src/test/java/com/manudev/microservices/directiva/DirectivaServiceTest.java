package com.manudev.microservices.directiva;

import com.manudev.microservices.directiva.dto.*;
import com.manudev.microservices.directiva.entities.*;
import com.manudev.microservices.directiva.mapper.DirectivaMapper;
import com.manudev.microservices.directiva.repository.DirectivaRepository;
import com.manudev.microservices.directiva.service.impl.DirectivaServiceImpl;
import com.manudev.microservices.socio.entities.Socio;
import com.manudev.microservices.socio.repository.SocioRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;
import org.springframework.web.server.ResponseStatusException;
import java.time.LocalDate;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Stream;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DirectivaServiceTest {
    DirectivaRepository repository;
    SocioRepository socios;
    DirectivaServiceImpl service;
    final LocalDate inicio = LocalDate.of(2026, 1, 1);

    @BeforeEach void setup() {
        repository = mock(DirectivaRepository.class);
        socios = mock(SocioRepository.class);
        service = new DirectivaServiceImpl(repository, socios, new DirectivaMapper());
    }
    DirectivaRequest request() {
        return DirectivaRequest.builder().socioId(2L).cargo("Presidente").fecha_inicio(inicio).build();
    }
    Directiva entity() {
        return Directiva.builder().directiva_id(1L).socio(Socio.builder().socio_id(2L).build())
                .cargo("Presidente").fecha_inicio(inicio).build();
    }
    void saveReturnsEntity() {
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
    }
    @ParameterizedTest @ValueSource(strings = {"ACTIVO", "INACTIVO"})
    void createsWithSelectedState(String estado) {
        var request = request();
        request.setEstado(estado);
        when(socios.findById(2L)).thenReturn(Optional.of(entity().getSocio()));
        when(repository.save(any())).thenAnswer(invocation -> {
            Directiva saved = invocation.getArgument(0);
            saved.setDirectiva_id(9L);
            return saved;
        });
        var response = service.createDirectiva(request);
        assertEquals(9L, response.getDirectivaId());
        assertEquals(2L, response.getSocioId());
        assertEquals(estado, response.getEstado());
        assertEquals(inicio, response.getFecha_inicio());
        assertNull(response.getFecha_final());
    }
    @Test void omittedStateDefaultsToActive() {
        when(socios.findById(2L)).thenReturn(Optional.of(entity().getSocio()));
        saveReturnsEntity();
        assertEquals("ACTIVO", service.createDirectiva(request()).getEstado());
    }
    @Test void missingSocioDoesNotSave() {
        assertStatus(404, () -> service.createDirectiva(request()));
        verifyNoInteractions(repository);
    }
    @Test void listsActiveDirectivas() {
        when(repository.findAllByEstado(EstadoDirectiva.ACTIVO)).thenReturn(List.of(entity()));
        assertEquals(1L, service.getDirectivas().getFirst().getDirectivaId());
        verify(repository).findAllByEstado(EstadoDirectiva.ACTIVO);
        verify(repository, never()).findAll();
    }
    @Test void emptyListIsSupported() {
        assertTrue(service.getDirectivas().isEmpty());
    }
    @Test void findsById() {
        when(repository.findById(1L)).thenReturn(Optional.of(entity()));
        assertEquals(1L, service.getDirectivaById(1L).getDirectivaId());
    }
    @Test void missingDirectivaReturns404ForReadUpdateAndDelete() {
        assertStatus(404, () -> service.getDirectivaById(1L));
        assertStatus(404, () -> service.updateDirectiva(1L, request()));
        assertStatus(404, () -> service.deleteDirectiva(1L));
        verify(repository, never()).save(any());
    }
    @ParameterizedTest @NullSource @ValueSource(longs = {0, -1})
    void invalidIdsReturn400(Long id) {
        assertStatus(400, () -> service.getDirectivaById(id));
        assertStatus(400, () -> service.updateDirectiva(id, request()));
        assertStatus(400, () -> service.deleteDirectiva(id));
        verifyNoInteractions(repository, socios);
    }
    @Test void updatesFieldsAndSocioWithoutChangingId() {
        var stored = entity();
        var request = request();
        request.setSocioId(3L);
        request.setCargo("Secretario");
        request.setEstado("INACTIVO");
        request.setFecha_final(inicio.plusYears(1));
        when(repository.findById(1L)).thenReturn(Optional.of(stored));
        when(socios.findById(3L)).thenReturn(Optional.of(Socio.builder().socio_id(3L).build()));
        saveReturnsEntity();
        var response = service.updateDirectiva(1L, request);
        assertEquals(1L, response.getDirectivaId());
        assertEquals(3L, response.getSocioId());
        assertEquals("Secretario", response.getCargo());
        assertEquals("INACTIVO", response.getEstado());
        assertEquals(inicio.plusYears(1), response.getFecha_final());
    }
    @Test void updateWithMissingSocioLeavesEntityUnchanged() {
        var stored = entity();
        when(repository.findById(1L)).thenReturn(Optional.of(stored));
        assertStatus(404, () -> service.updateDirectiva(1L, request()));
        assertEquals(EstadoDirectiva.ACTIVO, stored.getEstado());
        verify(repository, never()).save(any());
    }
    @Test void logicalDeletionIsRepeatableAndDoesNotDeleteRow() {
        var stored = entity();
        when(repository.findById(1L)).thenReturn(Optional.of(stored));
        saveReturnsEntity();
        assertEquals("INACTIVO", service.deleteDirectiva(1L).getEstado());
        assertEquals("INACTIVO", service.deleteDirectiva(1L).getEstado());
        verify(repository, never()).delete(any());
        verify(repository, never()).deleteById(any());
    }
    static Stream<Consumer<DirectivaRequest>> invalidRequests() {
        return Stream.of(r -> r.setSocioId(null), r -> r.setSocioId(0L),
                r -> r.setCargo(null), r -> r.setCargo(" "), r -> r.setCargo("x".repeat(256)),
                r -> r.setFecha_inicio(null), r -> r.setFecha_final(LocalDate.of(2025, 1, 1)),
                r -> r.setEstado("INVALIDO"), r -> r.setEstado(""));
    }
    @ParameterizedTest @MethodSource("invalidRequests")
    void rejectsInvalidDataBeforePersistence(Consumer<DirectivaRequest> change) {
        var request = request();
        change.accept(request);
        assertStatus(400, () -> service.createDirectiva(request));
        assertStatus(400, () -> service.updateDirectiva(1L, request));
        verifyNoInteractions(repository, socios);
    }
    @Test void rejectsNullRequest() {
        assertStatus(400, () -> service.createDirectiva(null));
        verifyNoInteractions(repository, socios);
    }
    @Test void permitsSameDayPeriodAndMaximumCargoLength() {
        var request = request();
        request.setFecha_final(inicio);
        request.setCargo("x".repeat(255));
        when(socios.findById(2L)).thenReturn(Optional.of(entity().getSocio()));
        saveReturnsEntity();
        assertEquals(inicio, service.createDirectiva(request).getFecha_final());
    }
    static void assertStatus(int status, org.junit.jupiter.api.function.Executable action) {
        assertEquals(status, assertThrows(ResponseStatusException.class, action).getStatusCode().value());
    }
}
