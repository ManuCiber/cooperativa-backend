package com.manudev.cooperativa.prestamo.prestamo.controller;

import com.manudev.cooperativa.prestamo.prestamo.dto.PrestamosRequest;
import com.manudev.cooperativa.prestamo.prestamo.dto.PrestamosResponse;
import com.manudev.cooperativa.prestamo.prestamo.service.IPrestamosService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/prestamos")
public class PrestamosController {
    private final IPrestamosService prestamosService;

    @PostMapping
    public ResponseEntity<PrestamosResponse> createPrestamo(@Valid @RequestBody PrestamosRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(prestamosService.createPrestamo(request));
    }

    @GetMapping
    public ResponseEntity<List<PrestamosResponse>> getPrestamos() {
        return ResponseEntity.ok(prestamosService.getPrestamos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PrestamosResponse> getPrestamoById(@PathVariable Long id) {
        return ResponseEntity.ok(prestamosService.getPrestamoById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PrestamosResponse> updatePrestamo(
            @PathVariable Long id, @Valid @RequestBody PrestamosRequest request) {
        return ResponseEntity.ok(prestamosService.updatePrestamo(id, request));
    }

    @PatchMapping("/{id}/cancelar")
    public ResponseEntity<PrestamosResponse> cancelarPrestamo(@PathVariable Long id) {
        return ResponseEntity.ok(prestamosService.cancelarPrestamo(id));
    }
}
