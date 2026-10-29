package com.manudev.microservices.cuentas.Ahorros_Programados.service;

import com.manudev.microservices.cuentas.Ahorros_Programados.dto.AhorrosProgramadosRequest;
import com.manudev.microservices.cuentas.Ahorros_Programados.dto.AhorrosProgramadosResponse;
import java.util.List;

public interface IAhorrosProgramadosService {
    AhorrosProgramadosResponse createAhorroProgramado(AhorrosProgramadosRequest request);
    List<AhorrosProgramadosResponse> getAhorrosProgramados();
    AhorrosProgramadosResponse getAhorroProgramadoById(Long id);
    AhorrosProgramadosResponse updateAhorroProgramado(Long id, AhorrosProgramadosRequest request);
    AhorrosProgramadosResponse cancelarAhorroProgramado(Long id);
}
