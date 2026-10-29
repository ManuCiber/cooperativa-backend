package com.manudev.microservices.usuarios_service.empleados.service.impl;

import com.manudev.microservices.usuarios_service.empleados.dto.EmpleadoRequest;
import com.manudev.microservices.usuarios_service.empleados.dto.EmpleadoResponse;
import com.manudev.microservices.usuarios_service.empleados.entities.Empleado;
import com.manudev.microservices.usuarios_service.empleados.entities.EstadoEmpleado;
import com.manudev.microservices.usuarios_service.empleados.repository.EmpleadoRepository;
import com.manudev.microservices.usuarios_service.empleados.service.IEmpleadoService;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EmpleadoServiceImpl implements IEmpleadoService {
    private final EmpleadoRepository empleadoRepository;
    private final Validator validator;

    @Override
    @Transactional
    public EmpleadoResponse createEmpleado(EmpleadoRequest request) {
        validateRequest(request);
        if (empleadoRepository.existsByDniCedula(request.getDniCedula())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "El DNI o cédula ya está registrado");
        }

        Empleado empleado = Empleado.builder()
                .nombre(request.getNombre())
                .apellidos(request.getApellidos())
                .dni_cedula(request.getDniCedula())
                .cargo(request.getCargo())
                .departamento(request.getDepartamento())
                .fecha_contratacion(request.getFechaContratacion())
                .salario(request.getSalario())
                .estado(request.getEstado())
                .build();

        return toResponse(empleadoRepository.save(empleado));
    }

    @Override
    public List<EmpleadoResponse> getEmpleados() {
        return empleadoRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public EmpleadoResponse getEmpleadoById(Long id) {
        return toResponse(findEmpleadoById(id));
    }

    @Override
    @Transactional
    public EmpleadoResponse updateEmpleado(Long id, EmpleadoRequest request) {
        validateRequest(request);
        Empleado empleado = findEmpleadoById(id);

        if (!empleado.getDni_cedula().equals(request.getDniCedula())
                && empleadoRepository.existsByDniCedula(request.getDniCedula())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "El DNI o cédula ya está registrado");
        }

        empleado.setNombre(request.getNombre());
        empleado.setApellidos(request.getApellidos());
        empleado.setDni_cedula(request.getDniCedula());
        empleado.setCargo(request.getCargo());
        empleado.setDepartamento(request.getDepartamento());
        empleado.setFecha_contratacion(request.getFechaContratacion());
        empleado.setSalario(request.getSalario());
        if (request.getEstado() != null) {
            empleado.setEstado(request.getEstado());
        }
        empleado.setUpdatedAt(LocalDateTime.now());

        return toResponse(empleadoRepository.save(empleado));
    }

    @Override
    @Transactional
    public EmpleadoResponse deleteEmpleado(Long id) {
        Empleado empleado = findEmpleadoById(id);
        if (empleado.getEstado() == EstadoEmpleado.INACTIVO) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Empleado no encontrado con ID: " + id);
        }

        empleado.setEstado(EstadoEmpleado.INACTIVO);
        empleado.setUpdatedAt(LocalDateTime.now());
        return toResponse(empleadoRepository.save(empleado));
    }

    private Empleado findEmpleadoById(Long id) {
        if (id == null || id <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "El ID del empleado debe ser positivo");
        }
        return empleadoRepository.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Empleado no encontrado con ID: " + id));
    }

    private void validateRequest(EmpleadoRequest request) {
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Los datos del empleado son obligatorios");
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

    private EmpleadoResponse toResponse(Empleado empleado) {
        return EmpleadoResponse.builder()
                .empleadoId(empleado.getEmpleado_id())
                .nombre(empleado.getNombre())
                .apellidos(empleado.getApellidos())
                .dniCedula(empleado.getDni_cedula())
                .cargo(empleado.getCargo())
                .departamento(empleado.getDepartamento())
                .fechaContratacion(empleado.getFecha_contratacion())
                .salario(empleado.getSalario())
                .estado(empleado.getEstado())
                .createdAt(empleado.getCreatedAt())
                .updatedAt(empleado.getUpdatedAt())
                .build();
    }
}
