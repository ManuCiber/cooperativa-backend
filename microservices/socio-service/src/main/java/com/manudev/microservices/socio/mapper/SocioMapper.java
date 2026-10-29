package com.manudev.microservices.socio.mapper;

import com.manudev.microservices.socio.dto.SocioRequest;
import com.manudev.microservices.socio.dto.SocioResponse;
import com.manudev.microservices.socio.entities.Socio;
import org.springframework.stereotype.Component;

@Component
public class SocioMapper {
    public Socio toEntity(SocioRequest request){
        return Socio.builder()
                .nombre(request.getNombre())
                .apellidos(request.getApellidos())
                .dni_cedula(request.getDni_cedula())
                .fecha_nacimiento(request.getFecha_nacimiento())
                .direccion(request.getDireccion())
                .telefono(request.getTelefono())
                .email(request.getEmail())
                .tipo_socio(request.getTipoSocio())
                .build();
    }

    public SocioResponse toResponse(Socio lv_socio){
        return SocioResponse.builder()
                .socioId(lv_socio.getSocio_id())
                .no_socio(lv_socio.getNo_socio())
                .nombre(lv_socio.getNombre())
                .apellidos(lv_socio.getApellidos())
                .dni_Cedula(lv_socio.getDni_cedula())
                .fecha_nacimiento(lv_socio.getFecha_nacimiento())
                .direccion(lv_socio.getDireccion())
                .telefono(lv_socio.getDireccion())
                .email(lv_socio.getEmail())
                .fecha_ingreso(lv_socio.getFecha_entrada())
                .estado_socio(lv_socio.getEstado_socio())
                .tipo_socio(lv_socio.getTipo_socio())
                .createdAt(lv_socio.getCreatedAt())
                .updatedAt(lv_socio.getUpdatedAt())
                .build();
    }

    public void updateEntity(Socio lv_socio, SocioRequest lv_request){
        lv_socio.setNombre(lv_request.getNombre());
        lv_socio.setApellidos(lv_request.getApellidos());
        lv_socio.setDni_cedula(lv_request.getDni_cedula());
        lv_socio.setFecha_nacimiento(lv_request.getFecha_nacimiento());
        lv_socio.setDireccion(lv_request.getDireccion());
        lv_socio.setTelefono(lv_request.getTelefono());
        lv_socio.setEmail(lv_request.getEmail());
        lv_socio.setTipo_socio(lv_request.getTipoSocio());
    }
}
