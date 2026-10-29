package com.manudev.cooperativa.prestamo.PagoPrestamos.controller;

import com.manudev.cooperativa.prestamo.PagoPrestamos.dto.PagoPrestamosRequest;
import com.manudev.cooperativa.prestamo.PagoPrestamos.dto.PagoPrestamosResponse;
import com.manudev.cooperativa.prestamo.PagoPrestamos.entities.EstadoPagosPrestamo;
import com.manudev.cooperativa.prestamo.PagoPrestamos.services.PagoPrestamosService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/api/pago-prestamo")
public class PagoPrestamoController {
    private final PagoPrestamosService service;

    @PostMapping
    public ResponseEntity<PagoPrestamosResponse> create(@Valid @RequestBody PagoPrestamosRequest request){
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crear(request));
    }

    @GetMapping("/{pago_id}")
    public ResponseEntity<PagoPrestamosResponse> getById(@PathVariable Long pago_id){
        return ResponseEntity.ok(service.obtenerPorId(pago_id));
    }

    @GetMapping("/prestamo/{prestamo_id}")
    public ResponseEntity<List<PagoPrestamosResponse>>ListByPrestamo(
            @PathVariable Long prestamo_id,
            @RequestParam(required = false)EstadoPagosPrestamo estado
            ){
        return ResponseEntity.ok(service.listarPorPrestamo(prestamo_id, estado));
    }

    @GetMapping("/vencidas")
    public ResponseEntity<List<PagoPrestamosResponse>>ListVencidos(){
        return ResponseEntity.ok(service.listarVencidas());
    }

    @PatchMapping("/{pago_id}/pagar")
    public ResponseEntity<PagoPrestamosResponse> registrarPago(@PathVariable Long pago_id){
        return ResponseEntity.ok(service.registrarPago(pago_id));
    }

    @DeleteMapping("/{pago_id}")
    public ResponseEntity<Void>eliminarPago(@PathVariable Long pago_id){
        service.eliminarPago(pago_id);
        return ResponseEntity.noContent().build();
    }
}