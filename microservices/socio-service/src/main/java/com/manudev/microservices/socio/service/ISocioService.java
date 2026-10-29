package com.manudev.microservices.socio.service;


import com.manudev.microservices.socio.dto.SocioRequest;
import com.manudev.microservices.socio.dto.SocioResponse;

import java.util.List;

public interface ISocioService {
    SocioResponse createSocio(SocioRequest socioRequest);
    List<SocioResponse> getSocios();
    SocioResponse getSocioById(Long id);
    SocioResponse updateSocio(Long id, SocioRequest request);
    SocioResponse deleteSocio(Long id);
}
