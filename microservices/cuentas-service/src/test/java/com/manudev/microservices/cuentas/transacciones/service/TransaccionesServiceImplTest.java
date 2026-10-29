package com.manudev.microservices.cuentas.transacciones.service;

import com.manudev.microservices.cuentas.cuentas.entities.*;
import com.manudev.microservices.cuentas.cuentas.repository.CuentasRepository;
import com.manudev.microservices.cuentas.transacciones.dto.TransaccionesRequest;
import com.manudev.microservices.cuentas.transacciones.entities.*;
import com.manudev.microservices.cuentas.transacciones.mapper.TransaccionesMapper;
import com.manudev.microservices.cuentas.transacciones.repository.TransaccionesRepository;
import com.manudev.microservices.cuentas.transacciones.service.impl.TransaccionesServiceImpl;
import jakarta.validation.Validation;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.data.domain.Sort;
import org.springframework.web.server.ResponseStatusException;
import java.math.BigDecimal;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TransaccionesServiceImplTest {
    static ValidatorFactory factory;
    CuentasRepository cuentas;
    TransaccionesRepository transactions;
    ITransaccionesService service;
    @BeforeAll static void init() { factory = Validation.buildDefaultValidatorFactory(); }
    @AfterAll static void close() { factory.close(); }
    @BeforeEach void setup() {
        cuentas = mock(CuentasRepository.class);
        transactions = mock(TransaccionesRepository.class);
        service = new TransaccionesServiceImpl(transactions, cuentas, new TransaccionesMapper(), factory.getValidator());
    }
    Cuentas account(long id, String saldo) {
        return Cuentas.builder().id(id).estado(EstadoCuenta.ACTIVO).saldo_actual(new BigDecimal(saldo)).build();
    }
    TransaccionesRequest request(TipoTransacciones type) {
        return TransaccionesRequest.builder().cuentaId(2L).tipo_transaccion(type)
                .monto(new BigDecimal("10.25")).descripcion("Operacion").build();
    }
    void saveTransaction() {
        when(transactions.save(any())).thenAnswer(invocation -> {
            Transacciones transaction = invocation.getArgument(0);
            transaction.setTransaccion_id(9L);
            transaction.prePersist();
            return transaction;
        });
    }
    @ParameterizedTest @EnumSource(value=TipoTransacciones.class, names={"DEPOSITO","AJUSTE_CREDITO"})
    void creditsIncreaseBalance(TipoTransacciones type) {
        var origen = account(2L, "100.00");
        when(cuentas.findByIdForUpdate(2L)).thenReturn(Optional.of(origen));
        saveTransaction();
        var response = service.createTransaccion(request(type));
        assertEquals(new BigDecimal("110.25"), origen.getSaldo_actual());
        assertEquals(9L, response.getTransaccionId());
        assertEquals(type, response.getTipo_transaccion());
        assertEquals(2L, response.getCuentaId());
        assertEquals("Operacion", response.getDescripcion());
        assertNotNull(response.getFecha());
        assertNull(response.getCuentaDestinoId());
        verify(cuentas).save(origen);
    }
    @ParameterizedTest @EnumSource(value=TipoTransacciones.class, names={"RETIRO","AJUSTE_DEBITO"})
    void debitsDecreaseBalance(TipoTransacciones type) {
        var origen = account(2L, "100.00");
        when(cuentas.findByIdForUpdate(2L)).thenReturn(Optional.of(origen));
        saveTransaction();
        service.createTransaccion(request(type));
        assertEquals(new BigDecimal("89.75"), origen.getSaldo_actual());
    }
    @Test void transferConservesTotalAndLocksInAscendingOrder() {
        var origen = account(2L, "100.00");
        var destino = account(1L, "50.00");
        when(cuentas.findByIdForUpdate(2L)).thenReturn(Optional.of(origen));
        when(cuentas.findByIdForUpdate(1L)).thenReturn(Optional.of(destino));
        var request = request(TipoTransacciones.TRANSFERENCIA);
        request.setCuentaDestinoId(1L);
        saveTransaction();
        var response = service.createTransaccion(request);
        assertEquals(new BigDecimal("89.75"), origen.getSaldo_actual());
        assertEquals(new BigDecimal("60.25"), destino.getSaldo_actual());
        assertEquals(new BigDecimal("150.00"), origen.getSaldo_actual().add(destino.getSaldo_actual()));
        assertEquals(1L, response.getCuentaDestinoId());
        var order = inOrder(cuentas);
        order.verify(cuentas).findByIdForUpdate(1L);
        order.verify(cuentas).findByIdForUpdate(2L);
    }
    @ParameterizedTest @EnumSource(value=TipoTransacciones.class, names={"RETIRO","AJUSTE_DEBITO","TRANSFERENCIA"})
    void insufficientBalanceDoesNotWrite(TipoTransacciones type) {
        var origen = account(2L, "1.00");
        when(cuentas.findByIdForUpdate(2L)).thenReturn(Optional.of(origen));
        var request = request(type);
        if(type == TipoTransacciones.TRANSFERENCIA) {
            request.setCuentaDestinoId(3L);
            when(cuentas.findByIdForUpdate(3L)).thenReturn(Optional.of(account(3L, "0")));
        }
        assertStatus(409, () -> service.createTransaccion(request));
        assertEquals(new BigDecimal("1.00"), origen.getSaldo_actual());
        verify(cuentas, never()).save(any());
        verifyNoInteractions(transactions);
    }
    @Test void destinationOverflowLeavesBothBalancesUntouched() {
        var origen = account(2L, "100.00");
        var destino = account(3L, "9999999999999999.99");
        when(cuentas.findByIdForUpdate(2L)).thenReturn(Optional.of(origen));
        when(cuentas.findByIdForUpdate(3L)).thenReturn(Optional.of(destino));
        var request = request(TipoTransacciones.TRANSFERENCIA);
        request.setCuentaDestinoId(3L);
        assertStatus(409, () -> service.createTransaccion(request));
        assertEquals(new BigDecimal("100.00"), origen.getSaldo_actual());
        assertEquals(new BigDecimal("9999999999999999.99"), destino.getSaldo_actual());
        verify(cuentas, never()).save(any());
        verifyNoInteractions(transactions);
    }
    @Test void missingAccountReturns404() {
        assertStatus(404, () -> service.createTransaccion(request(TipoTransacciones.DEPOSITO)));
        verifyNoInteractions(transactions);
    }
    @Test void inactiveAccountIsRejected() {
        var origen = account(2L, "100.00");
        origen.setEstado(EstadoCuenta.INACTIVO);
        when(cuentas.findByIdForUpdate(2L)).thenReturn(Optional.of(origen));
        assertStatus(409, () -> service.createTransaccion(request(TipoTransacciones.DEPOSITO)));
        verify(cuentas, never()).save(any());
        verifyNoInteractions(transactions);
    }
    @Test void invalidRequestsNeverReachDatabase() {
        assertStatus(400, () -> service.createTransaccion(null));
        var request = request(TipoTransacciones.TRANSFERENCIA);
        assertStatus(400, () -> service.createTransaccion(request));
        request.setCuentaDestinoId(2L);
        assertStatus(400, () -> service.createTransaccion(request));
        request.setTipo_transaccion(TipoTransacciones.DEPOSITO);
        assertStatus(400, () -> service.createTransaccion(request));
        request.setCuentaDestinoId(null);
        request.setMonto(BigDecimal.ZERO);
        assertStatus(400, () -> service.createTransaccion(request));
        verifyNoInteractions(cuentas, transactions);
    }
    @Test void readsHistoryAndFindsTransaction() {
        var transaction = Transacciones.builder().transaccion_id(9L).cuenta(account(2L,"100"))
                .tipo_transaccion(TipoTransacciones.DEPOSITO).monto(BigDecimal.TEN).build();
        when(transactions.findById(9L)).thenReturn(Optional.of(transaction));
        when(transactions.findAll(any(Sort.class))).thenReturn(List.of(transaction));
        assertEquals(9L, service.getTransaccionById(9L).getTransaccionId());
        assertEquals(9L, service.getTransacciones().get(0).getTransaccionId());
    }
    @Test void missingOrInvalidTransactionIdIsRejected() {
        assertStatus(404, () -> service.getTransaccionById(9L));
        assertStatus(400, () -> service.getTransaccionById(null));
        assertStatus(400, () -> service.getTransaccionById(0L));
    }
    static void assertStatus(int code, org.junit.jupiter.api.function.Executable action) {
        assertEquals(code, assertThrows(ResponseStatusException.class, action).getStatusCode().value());
    }
}
