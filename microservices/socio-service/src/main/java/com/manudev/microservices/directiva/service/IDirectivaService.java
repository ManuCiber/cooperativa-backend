package com.manudev.microservices.directiva.service;

import com.manudev.microservices.directiva.dto.DirectivaRequest;
import com.manudev.microservices.directiva.dto.DirectivaResponse;

import java.util.List;

public interface IDirectivaService {
    DirectivaResponse createDirectiva(DirectivaRequest directivaRequest);
    List<DirectivaResponse> getDirectivas();
    DirectivaResponse getDirectivaById(Long directiva_id);
    DirectivaResponse updateDirectiva(Long directiva_id, DirectivaRequest directivaRequest);
    DirectivaResponse deleteDirectiva(Long directiva_id);
}
