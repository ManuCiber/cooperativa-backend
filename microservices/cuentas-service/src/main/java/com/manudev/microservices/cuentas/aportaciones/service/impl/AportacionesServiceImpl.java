package com.manudev.microservices.cuentas.aportaciones.service.impl;

import com.manudev.microservices.cuentas.aportaciones.dto.*;
import com.manudev.microservices.cuentas.aportaciones.entities.*;
import com.manudev.microservices.cuentas.aportaciones.mapper.AportacionesMapper;
import com.manudev.microservices.cuentas.aportaciones.repository.AportacionesRepository;
import com.manudev.microservices.cuentas.aportaciones.service.IAportacionesService;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AportacionesServiceImpl implements IAportacionesService {
    private final AportacionesRepository aportacionesRepository;
    private final AportacionesMapper aportacionesMapper;
    private final Validator validator;

    @Override
    @Transactional
    public AportacionesResponse createAportacion(AportacionesRequest request) {
        validateRequest(request);
        return aportacionesMapper.toResponse(
                aportacionesRepository.save(aportacionesMapper.toEntity(request)));
    }

    @Override
    public List<AportacionesResponse> getAportaciones(Long socioId, EstadoAportes estado) {
        if (socioId != null && socioId <= 0) {
            throw badRequest("El ID del socio debe ser positivo");
        }
        List<Aportaciones> aportaciones = socioId != null
                ? aportacionesRepository.findAllBySocioId(socioId)
                : estado != null
                    ? aportacionesRepository.findAllByEstadoOrderByFechaAportacionDesc(estado)
                    : aportacionesRepository.findAll();
        return aportaciones.stream().map(aportacionesMapper::toResponse).toList();
    }

    @Override
    public AportacionesResponse getAportacionById(Long id) {
        return aportacionesMapper.toResponse(findById(id));
    }

    @Override
    @Transactional
    public AportacionesResponse aplicarAportacion(Long id) {
        return changeState(id, EstadoAportes.APLICADA);
    }

    @Override
    @Transactional
    public AportacionesResponse anularAportacion(Long id) {
        return changeState(id, EstadoAportes.ANULADA);
    }

    private AportacionesResponse changeState(Long id, EstadoAportes target) {
        Aportaciones aportacion = findById(id);
        if (aportacion.getEstado() != EstadoAportes.PENDIENTE) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Solo se puede cambiar el estado de una aportación PENDIENTE");
        }
        aportacion.setEstado(target);
        return aportacionesMapper.toResponse(aportacionesRepository.save(aportacion));
    }

    private Aportaciones findById(Long id) {
        if (id == null || id <= 0) {
            throw badRequest("El ID de aportación debe ser positivo");
        }
        return aportacionesRepository.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Aportación no encontrada con ID: " + id));
    }

    private void validateRequest(AportacionesRequest request) {
        if (request == null) {
            throw badRequest("Los datos de la aportación son obligatorios");
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
