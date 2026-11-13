package com.manudev.microservices.usuarios_service.usuarios.mapper;

import com.manudev.microservices.usuarios_service.usuarios.dto.UsuariosRequest;
import com.manudev.microservices.usuarios_service.usuarios.dto.UsuariosResponse;
import com.manudev.microservices.usuarios_service.usuarios.entities.Usuarios;
import org.springframework.stereotype.Component;

@Component
public class UsuariosMapper {
    public Usuarios toEntity(UsuariosRequest request) {
        Usuarios usuario = new Usuarios();
        updateEntity(usuario, request);
        return usuario;
    }

    public void updateEntity(Usuarios usuario, UsuariosRequest request) {
        usuario.setNombreUsuario(request.getNombreUsuario());
        usuario.setPassword(request.getPassword());
        usuario.setRol(request.getRol());
        if (request.getEstadoUsuario() != null) {
            usuario.setEstadoUsuario(request.getEstadoUsuario());
        }
    }

    public UsuariosResponse toResponse(Usuarios usuario) {
        return UsuariosResponse.builder()
                .usuarioId(usuario.getUsuarioId())
                .empleadoId(usuario.getEmpleado().getEmpleado_id())
                .nombreUsuario(usuario.getNombreUsuario())
                .rol(usuario.getRol())
                .estadoUsuario(usuario.getEstadoUsuario())
                .build();
    }
}
