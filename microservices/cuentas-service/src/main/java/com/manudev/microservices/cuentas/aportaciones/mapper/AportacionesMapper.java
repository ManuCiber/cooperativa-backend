package com.manudev.microservices.cuentas.aportaciones.mapper;

import com.manudev.microservices.cuentas.aportaciones.dto.AportacionesRequest;
import com.manudev.microservices.cuentas.aportaciones.dto.AportacionesResponse;
import com.manudev.microservices.cuentas.aportaciones.entities.Aportaciones;
import org.springframework.stereotype.Component;

@Component
public class AportacionesMapper {
    public Aportaciones toEntity(AportacionesRequest request) {
        return Aportaciones.builder()
                .socio_id(request.getSocioId())
                .monto(request.getMonto())
                .fecha_aportacion(request.getFecha_aportacion())
                .tipo_aportacion(request.getTipo_aportacion())
                .build();
    }

    public AportacionesResponse toResponse(Aportaciones aportacion) {
        return AportacionesResponse.builder()
                .aportacionId(aportacion.getAportacion_id())
                .socioId(aportacion.getSocio_id())
                .monto(aportacion.getMonto())
                .fecha_aportacion(aportacion.getFecha_aportacion())
                .tipo_aportacion(aportacion.getTipo_aportacion())
                .estado(aportacion.getEstado())
                .build();
    }
}
