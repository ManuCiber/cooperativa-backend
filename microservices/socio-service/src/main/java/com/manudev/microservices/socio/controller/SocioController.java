package com.manudev.microservices.socio.controller;

import com.manudev.microservices.socio.dto.SocioRequest;
import com.manudev.microservices.socio.dto.SocioResponse;
import com.manudev.microservices.socio.service.ISocioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/socios")
public class SocioController {
    private final ISocioService socioService;

    @PostMapping
    public ResponseEntity<SocioResponse> createSocio(@Valid @RequestBody SocioRequest lv_socioRequest){
        SocioResponse lv_socio = socioService.createSocio(lv_socioRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(lv_socio);
    }

    @GetMapping
    public ResponseEntity<List<SocioResponse>> getSocios() {
        List<SocioResponse> lv_socios = socioService.getSocios();
        return ResponseEntity.ok(lv_socios);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SocioResponse> getSocioById(@PathVariable Long id){
        SocioResponse lv_socio = socioService.getSocioById(id);
        return ResponseEntity.ok(lv_socio);
    }

    @PutMapping("/{id}")
    public ResponseEntity<SocioResponse> updateSocio(@PathVariable Long id,  @Valid @RequestBody SocioRequest lv_socioRequest){
        SocioResponse lv_socio = socioService.updateSocio(id, lv_socioRequest);
        return ResponseEntity.ok(lv_socio);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSocio(@PathVariable Long id){
        socioService.deleteSocio(id);
        return ResponseEntity.noContent().build();
    }




}
