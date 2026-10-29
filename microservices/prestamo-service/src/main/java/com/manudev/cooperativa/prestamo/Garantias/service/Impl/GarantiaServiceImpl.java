package com.manudev.cooperativa.prestamo.Garantias.service.Impl;

import com.manudev.cooperativa.prestamo.Garantias.dto.GarantiaResponse;
import com.manudev.cooperativa.prestamo.Garantias.dto.GarantiasRequest;
import com.manudev.cooperativa.prestamo.Garantias.entities.EstadoGarantia;
import com.manudev.cooperativa.prestamo.Garantias.entities.Garantias;
import com.manudev.cooperativa.prestamo.Garantias.entities.TipoGarantia;
import com.manudev.cooperativa.prestamo.Garantias.mapper.GarantiasMapper;
import com.manudev.cooperativa.prestamo.Garantias.repository.IGarantiasRepository;
import com.manudev.cooperativa.prestamo.prestamo.repository.PrestamosRepository;
import com.manudev.cooperativa.prestamo.Garantias.service.IGarantiaService;
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
public class GarantiaServiceImpl implements IGarantiaService {
    private final IGarantiasRepository garantiasRepository;
    private final PrestamosRepository prestamosRepository;
    private final GarantiasMapper garantiasMapper;
    private final Validator validator;

    @Override
    @Transactional
    public GarantiaResponse crear(GarantiasRequest request) {
        validarRequest(request);
        validarPrestamo(request.getPrestamo_id());

        Garantias garantia = garantiasMapper.toEntity(request);
        return garantiasMapper.toResponse(garantiasRepository.saveAndFlush(garantia));
    }

    @Override
    public GarantiaResponse obtenerPorId(Long garantiaId) {
        return garantiasMapper.toResponse(buscarPorId(garantiaId));
    }

    @Override
    public List<GarantiaResponse> listarTodas() {
        return garantiasRepository.findByEstado(EstadoGarantia.ACTIVO).stream()
                .map(garantiasMapper::toResponse)
                .toList();
    }

    @Override
    public List<GarantiaResponse> listarPorPrestamo(Long prestamoId, TipoGarantia tipoGarantia) {
        validarIdPositivo(prestamoId, "El ID del prestamo debe ser positivo");
        if (!prestamosRepository.existsById(prestamoId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Prestamo no encontrado con ID: " + prestamoId);
        }

        List<Garantias> garantias = tipoGarantia == null
                ? garantiasRepository.findByPrestamoId(prestamoId, EstadoGarantia.ACTIVO)
                : garantiasRepository.findByPrestamoIdAndTipoGarantia(
                        prestamoId, tipoGarantia, EstadoGarantia.ACTIVO);
        return garantias.stream().map(garantiasMapper::toResponse).toList();
    }

    @Override
    @Transactional
    public GarantiaResponse actualizar(Long garantiaId, GarantiasRequest request) {
        validarRequest(request);
        validarPrestamo(request.getPrestamo_id());

        Garantias garantia = buscarPorId(garantiaId);
        requireActivo(garantia);
        garantiasMapper.updateEntity(garantia, request);
        return garantiasMapper.toResponse(garantiasRepository.saveAndFlush(garantia));
    }

    @Override
    @Transactional
    public void eliminar(Long garantiaId) {
        Garantias garantia = buscarPorId(garantiaId);
        requireActivo(garantia);
        garantia.setEstado(EstadoGarantia.INACTIVO);
        garantiasRepository.saveAndFlush(garantia);
    }

    private Garantias buscarPorId(Long garantiaId) {
        validarIdPositivo(garantiaId, "El ID de la garantia debe ser positivo");
        return garantiasRepository.findById(garantiaId).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Garantia no encontrada con ID: " + garantiaId));
    }

    private void validarPrestamo(Long prestamoId) {
        if (!prestamosRepository.existsById(prestamoId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Prestamo no encontrado con ID: " + prestamoId);
        }
    }

    private void requireActivo(Garantias garantia) {
        if (garantia.getEstado() != EstadoGarantia.ACTIVO) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Solo se pueden modificar o eliminar garantias en estado ACTIVO");
        }
    }

    private void validarRequest(GarantiasRequest request) {
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Los datos de la garantia son obligatorios");
        }
        var violations = validator.validate(request);
        if (!violations.isEmpty()) {
            String message = violations.stream()
                    .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                    .sorted()
                    .collect(Collectors.joining("; "));
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
        }
    }

    private void validarIdPositivo(Long id, String message) {
        if (id == null || id <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
        }
    }
}
