package com.manudev.microservices.usuarios_service.empleados.service;

import com.manudev.microservices.usuarios_service.empleados.dto.EmpleadoRequest;
import com.manudev.microservices.usuarios_service.empleados.entities.Empleado;
import com.manudev.microservices.usuarios_service.empleados.entities.EstadoEmpleado;
import com.manudev.microservices.usuarios_service.empleados.repository.EmpleadoRepository;
import com.manudev.microservices.usuarios_service.empleados.service.impl.EmpleadoServiceImpl;
import jakarta.validation.Validation;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class EmpleadoServiceImplTest {
    private static final ValidatorFactory VALIDATOR_FACTORY = Validation.buildDefaultValidatorFactory();

    private EmpleadoRepository repository;
    private IEmpleadoService service;

    @AfterAll
    static void closeValidatorFactory() {
        VALIDATOR_FACTORY.close();
    }

    @BeforeEach
    void setUp() {
        repository = mock(EmpleadoRepository.class);
        service = new EmpleadoServiceImpl(repository, VALIDATOR_FACTORY.getValidator());
        when(repository.save(any(Empleado.class))).thenAnswer(invocation -> {
            Empleado empleado = invocation.getArgument(0);
            if (empleado.getCreatedAt() == null) {
                empleado.prePersist();
            }
            return empleado;
        });
    }

    @Test
    void createsEmpleadoWithGeneratedFieldsAndDefaultState() {
        when(repository.existsByDniCedula("123456789")).thenReturn(false);
        when(repository.save(any(Empleado.class))).thenAnswer(invocation -> {
            Empleado empleado = invocation.getArgument(0);
            empleado.setEmpleado_id(7L);
            empleado.prePersist();
            return empleado;
        });

        var response = service.createEmpleado(validRequest());

        assertEquals(7L, response.getEmpleadoId());
        assertEquals("123456789", response.getDniCedula());
        assertEquals(EstadoEmpleado.ACTIVO, response.getEstado());
        assertNotNull(response.getCreatedAt());
        assertEquals(response.getCreatedAt(), response.getUpdatedAt());
    }

    @Test
    void rejectsDuplicateDniBeforeSaving() {
        when(repository.existsByDniCedula("123456789")).thenReturn(true);

        assertStatus(HttpStatus.CONFLICT, () -> service.createEmpleado(validRequest()));

        verify(repository, never()).save(any(Empleado.class));
    }

    @Test
    void rejectsInvalidRequestBeforePersistence() {
        EmpleadoRequest request = validRequest();
        request.setSalario(new BigDecimal("-0.01"));

        assertStatus(HttpStatus.BAD_REQUEST, () -> service.createEmpleado(request));

        verifyNoInteractions(repository);
    }

    @Test
    void rejectsNullRequest() {
        assertStatus(HttpStatus.BAD_REQUEST, () -> service.createEmpleado(null));

        verifyNoInteractions(repository);
    }

    @Test
    void listsAndRetrievesEmployees() {
        Empleado empleado = storedEmpleado();
        when(repository.findAll()).thenReturn(List.of(empleado));
        when(repository.findById(4L)).thenReturn(Optional.of(empleado));

        assertEquals(4L, service.getEmpleados().getFirst().getEmpleadoId());
        assertEquals("Ana", service.getEmpleadoById(4L).getNombre());
    }

    @Test
    void rejectsInvalidOrMissingIds() {
        when(repository.findById(10L)).thenReturn(Optional.empty());

        assertStatus(HttpStatus.BAD_REQUEST, () -> service.getEmpleadoById(0L));
        assertStatus(HttpStatus.NOT_FOUND, () -> service.getEmpleadoById(10L));
        verify(repository, never()).findById(0L);
    }

    @Test
    void updatesEmployeeAndPreservesCreationTimestamp() {
        Empleado stored = storedEmpleado();
        LocalDateTime createdAt = stored.getCreatedAt();
        when(repository.findById(4L)).thenReturn(Optional.of(stored));
        when(repository.existsByDniCedula("987654321")).thenReturn(false);

        var request = validRequest();
        request.setDniCedula("987654321");
        request.setNombre("Ana María");
        request.setEstado(EstadoEmpleado.INACTIVO);

        var response = service.updateEmpleado(4L, request);

        assertEquals(4L, response.getEmpleadoId());
        assertEquals("Ana María", response.getNombre());
        assertEquals("987654321", response.getDniCedula());
        assertEquals(EstadoEmpleado.INACTIVO, response.getEstado());
        assertEquals(createdAt, response.getCreatedAt());
        assertFalse(response.getUpdatedAt().isBefore(createdAt));
    }

    @Test
    void allowsKeepingCurrentDniOnUpdate() {
        Empleado stored = storedEmpleado();
        when(repository.findById(4L)).thenReturn(Optional.of(stored));

        service.updateEmpleado(4L, validRequest());

        verify(repository, never()).existsByDniCedula("123456789");
    }

    @Test
    void rejectsDniAlreadyUsedByAnotherEmployeeOnUpdate() {
        Empleado stored = storedEmpleado();
        when(repository.findById(4L)).thenReturn(Optional.of(stored));
        when(repository.existsByDniCedula("987654321")).thenReturn(true);

        var request = validRequest();
        request.setDniCedula("987654321");

        assertStatus(HttpStatus.CONFLICT, () -> service.updateEmpleado(4L, request));
        verify(repository, never()).save(any(Empleado.class));
    }

    @Test
    void logicallyDeletesEmployeeWithoutDeletingRow() {
        Empleado stored = storedEmpleado();
        when(repository.findById(4L)).thenReturn(Optional.of(stored));

        var response = service.deleteEmpleado(4L);

        assertEquals(EstadoEmpleado.INACTIVO, response.getEstado());
        verify(repository, never()).delete(any(Empleado.class));
        verify(repository, never()).deleteById(4L);
    }

    @Test
    void reportsNotFoundWhenDeletingAlreadyInactiveEmployee() {
        Empleado stored = storedEmpleado();
        stored.setEstado(EstadoEmpleado.INACTIVO);
        when(repository.findById(4L)).thenReturn(Optional.of(stored));

        assertStatus(HttpStatus.NOT_FOUND, () -> service.deleteEmpleado(4L));
        verify(repository, never()).save(any(Empleado.class));
    }

    private EmpleadoRequest validRequest() {
        return EmpleadoRequest.builder()
                .nombre("Ana")
                .apellidos("Pérez")
                .dniCedula("123456789")
                .cargo("Asistente")
                .departamento("Administración")
                .salario(new BigDecimal("1250.50"))
                .build();
    }

    private Empleado storedEmpleado() {
        Empleado empleado = Empleado.builder()
                .empleado_id(4L)
                .nombre("Ana")
                .apellidos("Pérez")
                .dni_cedula("123456789")
                .cargo("Asistente")
                .departamento("Administración")
                .salario(new BigDecimal("1250.50"))
                .estado(EstadoEmpleado.ACTIVO)
                .build();
        empleado.prePersist();
        return empleado;
    }

    private void assertStatus(HttpStatus status, Runnable action) {
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, action::run);
        assertEquals(status, exception.getStatusCode());
    }
}
