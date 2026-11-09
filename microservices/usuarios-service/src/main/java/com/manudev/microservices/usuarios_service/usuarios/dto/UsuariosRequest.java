package com.manudev.microservices.usuarios_service.usuarios.dto;

import com.manudev.microservices.usuarios_service.usuarios.entities.EstadoUsuario;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
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
public class UsuariosRequest {
    @NotNull(message = "El empleado es obligatorio")
    @Positive(message = "El ID del empleado debe ser positivo")
    private Long empleadoId;

    @NotBlank(message = "El nombre de usuario es obligatorio")
    private String nombreUsuario;

    @NotBlank(message = "La contraseña es obligatoria")
    private String password;

    @NotBlank(message = "El rol es obligatorio")
    private String rol;

    private EstadoUsuario estadoUsuario;
}
