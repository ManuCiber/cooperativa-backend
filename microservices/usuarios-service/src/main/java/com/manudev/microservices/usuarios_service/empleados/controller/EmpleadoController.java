package com.manudev.microservices.usuarios_service.empleados.controller;

import com.manudev.microservices.usuarios_service.empleados.dto.EmpleadoRequest;
import com.manudev.microservices.usuarios_service.empleados.dto.EmpleadoResponse;
import com.manudev.microservices.usuarios_service.empleados.service.IEmpleadoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/empleados")
public class EmpleadoController {
    private final IEmpleadoService empleadoService;

    @PostMapping
    public ResponseEntity<EmpleadoResponse> createEmpleado(
            @Valid @RequestBody EmpleadoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(empleadoService.createEmpleado(request));
    }

    @GetMapping
    public ResponseEntity<List<EmpleadoResponse>> getEmpleados() {
        return ResponseEntity.ok(empleadoService.getEmpleados());
    }

    @GetMapping("/{id}")
    public ResponseEntity<EmpleadoResponse> getEmpleadoById(@PathVariable Long id) {
        return ResponseEntity.ok(empleadoService.getEmpleadoById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EmpleadoResponse> updateEmpleado(
            @PathVariable Long id,
            @Valid @RequestBody EmpleadoRequest request) {
        return ResponseEntity.ok(empleadoService.updateEmpleado(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEmpleado(@PathVariable Long id) {
        empleadoService.deleteEmpleado(id);
        return ResponseEntity.noContent().build();
    }
}
