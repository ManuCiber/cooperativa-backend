package com.manudev.microservices.directiva;

import com.manudev.microservices.directiva.controller.DirectivaController;
import com.manudev.microservices.directiva.dto.*;
import com.manudev.microservices.directiva.service.IDirectivaService;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class DirectivaControllerTest {
    IDirectivaService service;
    MockMvc mvc;
    final String valid = """
            {"socioId":2,"cargo":"Presidente","fecha_inicio":"2026-01-01"}
            """;
    @BeforeEach void setup() {
        service = mock(IDirectivaService.class);
        mvc = MockMvcBuilders.standaloneSetup(new DirectivaController(service)).build();
    }
    @Test void postReturns201AndDefaultsOmittedState() throws Exception {
        when(service.createDirectiva(any())).thenReturn(DirectivaResponse.builder()
                .directivaId(1L).socioId(2L).estado("ACTIVO").build());
        mvc.perform(post("/api/directivas").contentType(MediaType.APPLICATION_JSON).content(valid))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.directivaId").value(1))
                .andExpect(jsonPath("$.estado").value("ACTIVO"));
        var captor = ArgumentCaptor.forClass(DirectivaRequest.class);
        verify(service).createDirectiva(captor.capture());
        assertEquals("ACTIVO", captor.getValue().getEstado());
        assertEquals(2L, captor.getValue().getSocioId());
    }
    @Test void getListAndDetailReturn200() throws Exception {
        var response = DirectivaResponse.builder().directivaId(1L).estado("ACTIVO").build();
        when(service.getDirectivas()).thenReturn(List.of(response));
        when(service.getDirectivaById(1L)).thenReturn(response);
        mvc.perform(get("/api/directivas")).andExpect(status().isOk())
                .andExpect(jsonPath("$[0].directivaId").value(1));
        mvc.perform(get("/api/directivas/1")).andExpect(status().isOk())
                .andExpect(jsonPath("$.directivaId").value(1));
    }
    @Test void putPassesIdAndExplicitState() throws Exception {
        when(service.updateDirectiva(eq(1L), any())).thenReturn(
                DirectivaResponse.builder().directivaId(1L).estado("INACTIVO").build());
        mvc.perform(put("/api/directivas/1").contentType(MediaType.APPLICATION_JSON)
                        .content(valid.replace("}", ", \"estado\":\"INACTIVO\"}")))
                .andExpect(status().isOk()).andExpect(jsonPath("$.estado").value("INACTIVO"));
        var captor = ArgumentCaptor.forClass(DirectivaRequest.class);
        verify(service).updateDirectiva(eq(1L), captor.capture());
        assertEquals("INACTIVO", captor.getValue().getEstado());
    }
    @Test void deleteReturns204WithoutBody() throws Exception {
        mvc.perform(delete("/api/directivas/1")).andExpect(status().isNoContent())
                .andExpect(content().string(""));
        verify(service).deleteDirectiva(1L);
    }
    @ParameterizedTest
    @ValueSource(strings = {"{}", "{\"socioId\":0,\"cargo\":\"Presidente\",\"fecha_inicio\":\"2026-01-01\"}",
            "{\"socioId\":2,\"cargo\":\" \",\"fecha_inicio\":\"2026-01-01\"}",
            "{\"socioId\":2,\"cargo\":\"Presidente\"}",
            "{\"socioId\":2,\"cargo\":\"Presidente\",\"fecha_inicio\":\"incorrecta\"}"})
    void invalidPayloadReturns400WithoutCallingService(String json) throws Exception {
        mvc.perform(post("/api/directivas").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isBadRequest());
        mvc.perform(put("/api/directivas/1").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(service);
    }
    @Test void mapsServiceErrorsToHttpStatus() throws Exception {
        when(service.getDirectivaById(99L)).thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND));
        mvc.perform(get("/api/directivas/99")).andExpect(status().isNotFound());
        when(service.createDirectiva(any())).thenThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST));
        mvc.perform(post("/api/directivas").contentType(MediaType.APPLICATION_JSON).content(valid))
                .andExpect(status().isBadRequest());
    }
}
