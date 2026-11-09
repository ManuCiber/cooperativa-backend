package com.manudev.microservices.usuarios_service.usuarios.dto;

import com.manudev.microservices.usuarios_service.usuarios.entities.EstadoUsuario;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsuariosResponse {
    private Long usuarioId;
    private Long empleadoId;
    private String nombreUsuario;
    private String rol;
    private EstadoUsuario estadoUsuario;
}
