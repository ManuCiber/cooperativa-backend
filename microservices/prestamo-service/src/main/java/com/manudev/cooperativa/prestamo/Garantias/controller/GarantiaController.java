package com.manudev.cooperativa.prestamo.Garantias.controller;

import com.manudev.cooperativa.prestamo.Garantias.dto.GarantiaResponse;
import com.manudev.cooperativa.prestamo.Garantias.dto.GarantiasRequest;
import com.manudev.cooperativa.prestamo.Garantias.entities.TipoGarantia;
import com.manudev.cooperativa.prestamo.Garantias.service.IGarantiaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/garantias")
public class GarantiaController {
    private final IGarantiaService garantiaService;

    @PostMapping
    public ResponseEntity<GarantiaResponse> crear(@Valid @RequestBody GarantiasRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(garantiaService.crear(request));
    }

    @GetMapping
    public ResponseEntity<List<GarantiaResponse>> listarTodas() {
        return ResponseEntity.ok(garantiaService.listarTodas());
    }

    @GetMapping("/{garantiaId}")
    public ResponseEntity<GarantiaResponse> obtenerPorId(@PathVariable Long garantiaId) {
        return ResponseEntity.ok(garantiaService.obtenerPorId(garantiaId));
    }

    @GetMapping("/prestamo/{prestamoId}")
    public ResponseEntity<List<GarantiaResponse>> listarPorPrestamo(
            @PathVariable Long prestamoId,
            @RequestParam(required = false) TipoGarantia tipoGarantia) {
        return ResponseEntity.ok(garantiaService.listarPorPrestamo(prestamoId, tipoGarantia));
    }

    @PutMapping("/{garantiaId}")
    public ResponseEntity<GarantiaResponse> actualizar(
            @PathVariable Long garantiaId,
            @Valid @RequestBody GarantiasRequest request) {
        return ResponseEntity.ok(garantiaService.actualizar(garantiaId, request));
    }

    @DeleteMapping("/{garantiaId}")
    public ResponseEntity<Void> eliminar(@PathVariable Long garantiaId) {
        garantiaService.eliminar(garantiaId);
        return ResponseEntity.noContent().build();
    }
}
