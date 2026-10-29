package com.manudev.microservices.directiva.mapper;

import com.manudev.microservices.directiva.dto.DirectivaRequest;
import com.manudev.microservices.directiva.dto.DirectivaResponse;
import com.manudev.microservices.directiva.entities.Directiva;
import com.manudev.microservices.directiva.entities.EstadoDirectiva;
import com.manudev.microservices.socio.entities.Socio;
import org.springframework.stereotype.Component;

@Component
public class DirectivaMapper {
    // El servicio debe buscar el socio indicado en el request antes de mapear.
    public Directiva toEntity(DirectivaRequest request, Socio socio) {
        return Directiva.builder()
                .socio(socio)
                .cargo(request.getCargo())
                .fecha_inicio(request.getFecha_inicio())
                .fecha_final(request.getFecha_final())
                .estado(toEstado(request.getEstado()))
                .build();
    }

    public DirectivaResponse toResponse(Directiva directiva) {
        return DirectivaResponse.builder()
                .directivaId(directiva.getDirectiva_id())
                .socioId(directiva.getSocio().getSocio_id())
                .cargo(directiva.getCargo())
                .fecha_inicio(directiva.getFecha_inicio())
                .fecha_final(directiva.getFecha_final())
                .estado(directiva.getEstado().name())
                .build();
    }

    public void updateEntity(Directiva directiva, DirectivaRequest request, Socio socio) {
        EstadoDirectiva estado = toEstado(request.getEstado());
        directiva.setSocio(socio);
        directiva.setCargo(request.getCargo());
        directiva.setFecha_inicio(request.getFecha_inicio());
        directiva.setFecha_final(request.getFecha_final());
        directiva.setEstado(estado);
    }

    private EstadoDirectiva toEstado(String estado) {
        if (estado == null) {
            return EstadoDirectiva.ACTIVO;
        }
        try {
            return EstadoDirectiva.valueOf(estado);
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("El estado debe ser ACTIVO o INACTIVO", exception);
        }
    }
}
