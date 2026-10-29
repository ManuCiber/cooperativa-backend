package com.manudev.microservices.cuentas.aportaciones.service;

import com.manudev.microservices.cuentas.aportaciones.dto.AportacionesRequest;
import com.manudev.microservices.cuentas.aportaciones.dto.AportacionesResponse;
import com.manudev.microservices.cuentas.aportaciones.entities.EstadoAportes;
import java.util.List;

public interface IAportacionesService {
    AportacionesResponse createAportacion(AportacionesRequest request);
    List<AportacionesResponse> getAportaciones(Long socioId, EstadoAportes estado);
    AportacionesResponse getAportacionById(Long id);
    AportacionesResponse aplicarAportacion(Long id);
    AportacionesResponse anularAportacion(Long id);
}
