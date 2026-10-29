package com.manudev.microservices.cuentas.Ahorros_Programados.controller;

import com.manudev.microservices.cuentas.Ahorros_Programados.dto.AhorrosProgramadosRequest;
import com.manudev.microservices.cuentas.Ahorros_Programados.dto.AhorrosProgramadosResponse;
import com.manudev.microservices.cuentas.Ahorros_Programados.service.IAhorrosProgramadosService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/ahorros-programados")
public class AhorrosProgramadosController {
    private final IAhorrosProgramadosService ahorrosProgramadosService;

    @PostMapping
    public ResponseEntity<AhorrosProgramadosResponse> createAhorroProgramado(
            @Valid @RequestBody AhorrosProgramadosRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ahorrosProgramadosService.createAhorroProgramado(request));
    }

    @GetMapping
    public ResponseEntity<List<AhorrosProgramadosResponse>> getAhorrosProgramados() {
        return ResponseEntity.ok(ahorrosProgramadosService.getAhorrosProgramados());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AhorrosProgramadosResponse> getAhorroProgramadoById(@PathVariable Long id) {
        return ResponseEntity.ok(ahorrosProgramadosService.getAhorroProgramadoById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AhorrosProgramadosResponse> updateAhorroProgramado(
            @PathVariable Long id, @Valid @RequestBody AhorrosProgramadosRequest request) {
        return ResponseEntity.ok(ahorrosProgramadosService.updateAhorroProgramado(id, request));
    }

    @PatchMapping("/{id}/cancelar")
    public ResponseEntity<AhorrosProgramadosResponse> cancelarAhorroProgramado(@PathVariable Long id) {
        return ResponseEntity.ok(ahorrosProgramadosService.cancelarAhorroProgramado(id));
    }
}
