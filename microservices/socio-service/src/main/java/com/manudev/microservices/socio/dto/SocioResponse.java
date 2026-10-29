package com.manudev.microservices.socio.dto;

import com.manudev.microservices.socio.entities.EstadoSocio;
import com.manudev.microservices.socio.entities.TipoSocio;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SocioResponse {
    private long socioId;
    private String no_socio;
    private String nombre;
    private String apellidos;
    private String dni_Cedula;
    private LocalDate fecha_nacimiento;
    private String direccion;
    private String telefono;
    private String email;
    private LocalDate fecha_ingreso;
    private EstadoSocio estado_socio;
    private TipoSocio tipo_socio;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
