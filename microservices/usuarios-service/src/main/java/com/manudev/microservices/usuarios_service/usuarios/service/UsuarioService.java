package com.manudev.microservices.usuarios_service.usuarios.service;

import com.manudev.microservices.usuarios_service.usuarios.dto.UsuariosRequest;
import com.manudev.microservices.usuarios_service.usuarios.dto.UsuariosResponse;

import java.util.List;

public interface UsuarioService {
    UsuariosResponse createUsuario(UsuariosRequest request);
    List<UsuariosResponse> getUsuarios();
    UsuariosResponse getUsuarioById(Long id);
    UsuariosResponse updateUsuario(Long id, UsuariosRequest request);
    UsuariosResponse deleteUsuario(Long id);
}
