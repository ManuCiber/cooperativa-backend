package com.manudev.microservices.cuentas.transacciones.controller;

import com.manudev.microservices.cuentas.transacciones.dto.*;
import com.manudev.microservices.cuentas.transacciones.entities.TipoTransacciones;
import com.manudev.microservices.cuentas.transacciones.service.ITransaccionesService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/transacciones")
public class TransaccionesController {
    private final ITransaccionesService service;

    @PostMapping
    public ResponseEntity<TransaccionesResponse> createTransaccion(@Valid @RequestBody TransaccionesRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createTransaccion(request));
    }

    @GetMapping
    public ResponseEntity<List<TransaccionesResponse>> getTransacciones(
            @RequestParam(required = false) Long socioId,
            @RequestParam(required = false) TipoTransacciones tipo) {
        return ResponseEntity.ok(service.getTransacciones(socioId, tipo));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TransaccionesResponse> getTransaccionById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getTransaccionById(id));
    }
}
