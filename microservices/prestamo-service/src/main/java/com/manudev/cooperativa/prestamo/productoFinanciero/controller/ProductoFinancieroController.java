package com.manudev.cooperativa.prestamo.productoFinanciero.controller;

import com.manudev.cooperativa.prestamo.productoFinanciero.dto.ProductoFinancieroRequest;
import com.manudev.cooperativa.prestamo.productoFinanciero.dto.ProductoFinancieroResponse;
import com.manudev.cooperativa.prestamo.productoFinanciero.entity.EstadoProducto;
import com.manudev.cooperativa.prestamo.productoFinanciero.service.IProductoFinancieroService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/productos-financieros")
public class ProductoFinancieroController {

    private final IProductoFinancieroService productoFinancieroService;

    @PostMapping
    public ResponseEntity<ProductoFinancieroResponse> createProducto(
            @Valid @RequestBody ProductoFinancieroRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(productoFinancieroService.createProducto(request));
    }

    @GetMapping
    public ResponseEntity<List<ProductoFinancieroResponse>> getProductos(
            @RequestParam(required = false) EstadoProducto estado) {
        if (estado != null) {
            return ResponseEntity.ok(productoFinancieroService.getProductosByEstado(estado));
        }
        return ResponseEntity.ok(productoFinancieroService.getProductos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductoFinancieroResponse> getProductoById(@PathVariable Long id) {
        return ResponseEntity.ok(productoFinancieroService.getProductoById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductoFinancieroResponse> updateProducto(
            @PathVariable Long id,
            @Valid @RequestBody ProductoFinancieroRequest request) {
        return ResponseEntity.ok(productoFinancieroService.updateProducto(id, request));
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<ProductoFinancieroResponse> cambiarEstado(
            @PathVariable Long id,
            @RequestParam EstadoProducto estado) {
        return ResponseEntity.ok(productoFinancieroService.cambiarEstado(id, estado));
    }
}
