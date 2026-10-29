package com.manudev.microservices.cuentas.cuentas.controller;

import com.manudev.microservices.cuentas.cuentas.dto.CuentasRequest;
import com.manudev.microservices.cuentas.cuentas.dto.CuentasResponse;
import com.manudev.microservices.cuentas.cuentas.service.ICuentasService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/cuentas")
public class CuentasController {
    private final ICuentasService cuentasService;

    @PostMapping
    public ResponseEntity<CuentasResponse> createCuenta(@Valid @RequestBody CuentasRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(cuentasService.createCuenta(request));
    }

    @GetMapping
    public ResponseEntity<List<CuentasResponse>> getCuentas() {
        return ResponseEntity.ok(cuentasService.getCuentas());
    }

    @GetMapping("/inactivas")
    public ResponseEntity<List<CuentasResponse>> getCuentasInactivas() {
        return ResponseEntity.ok(cuentasService.getCuentasInactivas());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CuentasResponse> getCuentaById(@PathVariable Long id) {
        return ResponseEntity.ok(cuentasService.getCuentaById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CuentasResponse> updateCuenta(
            @PathVariable Long id, @Valid @RequestBody CuentasRequest request) {
        return ResponseEntity.ok(cuentasService.updateCuenta(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCuenta(@PathVariable Long id) {
        cuentasService.deleteCuenta(id);
        return ResponseEntity.noContent().build();
    }

}