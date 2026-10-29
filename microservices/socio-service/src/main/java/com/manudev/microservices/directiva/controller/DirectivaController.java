package com.manudev.microservices.directiva.controller;

import com.manudev.microservices.directiva.dto.DirectivaRequest;
import com.manudev.microservices.directiva.dto.DirectivaResponse;
import com.manudev.microservices.directiva.service.IDirectivaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/directivas")
public class DirectivaController {
    private final IDirectivaService directivaService;

    @PostMapping
    public ResponseEntity<DirectivaResponse> createDirectiva(@Valid @RequestBody DirectivaRequest request) {
        DirectivaResponse directiva = directivaService.createDirectiva(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(directiva);
    }

    @GetMapping
    public ResponseEntity<List<DirectivaResponse>> getDirectivas() {
        return ResponseEntity.ok(directivaService.getDirectivas());
    }

    @GetMapping("/{id}")
    public ResponseEntity<DirectivaResponse> getDirectivaById(@PathVariable Long id) {
        return ResponseEntity.ok(directivaService.getDirectivaById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<DirectivaResponse> updateDirectiva(
            @PathVariable Long id, @Valid @RequestBody DirectivaRequest request) {
        return ResponseEntity.ok(directivaService.updateDirectiva(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDirectiva(@PathVariable Long id) {
        directivaService.deleteDirectiva(id);
        return ResponseEntity.noContent().build();
    }
}
