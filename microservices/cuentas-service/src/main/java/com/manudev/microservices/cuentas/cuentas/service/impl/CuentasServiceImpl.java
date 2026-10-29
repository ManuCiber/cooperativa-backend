package com.manudev.microservices.cuentas.cuentas.service.impl;

import com.manudev.microservices.cuentas.cuentas.dto.CuentasRequest;
import com.manudev.microservices.cuentas.cuentas.dto.CuentasResponse;
import com.manudev.microservices.cuentas.cuentas.entities.Cuentas;
import com.manudev.microservices.cuentas.cuentas.entities.EstadoCuenta;
import com.manudev.microservices.cuentas.cuentas.mapper.CuentasMapper;
import com.manudev.microservices.cuentas.cuentas.repository.CuentasRepository;
import com.manudev.microservices.cuentas.cuentas.service.ICuentasService;
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
public class CuentasServiceImpl implements ICuentasService {
    private final CuentasRepository cuentasRepository;
    private final CuentasMapper cuentasMapper;
    private final Validator validator;

    @Override
    @Transactional
    public CuentasResponse createCuenta(CuentasRequest request) {
        validateRequest(request);
        if (cuentasRepository.existsByNumeroCuenta(request.getNumeroCuenta())) {
            throw duplicateNumber();
        }
        Cuentas cuenta = cuentasMapper.toEntity(request);
        return cuentasMapper.toResponse(cuentasRepository.save(cuenta));
    }

    @Override
    public List<CuentasResponse> getCuentas() {
        return cuentasRepository.findAllByEstado(EstadoCuenta.ACTIVO)
                .stream()
                .map(cuentasMapper::toResponse)
                .toList();
    }

    @Override
    public List<CuentasResponse> getCuentasInactivas() {
        return cuentasRepository.findAllByEstado(EstadoCuenta.INACTIVO)
                .stream()
                .map(cuentasMapper::toResponse)
                .toList();
    }

    @Override
    public CuentasResponse getCuentaById(Long id) {
        return cuentasMapper.toResponse(findCuentaById(id));
    }

    @Override
    @Transactional
    public CuentasResponse updateCuenta(Long id, CuentasRequest request) {
        validateRequest(request);
        Cuentas cuenta = findCuentaById(id);
        if (cuentasRepository.existsByNumeroCuentaAndIdNot(request.getNumeroCuenta(), id)) {
            throw duplicateNumber();
        }
        cuentasMapper.updateEntity(cuenta, request);
        return cuentasMapper.toResponse(cuentasRepository.save(cuenta));
    }

    @Override
    @Transactional
    public CuentasResponse deleteCuenta(Long id) {
        Cuentas cuenta = findCuentaById(id);
        cuenta.setEstado(EstadoCuenta.INACTIVO);
        return cuentasMapper.toResponse(cuentasRepository.save(cuenta));
    }

    private Cuentas findCuentaById(Long id) {
        if (id == null || id <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El ID de cuenta debe ser positivo");
        }
        return cuentasRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Cuenta no encontrada con ID: " + id));
    }

    private void validateRequest(CuentasRequest request) {
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Los datos de la cuenta son obligatorios");
        }
        var violations = validator.validate(request);
        if (!violations.isEmpty()) {
            String message = violations.stream()
                    .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
                    .sorted()
                    .collect(Collectors.joining("; "));
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
        }
    }

    private ResponseStatusException duplicateNumber() {
        return new ResponseStatusException(HttpStatus.CONFLICT, "El numero de cuenta ya existe");
    }
}
