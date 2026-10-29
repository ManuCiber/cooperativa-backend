package com.manudev.microservices.socio.dto;


import com.manudev.microservices.socio.entities.TipoSocio;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SocioRequest {
    @NotBlank(message = "Name is required")
    @Size(max = 100, message = "Name musn't superated 100 characteres")
    private String nombre;

    @NotBlank(message = "Last Name is required")
    @Size(max = 100, message = "Last Name musn't superated 100 characteres")
    private String apellidos;

    @NotBlank(message = "Cedula / DNI Name is required")
    @Size(max = 100, message = "Cedula / DNI musn't superated 100 characteres")
    private String dni_cedula;

    @Past(message = "La fecha de nacimiento debe ser anterior a la fecha actual")
    private LocalDate fecha_nacimiento;

    @Size(max = 255)
    private String direccion;

    @Size(max = 20)
    private String telefono;

    @NotNull(message = "El tipo de socio es obligatorio")
    private TipoSocio tipoSocio;

    @NotNull(message = "El correo es requerido")
    private String email;
}

