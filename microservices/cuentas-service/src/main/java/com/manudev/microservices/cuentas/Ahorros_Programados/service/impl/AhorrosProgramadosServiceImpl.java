package com.manudev.microservices.cuentas.Ahorros_Programados.service.impl;

import com.manudev.microservices.cuentas.Ahorros_Programados.dto.*;
import com.manudev.microservices.cuentas.Ahorros_Programados.entities.*;
import com.manudev.microservices.cuentas.Ahorros_Programados.mapper.AhorrosProgramadosMapper;
import com.manudev.microservices.cuentas.Ahorros_Programados.repository.AhorrosProgramadosRepository;
import com.manudev.microservices.cuentas.Ahorros_Programados.service.IAhorrosProgramadosService;
import com.manudev.microservices.cuentas.cuentas.entities.Cuentas;
import com.manudev.microservices.cuentas.cuentas.repository.CuentasRepository;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AhorrosProgramadosServiceImpl implements IAhorrosProgramadosService {
    private final AhorrosProgramadosRepository ahorrosProgramadosRepository;
    private final CuentasRepository cuentasRepository;
    private final AhorrosProgramadosMapper ahorrosProgramadosMapper;
    private final Validator validator;

    @Override
    @Transactional
    public AhorrosProgramadosResponse createAhorroProgramado(AhorrosProgramadosRequest request) {
        validateRequest(request);
        Cuentas cuenta = findCuenta(request);
        return ahorrosProgramadosMapper.toResponse(ahorrosProgramadosRepository.save(
                ahorrosProgramadosMapper.toEntity(request, cuenta)));
    }

    @Override
    public List<AhorrosProgramadosResponse> getAhorrosProgramados() {
        return ahorrosProgramadosRepository.findAll().stream()
                .map(ahorrosProgramadosMapper::toResponse).toList();
    }

    @Override
    public AhorrosProgramadosResponse getAhorroProgramadoById(Long id) {
        return ahorrosProgramadosMapper.toResponse(findById(id));
    }

    @Override
    @Transactional
    public AhorrosProgramadosResponse updateAhorroProgramado(Long id, AhorrosProgramadosRequest request) {
        validateRequest(request);
        AhorrosProgramados ahorro = findById(id);
        Cuentas cuenta = findCuenta(request);
        ahorrosProgramadosMapper.updateEntity(ahorro, request, cuenta);
        return ahorrosProgramadosMapper.toResponse(ahorrosProgramadosRepository.save(ahorro));
    }

    @Override
    @Transactional
    public AhorrosProgramadosResponse cancelarAhorroProgramado(Long id) {
        AhorrosProgramados ahorro = findById(id);
        ahorro.setEstado(EstadoAhorros.CANCELADO);
        return ahorrosProgramadosMapper.toResponse(ahorrosProgramadosRepository.save(ahorro));
    }

    private AhorrosProgramados findById(Long id) {
        if (id == null || id <= 0) {
            throw badRequest("El ID del ahorro programado debe ser positivo");
        }
        return ahorrosProgramadosRepository.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Ahorro programado no encontrado con ID: " + id));
    }

    private Cuentas findCuenta(AhorrosProgramadosRequest request) {
        Cuentas cuenta = cuentasRepository.findById(request.getCuentaId()).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Cuenta no encontrada con ID: " + request.getCuentaId()));
        if (!Objects.equals(cuenta.getSocio_id(), request.getSocioId())) {
            throw badRequest("La cuenta no pertenece al socio indicado");
        }
        return cuenta;
    }

    private void validateRequest(AhorrosProgramadosRequest request) {
        if (request == null) {
            throw badRequest("Los datos del ahorro programado son obligatorios");
        }
        var violations = validator.validate(request);
        if (!violations.isEmpty()) {
            String message = violations.stream()
                    .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                    .sorted().collect(Collectors.joining("; "));
            throw badRequest(message);
        }
    }

    private ResponseStatusException badRequest(String message) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }
}
