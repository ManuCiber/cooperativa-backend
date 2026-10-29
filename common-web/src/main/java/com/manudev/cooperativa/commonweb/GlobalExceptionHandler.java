package com.manudev.cooperativa.commonweb;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebExceptionHandler;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.Map;
import java.util.TreeMap;

@RestControllerAdvice
@Order(-2)
public class GlobalExceptionHandler implements WebExceptionHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private ObjectMapper objectMapper;

    @Autowired(required = false)
    void setObjectMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleControllerException(Exception exception) {
        ErrorDetails details = resolve(exception);
        logUnexpected(exception, details.status());
        ApiErrorResponse body = errorResponse(details, null);
        return ResponseEntity.status(details.status()).body(body);
    }

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable exception) {
        if (exchange.getResponse().isCommitted()) {
            return Mono.error(exception);
        }
        if (!(exception instanceof Exception handledException)) {
            return Mono.error(exception);
        }

        ErrorDetails details = resolve(handledException);
        logUnexpected(handledException, details.status());
        ApiErrorResponse body = errorResponse(details, exchange.getRequest().getURI().getPath());

        if (objectMapper == null) {
            return Mono.error(new IllegalStateException("No hay un ObjectMapper configurado para WebFlux"));
        }

        byte[] bytes;
        try {
            bytes = objectMapper.writeValueAsBytes(body);
        } catch (JsonProcessingException serializationException) {
            return Mono.error(serializationException);
        }

        exchange.getResponse().setStatusCode(HttpStatusCode.valueOf(details.status()));
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
        return exchange.getResponse().writeWith(Mono.just(
                exchange.getResponse().bufferFactory().wrap(bytes)));
    }

    private ErrorDetails resolve(Exception exception) {
        if (exception instanceof DataIntegrityViolationException) {
            return new ErrorDetails(HttpStatus.CONFLICT.value(),
                    "La operación entra en conflicto con los datos existentes", Map.of());
        }
        if (exception instanceof ConstraintViolationException validationException) {
            Map<String, String> errors = new TreeMap<>();
            validationException.getConstraintViolations().forEach(violation ->
                    errors.put(violation.getPropertyPath().toString(), violation.getMessage()));
            return new ErrorDetails(HttpStatus.BAD_REQUEST.value(),
                    "La solicitud contiene datos inválidos", errors);
        }
        if (exception instanceof ResponseStatusException statusException) {
            int status = statusException.getStatusCode().value();
            if (status >= HttpStatus.INTERNAL_SERVER_ERROR.value()) {
                return new ErrorDetails(status,
                        "Ocurrió un error interno al procesar la solicitud", Map.of());
            }
            String message = statusException.getReason() == null
                    ? statusText(status)
                    : statusException.getReason();
            return new ErrorDetails(status, message, Map.of());
        }
        if (exception instanceof ErrorResponse errorResponse) {
            int status = errorResponse.getStatusCode().value();
            if (status >= HttpStatus.INTERNAL_SERVER_ERROR.value()) {
                return new ErrorDetails(status,
                        "Ocurrió un error interno al procesar la solicitud", Map.of());
            }
            String message = errorResponse.getBody().getDetail();
            return new ErrorDetails(status,
                    message == null || message.isBlank() ? statusText(status) : message,
                    Map.of());
        }
        if (exception instanceof IllegalArgumentException) {
            return new ErrorDetails(HttpStatus.BAD_REQUEST.value(),
                    "La solicitud no es válida", Map.of());
        }
        return new ErrorDetails(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Ocurrió un error interno al procesar la solicitud", Map.of());
    }

    private ApiErrorResponse errorResponse(ErrorDetails details, String path) {
        return new ApiErrorResponse(
                Instant.now(),
                details.status(),
                statusText(details.status()),
                details.message(),
                path,
                details.validationErrors());
    }

    private void logUnexpected(Exception exception, int status) {
        if (status >= HttpStatus.INTERNAL_SERVER_ERROR.value()) {
            LOGGER.error("Error no controlado al procesar una solicitud", exception);
        }
    }

    private String statusText(int status) {
        HttpStatus httpStatus = HttpStatus.resolve(status);
        return httpStatus == null ? "Error" : httpStatus.getReasonPhrase();
    }

    private record ErrorDetails(int status, String message, Map<String, String> validationErrors) {
    }
}
