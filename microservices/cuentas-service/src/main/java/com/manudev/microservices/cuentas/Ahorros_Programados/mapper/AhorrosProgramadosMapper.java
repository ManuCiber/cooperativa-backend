package com.manudev.microservices.cuentas.Ahorros_Programados.mapper;

import com.manudev.microservices.cuentas.Ahorros_Programados.dto.*;
import com.manudev.microservices.cuentas.Ahorros_Programados.entities.AhorrosProgramados;
import com.manudev.microservices.cuentas.cuentas.entities.Cuentas;
import org.springframework.stereotype.Component;

@Component
public class AhorrosProgramadosMapper {
    public AhorrosProgramados toEntity(AhorrosProgramadosRequest request, Cuentas cuenta) {
        AhorrosProgramados ahorro = new AhorrosProgramados();
        updateEntity(ahorro, request, cuenta);
        return ahorro;
    }

    public void updateEntity(AhorrosProgramados ahorro, AhorrosProgramadosRequest request, Cuentas cuenta) {
        ahorro.setSocio_id(request.getSocioId());
        ahorro.setCuenta_id(cuenta);
        ahorro.setMonto_periodico(request.getMonto_periodico());
        ahorro.setPeriodicidad(request.getPeriodicidad());
        ahorro.setFecha_inicio(request.getFecha_inicio());
        ahorro.setFecha_fin(request.getFecha_fin());
        ahorro.setEstado(request.getEstado());
    }

    public AhorrosProgramadosResponse toResponse(AhorrosProgramados ahorro) {
        return AhorrosProgramadosResponse.builder()
                .ahorroId(ahorro.getAhorro_id())
                .socioId(ahorro.getSocio_id())
                .cuentaId(ahorro.getCuenta_id().getId())
                .monto_periodico(ahorro.getMonto_periodico())
                .periodicidad(ahorro.getPeriodicidad())
                .fecha_inicio(ahorro.getFecha_inicio())
                .fecha_fin(ahorro.getFecha_fin())
                .estado(ahorro.getEstado())
                .createdAt(ahorro.getCreatedAt())
                .updatedAt(ahorro.getUpdatedAt())
                .build();
    }
}
