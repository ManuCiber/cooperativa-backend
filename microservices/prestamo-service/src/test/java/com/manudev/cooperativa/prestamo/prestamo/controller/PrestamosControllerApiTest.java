package com.manudev.cooperativa.prestamo.prestamo.controller;

import com.manudev.cooperativa.prestamo.prestamo.dto.PrestamosResponse;
import com.manudev.cooperativa.prestamo.prestamo.entities.EstadoPrestamos;
import com.manudev.cooperativa.prestamo.prestamo.service.IPrestamosService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PrestamosControllerApiTest {
    private IPrestamosService service;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        service = mock(IPrestamosService.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new PrestamosController(service)).build();
    }

    @Test
    void approvesLoanUsingTheDocumentedEndpointAndAmountParameter() throws Exception {
        when(service.aprobarPrestamo(42L, new BigDecimal("800.00")))
                .thenReturn(PrestamosResponse.builder()
                        .prestamoId(42L)
                        .monto_aprobado(new BigDecimal("800.00"))
                        .estado(EstadoPrestamos.APROBADO)
                        .build());

        mockMvc.perform(patch("/api/prestamos/42/aprobar").param("montoAprobado", "800.00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.prestamoId").value(42))
                .andExpect(jsonPath("$.monto_aprobado").value(800.00))
                .andExpect(jsonPath("$.estado").value("APROBADO"));

        verify(service).aprobarPrestamo(42L, new BigDecimal("800.00"));
    }

    @Test
    void disbursesLoanUsingTheDocumentedEndpoint() throws Exception {
        when(service.desembolsarPrestamo(42L)).thenReturn(PrestamosResponse.builder()
                .prestamoId(42L)
                .estado(EstadoPrestamos.ACTIVO)
                .build());

        mockMvc.perform(patch("/api/prestamos/42/desembolsar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("ACTIVO"));

        verify(service).desembolsarPrestamo(42L);
    }
}
