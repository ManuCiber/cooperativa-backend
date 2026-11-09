package com.manudev.microservices.usuarios_service.usuarios.service.impl;

import com.manudev.microservices.usuarios_service.empleados.entities.Empleado;
import com.manudev.microservices.usuarios_service.empleados.repository.EmpleadoRepository;
import com.manudev.microservices.usuarios_service.usuarios.dto.UsuariosRequest;
import com.manudev.microservices.usuarios_service.usuarios.dto.UsuariosResponse;
import com.manudev.microservices.usuarios_service.usuarios.entities.EstadoUsuario;
import com.manudev.microservices.usuarios_service.usuarios.entities.Usuarios;
import com.manudev.microservices.usuarios_service.usuarios.mapper.UsuariosMapper;
import com.manudev.microservices.usuarios_service.usuarios.repository.UsuariosRepository;
import com.manudev.microservices.usuarios_service.usuarios.service.UsuarioService;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UsuarioServiceImpl implements UsuarioService {
    private final UsuariosRepository usuariosRepository;
    private final EmpleadoRepository empleadoRepository;
    private final UsuariosMapper usuariosMapper;
    private final Validator validator;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public UsuariosResponse createUsuario(UsuariosRequest request) {
        validateRequest(request);
        if (usuariosRepository.existsByNombreUsuario(request.getNombreUsuario())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "El nombre de usuario ya está registrado");
        }

        Empleado empleado = findEmpleadoById(request.getEmpleadoId());
        if (usuariosRepository.existsByEmpleadoId(empleado.getEmpleado_id())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "El empleado ya tiene un usuario asociado");
        }

        Usuarios usuario = usuariosMapper.toEntity(request);
        usuario.setEmpleado(empleado);
        usuario.setPassword(passwordEncoder.encode(request.getPassword()));
        usuario.setEstadoUsuario(request.getEstadoUsuario() == null
                ? EstadoUsuario.ACTIVO : request.getEstadoUsuario());
        return usuariosMapper.toResponse(usuariosRepository.save(usuario));
    }

    @Override
    public List<UsuariosResponse> getUsuarios() {
        return usuariosRepository.findAll().stream()
                .map(usuariosMapper::toResponse)
                .toList();
    }

    @Override
    public UsuariosResponse getUsuarioById(Long id) {
        return usuariosMapper.toResponse(findUsuarioById(id));
    }

    @Override
    @Transactional
    public UsuariosResponse updateUsuario(Long id, UsuariosRequest request) {
        validateRequest(request);
        Usuarios usuario = findUsuarioById(id);

        if (!usuario.getNombreUsuario().equals(request.getNombreUsuario())
                && usuariosRepository.existsByNombreUsuarioAndUsuarioIdNot(
                        request.getNombreUsuario(), id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "El nombre de usuario ya está registrado");
        }

        Empleado empleado = findEmpleadoById(request.getEmpleadoId());
        if (!usuario.getEmpleado().getEmpleado_id().equals(empleado.getEmpleado_id())
                && usuariosRepository.existsByEmpleadoIdAndUsuarioIdNot(
                        empleado.getEmpleado_id(), id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "El empleado ya tiene un usuario asociado");
        }

        usuariosMapper.updateEntity(usuario, request);
        usuario.setEmpleado(empleado);
        usuario.setPassword(passwordEncoder.encode(request.getPassword()));
        return usuariosMapper.toResponse(usuariosRepository.save(usuario));
    }

    @Override
    @Transactional
    public UsuariosResponse deleteUsuario(Long id) {
        Usuarios usuario = findUsuarioById(id);
        if (usuario.getEstadoUsuario() == EstadoUsuario.INACTIVO) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Usuario no encontrado con ID: " + id);
        }

        usuario.setEstadoUsuario(EstadoUsuario.INACTIVO);
        return usuariosMapper.toResponse(usuariosRepository.save(usuario));
    }

    private Usuarios findUsuarioById(Long id) {
        if (id == null || id <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "El ID del usuario debe ser positivo");
        }
        return usuariosRepository.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Usuario no encontrado con ID: " + id));
    }

    private void validateRequest(UsuariosRequest request) {
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Los datos del usuario son obligatorios");
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

    private Empleado findEmpleadoById(Long id) {
        return empleadoRepository.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Empleado no encontrado con ID: " + id));
    }
}
