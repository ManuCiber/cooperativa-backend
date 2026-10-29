package com.manudev.microservices.cuentas.cuentas.controller;

import com.manudev.microservices.cuentas.cuentas.entities.Cuentas;
import com.manudev.microservices.cuentas.cuentas.entities.EstadoCuenta;
import com.manudev.microservices.cuentas.cuentas.mapper.CuentasMapper;
import com.manudev.microservices.cuentas.cuentas.repository.CuentasRepository;
import com.manudev.microservices.cuentas.cuentas.service.impl.CuentasServiceImpl;
import jakarta.validation.Validation;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class CuentasPropietarioTest {
    static ValidatorFactory validators;
    CuentasRepository repository;
    MockMvc mvc;

    @BeforeAll static void openValidator() {
        validators = Validation.buildDefaultValidatorFactory();
    }

    @AfterAll static void closeValidator() {
        validators.close();
    }

    @BeforeEach void setup() {
        repository = mock(CuentasRepository.class);
        var service = new CuentasServiceImpl(repository, new CuentasMapper(), validators.getValidator());
        mvc = MockMvcBuilders.standaloneSetup(new CuentasController(service)).build();
    }

    String payload(String socioId) {
        return """
                {"socioId":%s,"numeroCuenta":"000123","fecha_apertura":"2026-01-01",
                 "saldo_actual":123.45,"estado":"ACTIVO"}
                """.formatted(socioId);
    }

    Cuentas stored() {
        return Cuentas.builder().id(1L).socio_id(2L).numeroCuenta("000123")
                .fecha_apertura(LocalDate.of(2026, 1, 1))
                .saldo_actual(new BigDecimal("123.45")).estado(EstadoCuenta.ACTIVO).build();
    }

    @Test void creationSendsOwnerToRepositoryAndReturnsItInJson() throws Exception {
        when(repository.save(any())).thenAnswer(invocation -> {
            Cuentas cuenta = invocation.getArgument(0);
            cuenta.setId(1L);
            return cuenta;
        });
        mvc.perform(post("/api/cuentas").contentType(MediaType.APPLICATION_JSON).content(payload("2")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.socioId").value(2))
                .andExpect(jsonPath("$.tipoCuenta").value("AHORRO_ORDINARIO"));
        var captor = ArgumentCaptor.forClass(Cuentas.class);
        verify(repository).save(captor.capture());
        assertEquals(2L, captor.getValue().getSocio_id());
    }

    @Test void detailReturnsAccountOwner() throws Exception {
        when(repository.findById(1L)).thenReturn(Optional.of(stored()));
        mvc.perform(get("/api/cuentas/1")).andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.socioId").value(2));
    }

    @Test void listIdentifiesOwnerOfEachAccount() throws Exception {
        var other = stored();
        other.setId(4L);
        other.setSocio_id(3L);
        other.setNumeroCuenta("000456");
        when(repository.findAllByEstado(EstadoCuenta.ACTIVO)).thenReturn(List.of(stored(), other));
        mvc.perform(get("/api/cuentas")).andExpect(status().isOk())
                .andExpect(jsonPath("$[0].socioId").value(2))
                .andExpect(jsonPath("$[1].socioId").value(3));
    }

    @Test void inactiveAccountsHaveSeparateListEndpoint() throws Exception {
        var inactive = stored();
        inactive.setEstado(EstadoCuenta.INACTIVO);
        when(repository.findAllByEstado(EstadoCuenta.INACTIVO)).thenReturn(List.of(inactive));

        mvc.perform(get("/api/cuentas/inactivas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].estado").value("INACTIVO"));

        verify(repository).findAllByEstado(EstadoCuenta.INACTIVO);
    }

    @Test void reassignmentChangesOwnerAndSubsequentReadReturnsNewOwner() throws Exception {
        var cuenta = stored();
        when(repository.findById(1L)).thenReturn(Optional.of(cuenta));
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        mvc.perform(put("/api/cuentas/1").contentType(MediaType.APPLICATION_JSON).content(payload("3")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.socioId").value(3))
                .andExpect(jsonPath("$.numeroCuenta").value("000123"));
        verify(repository).save(same(cuenta));
        assertEquals(3L, cuenta.getSocio_id());
        assertEquals(new BigDecimal("123.45"), cuenta.getSaldo_actual());
        mvc.perform(get("/api/cuentas/1")).andExpect(status().isOk())
                .andExpect(jsonPath("$.socioId").value(3));
    }

    @ParameterizedTest
    @ValueSource(strings = {"null", "0", "-1", "\"incorrecto\""})
    void invalidOwnerIsRejectedForCreationAndReassignment(String socioId) throws Exception {
        mvc.perform(post("/api/cuentas").contentType(MediaType.APPLICATION_JSON).content(payload(socioId)))
                .andExpect(status().isBadRequest());
        mvc.perform(put("/api/cuentas/1").contentType(MediaType.APPLICATION_JSON).content(payload(socioId)))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(repository);
    }

    @Test void missingOwnerIsRejected() throws Exception {
        String json = payload("2").replace("\"socioId\":2,", "");
        mvc.perform(post("/api/cuentas").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isBadRequest());
        mvc.perform(put("/api/cuentas/1").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(repository);
    }

    @Test void missingAccountCannotBeReassigned() throws Exception {
        mvc.perform(put("/api/cuentas/99").contentType(MediaType.APPLICATION_JSON).content(payload("3")))
                .andExpect(status().isNotFound());
        verify(repository, never()).save(any());
    }

    @Test void duplicateNumberPreventsReassignmentWithoutChangingOwner() throws Exception {
        var cuenta = stored();
        when(repository.findById(1L)).thenReturn(Optional.of(cuenta));
        when(repository.existsByNumeroCuentaAndIdNot("OTRA", 1L)).thenReturn(true);
        mvc.perform(put("/api/cuentas/1").contentType(MediaType.APPLICATION_JSON)
                        .content(payload("3").replace("000123", "OTRA")))
                .andExpect(status().isConflict());
        assertEquals(2L, cuenta.getSocio_id());
        assertEquals("000123", cuenta.getNumeroCuenta());
        verify(repository, never()).save(any());
    }

    @Test void invalidBalancePreventsChangingOwner() throws Exception {
        var cuenta = stored();
        mvc.perform(put("/api/cuentas/1").contentType(MediaType.APPLICATION_JSON)
                        .content(payload("3").replace("123.45", "123.456")))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(repository);
    }

    @Test void logicalDeletionPreservesOwnerAndBalance() throws Exception {
        var cuenta = stored();
        when(repository.findById(1L)).thenReturn(Optional.of(cuenta));
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        mvc.perform(delete("/api/cuentas/1")).andExpect(status().isNoContent())
                .andExpect(content().string(""));
        assertEquals(2L, cuenta.getSocio_id());
        assertEquals(new BigDecimal("123.45"), cuenta.getSaldo_actual());
        assertEquals(EstadoCuenta.INACTIVO, cuenta.getEstado());
        verify(repository, never()).delete(any());
        verify(repository, never()).deleteById(any());
    }

}
