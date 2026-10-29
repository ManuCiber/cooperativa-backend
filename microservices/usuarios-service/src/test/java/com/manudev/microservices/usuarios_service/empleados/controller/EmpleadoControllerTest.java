package com.manudev.microservices.usuarios_service.empleados.controller;

import com.manudev.microservices.usuarios_service.empleados.dto.EmpleadoRequest;
import com.manudev.microservices.usuarios_service.empleados.dto.EmpleadoResponse;
import com.manudev.microservices.usuarios_service.empleados.entities.EstadoEmpleado;
import com.manudev.microservices.usuarios_service.empleados.service.IEmpleadoService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class EmpleadoControllerTest {
    private IEmpleadoService empleadoService;
    private MockMvc mockMvc;
    private LocalValidatorFactoryBean validator;

    private static final String VALID_REQUEST = """
            {
              "nombre": "Ana",
              "apellidos": "Pérez",
              "dniCedula": "123456789",
              "cargo": "Asistente",
              "departamento": "Administración",
              "salario": 1250.50
            }
            """;

    @BeforeEach
    void setUp() {
        empleadoService = mock(IEmpleadoService.class);
        validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        mockMvc = MockMvcBuilders.standaloneSetup(new EmpleadoController(empleadoService))
                .setValidator(validator)
                .build();
    }

    @AfterEach
    void tearDown() {
        validator.close();
    }

    @Test
    void postCreatesEmployeeAndReturns201() throws Exception {
        when(empleadoService.createEmpleado(any(EmpleadoRequest.class)))
                .thenReturn(response());

        mockMvc.perform(post("/api/empleados")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_REQUEST))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.empleadoId").value(1))
                .andExpect(jsonPath("$.dniCedula").value("123456789"));

        verify(empleadoService).createEmpleado(any(EmpleadoRequest.class));
    }

    @Test
    void getListAndDetailReturnEmployees() throws Exception {
        when(empleadoService.getEmpleados()).thenReturn(List.of(response()));
        when(empleadoService.getEmpleadoById(1L)).thenReturn(response());

        mockMvc.perform(get("/api/empleados"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].empleadoId").value(1));
        mockMvc.perform(get("/api/empleados/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Ana"));
    }

    @Test
    void putUpdatesEmployeeAndReturns200() throws Exception {
        when(empleadoService.updateEmpleado(eq(1L), any(EmpleadoRequest.class)))
                .thenReturn(response());

        mockMvc.perform(put("/api/empleados/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_REQUEST))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.empleadoId").value(1));

        verify(empleadoService).updateEmpleado(eq(1L), any(EmpleadoRequest.class));
    }

    @Test
    void deleteReturns204AndDelegatesToService() throws Exception {
        mockMvc.perform(delete("/api/empleados/1"))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(empleadoService).deleteEmpleado(1L);
    }

    @Test
    void invalidRequestReturns400WithoutCallingService() throws Exception {
        String invalidRequest = """
                {"nombre":" ","apellidos":"Pérez","dniCedula":"123456789",
                 "cargo":"Asistente","departamento":"Administración","salario":-1}
                """;

        mockMvc.perform(post("/api/empleados")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequest))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(empleadoService);
    }

    @Test
    void mapsServiceNotFoundAndConflictErrorsToHttpStatuses() throws Exception {
        when(empleadoService.getEmpleadoById(99L))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Empleado no encontrado"));
        when(empleadoService.createEmpleado(any(EmpleadoRequest.class)))
                .thenThrow(new ResponseStatusException(HttpStatus.CONFLICT, "Documento duplicado"));

        mockMvc.perform(get("/api/empleados/99"))
                .andExpect(status().isNotFound());
        mockMvc.perform(post("/api/empleados")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_REQUEST))
                .andExpect(status().isConflict());
    }

    private EmpleadoResponse response() {
        return EmpleadoResponse.builder()
                .empleadoId(1L)
                .nombre("Ana")
                .apellidos("Pérez")
                .dniCedula("123456789")
                .cargo("Asistente")
                .departamento("Administración")
                .salario(new BigDecimal("1250.50"))
                .estado(EstadoEmpleado.ACTIVO)
                .build();
    }
}
