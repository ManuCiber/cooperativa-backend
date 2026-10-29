package com.manudev.microservices.cuentas.transacciones.controller;

import com.manudev.microservices.cuentas.cuentas.entities.*;
import com.manudev.microservices.cuentas.cuentas.repository.CuentasRepository;
import com.manudev.microservices.cuentas.transacciones.entities.*;
import com.manudev.microservices.cuentas.transacciones.mapper.TransaccionesMapper;
import com.manudev.microservices.cuentas.transacciones.repository.TransaccionesRepository;
import com.manudev.microservices.cuentas.transacciones.service.impl.TransaccionesServiceImpl;
import jakarta.validation.Validation;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import java.math.BigDecimal;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class TransaccionesHistorialTest {
    static ValidatorFactory factory;
    CuentasRepository cuentas;
    TransaccionesRepository transactions;
    MockMvc mvc;
    @BeforeAll static void init() { factory = Validation.buildDefaultValidatorFactory(); }
    @AfterAll static void close() { factory.close(); }
    @BeforeEach void setup() {
        cuentas = mock(CuentasRepository.class);
        transactions = mock(TransaccionesRepository.class);
        var service = new TransaccionesServiceImpl(transactions, cuentas, new TransaccionesMapper(), factory.getValidator());
        mvc = MockMvcBuilders.standaloneSetup(new TransaccionesController(service)).build();
    }
    Cuentas account(long id, long socio) {
        return Cuentas.builder().id(id).socio_id(socio).estado(EstadoCuenta.ACTIVO)
                .saldo_actual(new BigDecimal("100.00")).build();
    }
    @Test void recordsBothOwnersAndPreservesThemAfterReassignment() throws Exception {
        var origen = account(1, 10);
        var destino = account(2, 20);
        when(cuentas.findByIdForUpdate(1L)).thenReturn(Optional.of(origen));
        when(cuentas.findByIdForUpdate(2L)).thenReturn(Optional.of(destino));
        var saved = new Transacciones[1];
        when(transactions.save(any())).thenAnswer(call -> {
            saved[0] = call.getArgument(0);
            saved[0].setTransaccion_id(7L);
            saved[0].prePersist();
            return saved[0];
        });
        mvc.perform(post("/api/transacciones").contentType(MediaType.APPLICATION_JSON).content("""
                {"cuentaId":1,"cuentaDestinoId":2,"tipo_transaccion":"TRANSFERENCIA","monto":10}
                """)).andExpect(status().isCreated())
                .andExpect(jsonPath("$.socioId").value(10))
                .andExpect(jsonPath("$.socioDestinoId").value(20))
                .andExpect(jsonPath("$.tipo_transaccion").value("TRANSFERENCIA"));
        assertNull(saved[0].getRealizadaPorUsuarioId());
        origen.setSocio_id(30L);
        destino.setSocio_id(40L);
        when(transactions.findById(7L)).thenReturn(Optional.of(saved[0]));
        mvc.perform(get("/api/transacciones/7")).andExpect(status().isOk())
                .andExpect(jsonPath("$.socioId").value(10))
                .andExpect(jsonPath("$.socioDestinoId").value(20));
    }
    @Test void legacyRecordDoesNotInventHistoricalOwnerOrAuthor() throws Exception {
        var transaction = Transacciones.builder().transaccion_id(7L).cuenta(account(1, 99))
                .tipo_transaccion(TipoTransacciones.DEPOSITO).build();
        when(transactions.findById(7L)).thenReturn(Optional.of(transaction));
        mvc.perform(get("/api/transacciones/7")).andExpect(status().isOk())
                .andExpect(jsonPath("$.socioId").isEmpty())
                .andExpect(jsonPath("$.realizadaPorUsuarioId").isEmpty());
    }
    @Test void requestCannotForgeAuthorOrHistoricalOwners() throws Exception {
        when(cuentas.findByIdForUpdate(1L)).thenReturn(Optional.of(account(1, 10)));
        var saved = new Transacciones[1];
        when(transactions.save(any())).thenAnswer(call -> {
            saved[0] = call.getArgument(0);
            saved[0].prePersist();
            return saved[0];
        });
        var result = mvc.perform(post("/api/transacciones").contentType(MediaType.APPLICATION_JSON).content("""
                {"cuentaId":1,"tipo_transaccion":"DEPOSITO","monto":10,
                 "socioId":999,"socioDestinoId":888,"realizadaPorUsuarioId":777}
                """)).andReturn();
        // Jackson puede rechazar campos desconocidos o ignorarlos segun su configuracion.
        if (result.getResponse().getStatus() == 400) {
            verify(transactions, never()).save(any());
        } else {
            assertEquals(201, result.getResponse().getStatus());
            assertEquals(10L, saved[0].getSocioId());
            assertNull(saved[0].getSocioDestinoId());
            assertNull(saved[0].getRealizadaPorUsuarioId());
        }
    }
    @Test void combinesOwnerAndTypeFilters() throws Exception {
        when(transactions.findByFiltros(10L, TipoTransacciones.RETIRO)).thenReturn(List.of());
        mvc.perform(get("/api/transacciones").param("socioId", "10").param("tipo", "RETIRO"))
                .andExpect(status().isOk()).andExpect(content().json("[]"));
        verify(transactions).findByFiltros(10L, TipoTransacciones.RETIRO);
    }
    @Test void acceptsEachFilterSeparately() throws Exception {
        mvc.perform(get("/api/transacciones").param("socioId", "20")).andExpect(status().isOk());
        mvc.perform(get("/api/transacciones").param("tipo", "DEPOSITO")).andExpect(status().isOk());
        verify(transactions).findByFiltros(20L, null);
        verify(transactions).findByFiltros(null, TipoTransacciones.DEPOSITO);
    }
    @ParameterizedTest @ValueSource(strings={"0","-1","abc"})
    void rejectsInvalidSocioFilter(String socioId) throws Exception {
        mvc.perform(get("/api/transacciones").param("socioId", socioId)).andExpect(status().isBadRequest());
        verifyNoInteractions(transactions);
    }
    @Test void rejectsUnknownType() throws Exception {
        mvc.perform(get("/api/transacciones").param("tipo", "OTRO")).andExpect(status().isBadRequest());
        verifyNoInteractions(transactions);
    }
}
