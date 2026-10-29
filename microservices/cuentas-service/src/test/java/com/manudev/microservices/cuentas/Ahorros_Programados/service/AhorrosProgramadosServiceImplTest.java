package com.manudev.microservices.cuentas.Ahorros_Programados.service;

import com.manudev.microservices.cuentas.Ahorros_Programados.dto.AhorrosProgramadosRequest;
import com.manudev.microservices.cuentas.Ahorros_Programados.entities.*;
import com.manudev.microservices.cuentas.Ahorros_Programados.mapper.AhorrosProgramadosMapper;
import com.manudev.microservices.cuentas.Ahorros_Programados.repository.AhorrosProgramadosRepository;
import com.manudev.microservices.cuentas.Ahorros_Programados.service.impl.AhorrosProgramadosServiceImpl;
import com.manudev.microservices.cuentas.cuentas.entities.Cuentas;
import com.manudev.microservices.cuentas.cuentas.repository.CuentasRepository;
import jakarta.validation.Validation;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.*;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AhorrosProgramadosServiceImplTest {
    static ValidatorFactory validators;
    AhorrosProgramadosRepository repository;
    CuentasRepository cuentas;
    IAhorrosProgramadosService service;
    final AhorrosProgramadosMapper mapper = new AhorrosProgramadosMapper();

    @BeforeAll static void openValidator() {
        validators = Validation.buildDefaultValidatorFactory();
    }

    @AfterAll static void closeValidator() {
        validators.close();
    }

    @BeforeEach void setup() {
        repository = mock(AhorrosProgramadosRepository.class);
        cuentas = mock(CuentasRepository.class);
        service = new AhorrosProgramadosServiceImpl(repository, cuentas, mapper, validators.getValidator());
    }

    AhorrosProgramadosRequest request() {
        return AhorrosProgramadosRequest.builder()
                .socioId(2L).cuentaId(3L).monto_periodico(new BigDecimal("125.50"))
                .periodicidad(FrecuenciaAhorro.MENSUAL)
                .fecha_inicio(LocalDateTime.of(2026, 9, 16, 8, 0))
                .estado(EstadoAhorros.ACTIVO).build();
    }

    Cuentas cuenta() {
        return Cuentas.builder().id(3L).socio_id(2L).build();
    }

    void assertStatus(int status, Runnable action) {
        var error = assertThrows(ResponseStatusException.class, action::run);
        assertEquals(status, error.getStatusCode().value());
    }

    @Test void createsWithExistingAccountAndMapsResponse() {
        when(cuentas.findById(3L)).thenReturn(Optional.of(cuenta()));
        when(repository.save(any())).thenAnswer(invocation -> {
            AhorrosProgramados ahorro = invocation.getArgument(0);
            ahorro.setAhorro_id(1L);
            return ahorro;
        });
        var result = service.createAhorroProgramado(request());
        assertEquals(1L, result.getAhorroId());
        assertEquals(3L, result.getCuentaId());
        assertEquals(2L, result.getSocioId());
        assertEquals(new BigDecimal("125.50"), result.getMonto_periodico());
        assertNull(result.getFecha_fin());
    }

    @Test void rejectsAccountOwnedByAnotherMemberWithoutSaving() {
        var cuenta = cuenta();
        cuenta.setSocio_id(99L);
        when(cuentas.findById(3L)).thenReturn(Optional.of(cuenta));
        assertStatus(400, () -> service.createAhorroProgramado(request()));
        verifyNoInteractions(repository);
    }

    @Test void missingAccountReturns404WithoutSaving() {
        when(cuentas.findById(3L)).thenReturn(Optional.empty());
        assertStatus(404, () -> service.createAhorroProgramado(request()));
        verifyNoInteractions(repository);
    }

    @Test void invalidDatesAndAmountAreRejectedBeforeRepositoryAccess() {
        var request = request();
        request.setFecha_fin(request.getFecha_inicio().minusDays(1));
        assertStatus(400, () -> service.createAhorroProgramado(request));
        request.setFecha_fin(null);
        request.setMonto_periodico(BigDecimal.ZERO);
        assertStatus(400, () -> service.createAhorroProgramado(request));
        assertStatus(400, () -> service.createAhorroProgramado(null));
        verifyNoInteractions(repository, cuentas);
    }

    @Test void updatePreservesIdAndRechecksAccountOwnership() {
        var ahorro = mapper.toEntity(request(), cuenta());
        ahorro.setAhorro_id(1L);
        when(repository.findById(1L)).thenReturn(Optional.of(ahorro));
        var otherAccount = cuenta();
        otherAccount.setSocio_id(99L);
        when(cuentas.findById(3L)).thenReturn(Optional.of(otherAccount));
        assertStatus(400, () -> service.updateAhorroProgramado(1L, request()));
        verify(repository, never()).save(any());
        when(cuentas.findById(3L)).thenReturn(Optional.of(cuenta()));
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        var request = request();
        request.setMonto_periodico(new BigDecimal("200.25"));
        var result = service.updateAhorroProgramado(1L, request);
        assertEquals(1L, result.getAhorroId());
        assertEquals(new BigDecimal("200.25"), result.getMonto_periodico());
    }

    @Test void cancellationPreservesRecord() {
        var ahorro = mapper.toEntity(request(), cuenta());
        ahorro.setAhorro_id(1L);
        when(repository.findById(1L)).thenReturn(Optional.of(ahorro));
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        assertEquals(EstadoAhorros.CANCELADO, service.cancelarAhorroProgramado(1L).getEstado());
        verify(repository, never()).delete(any());
        verify(repository, never()).deleteById(any());
    }

    @Test void invalidAndMissingIdsReturnAppropriateErrors() {
        assertStatus(400, () -> service.getAhorroProgramadoById(0L));
        assertStatus(400, () -> service.getAhorroProgramadoById(null));
        when(repository.findById(1L)).thenReturn(Optional.empty());
        assertStatus(404, () -> service.getAhorroProgramadoById(1L));
    }
}
