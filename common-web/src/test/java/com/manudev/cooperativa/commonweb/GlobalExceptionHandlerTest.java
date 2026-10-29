package com.manudev.cooperativa.commonweb;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GlobalExceptionHandlerTest {
    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @BeforeEach
    void configureObjectMapper() {
        handler.setObjectMapper(new ObjectMapper().findAndRegisterModules());
    }

    @Test
    void mapsResponseStatusExceptionsToTheirHttpStatusAndReason() {
        var response = handler.handleControllerException(
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Socio no encontrado"));

        assertEquals(404, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(404, response.getBody().status());
        assertEquals("Socio no encontrado", response.getBody().message());
    }

    @Test
    void mapsDataIntegrityErrorsToConflict() {
        var response = handler.handleControllerException(
                new DataIntegrityViolationException("database details"));

        assertEquals(409, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals("La operación entra en conflicto con los datos existentes",
                response.getBody().message());
        assertFalse(response.getBody().message().contains("database details"));
    }

    @Test
    void hidesUnexpectedExceptionDetailsFromClients() {
        var response = handler.handleControllerException(
                new IllegalStateException("sensitive database details"));

        assertEquals(500, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().message().contains("sensitive database details"));
    }

    @Test
    void handlesReactiveRequestsAndIncludesTheirPath() {
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/gateway/missing").build());

        handler.handle(exchange,
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Ruta no encontrada")).block();

        assertEquals(404, exchange.getResponse().getStatusCode().value());
        String body = exchange.getResponse().getBodyAsString().block();
        assertNotNull(body);
        assertTrue(body.contains("\"path\":\"/gateway/missing\""));
        assertTrue(body.contains("\"status\":404"));
    }
}
