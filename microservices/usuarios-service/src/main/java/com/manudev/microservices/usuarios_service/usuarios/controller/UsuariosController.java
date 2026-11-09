package com.manudev.microservices.usuarios_service.usuarios.controller;

import com.manudev.microservices.usuarios_service.usuarios.dto.UsuariosRequest;
import com.manudev.microservices.usuarios_service.usuarios.dto.UsuariosResponse;
import com.manudev.microservices.usuarios_service.usuarios.service.UsuarioService;
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
@RequestMapping("/api/usuarios")
public class UsuariosController {
    private final UsuarioService usuarioService;

    @PostMapping
    public ResponseEntity<UsuariosResponse> createUsuario(
            @Valid @RequestBody UsuariosRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(usuarioService.createUsuario(request));
    }

    @GetMapping
    public ResponseEntity<List<UsuariosResponse>> getUsuarios() {
        return ResponseEntity.ok(usuarioService.getUsuarios());
    }

    @GetMapping("/{id}")
    public ResponseEntity<UsuariosResponse> getUsuarioById(@PathVariable Long id) {
        return ResponseEntity.ok(usuarioService.getUsuarioById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UsuariosResponse> updateUsuario(
            @PathVariable Long id,
            @Valid @RequestBody UsuariosRequest request) {
        return ResponseEntity.ok(usuarioService.updateUsuario(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUsuario(@PathVariable Long id) {
        usuarioService.deleteUsuario(id);
        return ResponseEntity.noContent().build();
    }
}
