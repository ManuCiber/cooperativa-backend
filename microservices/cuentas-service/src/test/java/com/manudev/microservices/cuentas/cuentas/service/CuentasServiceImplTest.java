package com.manudev.microservices.cuentas.cuentas.service;

import com.manudev.microservices.cuentas.cuentas.dto.*;
import com.manudev.microservices.cuentas.cuentas.entities.*;
import com.manudev.microservices.cuentas.cuentas.mapper.CuentasMapper;
import com.manudev.microservices.cuentas.cuentas.repository.CuentasRepository;
import com.manudev.microservices.cuentas.cuentas.service.impl.CuentasServiceImpl;
import jakarta.validation.Validation;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.*;
import org.springframework.web.server.ResponseStatusException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CuentasServiceImplTest {
    static ValidatorFactory validators;
    CuentasRepository repository;
    ICuentasService service;
    @BeforeAll static void openValidator() {
        validators = Validation.buildDefaultValidatorFactory();
    }
    @AfterAll static void closeValidator() { validators.close(); }
    @BeforeEach void setup() {
        repository = mock(CuentasRepository.class);
        service = new CuentasServiceImpl(repository, new CuentasMapper(), validators.getValidator());
    }
    CuentasRequest request() {
        return CuentasRequest.builder().socioId(2L).numeroCuenta("000123")
                .tipoCuenta(TipoCuenta.AHORRO_ORDINARIO).fecha_apertura(LocalDate.of(2026, 1, 1))
                .saldo_actual(new BigDecimal("123.45")).estado(EstadoCuenta.ACTIVO).build();
    }
    Cuentas stored() {
        var cuenta = new CuentasMapper().toEntity(request());
        cuenta.setId(1L);
        return cuenta;
    }
    void saveReturnsEntity() {
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
    }
    @Test void createsAndMapsAllFieldsWithoutLosingDecimals() {
        when(repository.save(any())).thenAnswer(invocation -> {
            Cuentas cuenta = invocation.getArgument(0);
            cuenta.setId(1L);
            return cuenta;
        });
        var response = service.createCuenta(request());
        assertEquals(1L, response.getId());
        assertEquals(2L, response.getSocioId());
        assertEquals("000123", response.getNumeroCuenta());
        assertEquals(TipoCuenta.AHORRO_ORDINARIO, response.getTipoCuenta());
        assertEquals(LocalDate.of(2026, 1, 1), response.getFecha_apertura());
        assertEquals(new BigDecimal("123.45"), response.getSaldo_actual());
        assertEquals(EstadoCuenta.ACTIVO, response.getEstado());
    }
    @Test void duplicateCreationReturns409AndDoesNotSave() {
        when(repository.existsByNumeroCuenta("000123")).thenReturn(true);
        assertStatus(409, () -> service.createCuenta(request()));
        verify(repository, never()).save(any());
    }
    @Test void updateKeepsIdAndAllowsOwnNumber() {
        var stored = stored();
        when(repository.findById(1L)).thenReturn(Optional.of(stored));
        saveReturnsEntity();
        var request = request();
        request.setSocioId(3L);
        request.setTipoCuenta(TipoCuenta.AHORRO_PROGRAMADO);
        request.setSaldo_actual(new BigDecimal("999.99"));
        request.setEstado(EstadoCuenta.INACTIVO);
        var response = service.updateCuenta(1L, request);
        assertEquals(1L, response.getId());
        assertEquals(3L, response.getSocioId());
        assertEquals(TipoCuenta.AHORRO_PROGRAMADO, response.getTipoCuenta());
        assertEquals(new BigDecimal("999.99"), response.getSaldo_actual());
        assertEquals(EstadoCuenta.INACTIVO, response.getEstado());
        verify(repository).existsByNumeroCuentaAndIdNot("000123", 1L);
    }
    @Test void duplicateUpdateDoesNotMutateStoredAccount() {
        var stored = stored();
        var request = request();
        request.setNumeroCuenta("OTRA");
        when(repository.findById(1L)).thenReturn(Optional.of(stored));
        when(repository.existsByNumeroCuentaAndIdNot("OTRA", 1L)).thenReturn(true);
        assertStatus(409, () -> service.updateCuenta(1L, request));
        assertEquals("000123", stored.getNumeroCuenta());
        verify(repository, never()).save(any());
    }
    @Test void listsActiveAndFindsById() {
        when(repository.findAllByEstado(EstadoCuenta.ACTIVO)).thenReturn(List.of(stored()));
        when(repository.findById(1L)).thenReturn(Optional.of(stored()));
        assertEquals(1L, service.getCuentas().get(0).getId());
        assertEquals(1L, service.getCuentaById(1L).getId());
        verify(repository, never()).findAll();
    }

    @Test void listsInactiveAccountsSeparately() {
        var inactive = stored();
        inactive.setEstado(EstadoCuenta.INACTIVO);
        when(repository.findAllByEstado(EstadoCuenta.INACTIVO)).thenReturn(List.of(inactive));

        var result = service.getCuentasInactivas();

        assertEquals(1, result.size());
        assertEquals(EstadoCuenta.INACTIVO, result.get(0).getEstado());
        verify(repository).findAllByEstado(EstadoCuenta.INACTIVO);
    }
    @Test void missingAccountReturns404ForReadUpdateAndDelete() {
        assertStatus(404, () -> service.getCuentaById(1L));
        assertStatus(404, () -> service.updateCuenta(1L, request()));
        assertStatus(404, () -> service.deleteCuenta(1L));
        verify(repository, never()).save(any());
    }
    @Test void invalidIdsDoNotReachRepository() {
        for (Long id : Arrays.asList(null, 0L, -1L)) {
            assertStatus(400, () -> service.getCuentaById(id));
            assertStatus(400, () -> service.updateCuenta(id, request()));
            assertStatus(400, () -> service.deleteCuenta(id));
        }
        verifyNoInteractions(repository);
    }
    @Test void deletionOnlyChangesStateAndIsRepeatable() {
        var stored = stored();
        when(repository.findById(1L)).thenReturn(Optional.of(stored));
        saveReturnsEntity();
        assertEquals(EstadoCuenta.INACTIVO, service.deleteCuenta(1L).getEstado());
        assertEquals(EstadoCuenta.INACTIVO, service.deleteCuenta(1L).getEstado());
        assertEquals(new BigDecimal("123.45"), stored.getSaldo_actual());
        verify(repository, never()).delete(any());
        verify(repository, never()).deleteById(any());
    }

    @Test void invalidRequestReturns400BeforePersistence() {
        assertStatus(400, () -> service.createCuenta(null));
        assertStatus(400, () -> service.createCuenta(new CuentasRequest()));
        var request = request();
        request.setSaldo_actual(new BigDecimal("1.001"));
        assertStatus(400, () -> service.createCuenta(request));
        assertStatus(400, () -> service.updateCuenta(1L, request));
        request.setSaldo_actual(new BigDecimal("10000000000000000.00"));
        assertStatus(400, () -> service.createCuenta(request));
        verifyNoInteractions(repository);
    }
    @Test void omittedTypeInJsonCreatesOrdinarySavingsAccount() throws Exception {
        var mapper = tools.jackson.databind.json.JsonMapper.builder().build();
        var request = mapper.readValue("""
                {"socioId":2,"numeroCuenta":"000123","fecha_apertura":"2026-01-01",
                 "saldo_actual":0,"estado":"ACTIVO"}
                """, CuentasRequest.class);
        saveReturnsEntity();
        assertEquals(TipoCuenta.AHORRO_ORDINARIO, service.createCuenta(request).getTipoCuenta());
    }
    @Test void explicitTypeInJsonIsPreserved() throws Exception {
        var mapper = tools.jackson.databind.json.JsonMapper.builder().build();
        var request = mapper.readValue("""
                {"socioId":2,"numeroCuenta":"000123","fecha_apertura":"2026-01-01",
                 "saldo_actual":0,"estado":"ACTIVO","tipoCuenta":"AHORRO_PROGRAMADO"}
                """, CuentasRequest.class);
        saveReturnsEntity();
        assertEquals(TipoCuenta.AHORRO_PROGRAMADO, service.createCuenta(request).getTipoCuenta());
    }
    @Test void buildersAndConstructorsUseOrdinarySavingsByDefault() {
        assertEquals(TipoCuenta.AHORRO_ORDINARIO, new CuentasRequest().getTipoCuenta());
        assertEquals(TipoCuenta.AHORRO_ORDINARIO, CuentasRequest.builder().build().getTipoCuenta());
        assertEquals(TipoCuenta.AHORRO_ORDINARIO, new Cuentas().getTipoCuenta());
        assertEquals(TipoCuenta.AHORRO_ORDINARIO, Cuentas.builder().build().getTipoCuenta());
    }
    static void assertStatus(int expected, org.junit.jupiter.api.function.Executable action) {
        assertEquals(expected, assertThrows(ResponseStatusException.class, action).getStatusCode().value());
    }
}
