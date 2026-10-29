package com.manudev.cooperativa.prestamo.Garantias.mapper;

import com.manudev.cooperativa.prestamo.Garantias.dto.GarantiaResponse;
import com.manudev.cooperativa.prestamo.Garantias.dto.GarantiasRequest;
import com.manudev.cooperativa.prestamo.Garantias.entities.Garantias;
import org.springframework.stereotype.Component;

@Component
public class GarantiasMapper {
    public Garantias toEntity(GarantiasRequest request){
        Garantias garantia = new Garantias();
        updateEntity(garantia, request);
        return garantia;
    }

    public void updateEntity(Garantias garantia, GarantiasRequest request){
        garantia.setPrestamo_id(request.getPrestamo_id());
        garantia.setDocumentos(request.getDocumentacion());
        garantia.setValor_estimado(request.getValor_estimado());
        if (request.getTipo_garantia() != null) {
            garantia.setTipo_garantia(request.getTipo_garantia());
        }
    }
    public GarantiaResponse toResponse(Garantias garantias){
        return GarantiaResponse.builder()
                .garantia_id(garantias.getGarantia_id())
                .prestamo_id(garantias.getPrestamo_id())
                .tipo_garantia(garantias.getTipo_garantia())
                .valor_estimado(garantias.getValor_estimado())
                .documentos(garantias.getDocumentos())
                .createdAt(garantias.getCreateAt())
                .updatedAt(garantias.getUpdatedAt())
                .estado(garantias.getEstado())
                .build();
    }
}
