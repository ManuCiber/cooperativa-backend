package com.manudev.microservices.cuentas.aportaciones.controller;

import com.manudev.microservices.cuentas.aportaciones.dto.AportacionesRequest;
import com.manudev.microservices.cuentas.aportaciones.dto.AportacionesResponse;
import com.manudev.microservices.cuentas.aportaciones.entities.EstadoAportes;
import com.manudev.microservices.cuentas.aportaciones.service.IAportacionesService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/aportaciones")
public class AportacionesController {
    private final IAportacionesService aportacionesService;

    @PostMapping
    public ResponseEntity<AportacionesResponse> createAportacion(
            @Valid @RequestBody AportacionesRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(aportacionesService.createAportacion(request));
    }

    @GetMapping
    public ResponseEntity<List<AportacionesResponse>> getAportaciones(
            @RequestParam(required = false) Long socioId,
            @RequestParam(required = false) EstadoAportes estado) {
        return ResponseEntity.ok(aportacionesService.getAportaciones(socioId, estado));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AportacionesResponse> getAportacionById(@PathVariable Long id) {
        return ResponseEntity.ok(aportacionesService.getAportacionById(id));
    }

    @PatchMapping("/{id}/aplicar")
    public ResponseEntity<AportacionesResponse> aplicarAportacion(@PathVariable Long id) {
        return ResponseEntity.ok(aportacionesService.aplicarAportacion(id));
    }

    @PatchMapping("/{id}/anular")
    public ResponseEntity<AportacionesResponse> anularAportacion(@PathVariable Long id) {
        return ResponseEntity.ok(aportacionesService.anularAportacion(id));
    }
}
