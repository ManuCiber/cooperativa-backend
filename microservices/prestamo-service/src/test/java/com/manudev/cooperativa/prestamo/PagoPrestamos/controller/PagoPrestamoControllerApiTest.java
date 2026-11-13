package com.manudev.cooperativa.prestamo.PagoPrestamos.controller;

import com.manudev.cooperativa.prestamo.PagoPrestamos.dto.PagoPrestamosResponse;
import com.manudev.cooperativa.prestamo.PagoPrestamos.entities.EstadoPagosPrestamo;
import com.manudev.cooperativa.prestamo.PagoPrestamos.services.PagoPrestamosService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PagoPrestamoControllerApiTest {
    private PagoPrestamosService service;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        service = mock(PagoPrestamosService.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new PagoPrestamoController(service)).build();
    }

    @Test
    void exposesPrincipalAndInterestOnTheInstallmentPaymentResponse() throws Exception {
        when(service.registrarPago(17L)).thenReturn(PagoPrestamosResponse.builder()
                .pagoId(17L)
                .prestamoId(42L)
                .numeroCuota(1)
                .montoCuota(new BigDecimal("100.00"))
                .montoCapital(new BigDecimal("90.00"))
                .montoInteres(new BigDecimal("10.00"))
                .fechaVencimiento(LocalDate.of(2025, 6, 15))
                .estado(EstadoPagosPrestamo.PAGADO)
                .build());

        mockMvc.perform(patch("/api/pago-prestamo/17/pagar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.montoCapital").value(90.00))
                .andExpect(jsonPath("$.montoInteres").value(10.00))
                .andExpect(jsonPath("$.estado").value("PAGADO"));

        verify(service).registrarPago(17L);
    }

    @Test
    void rejectsManualInstallmentCreationAndDeletionAsConflicts() throws Exception {
        when(service.crear(org.mockito.ArgumentMatchers.any())).thenThrow(
                new ResponseStatusException(HttpStatus.CONFLICT, "Cronograma automatico"));
        doThrow(new ResponseStatusException(HttpStatus.CONFLICT, "Cuotas no eliminables"))
                .when(service).eliminarPago(17L);

        mockMvc.perform(post("/api/pago-prestamo")
                        .contentType("application/json")
                        .content("""
                                {
                                  "prestamoId": 42,
                                  "numeroCuota": 1,
                                  "montoCuota": 100.00,
                                  "montoInteres": 10.00,
                                  "fechaVencimiento": "2025-06-15"
                                }
                                """))
                .andExpect(status().isConflict());
        mockMvc.perform(delete("/api/pago-prestamo/17"))
                .andExpect(status().isConflict());
    }
}
