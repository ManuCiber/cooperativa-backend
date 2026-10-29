package com.manudev.microservices.cuentas.cuentas.mapper;

import com.manudev.microservices.cuentas.cuentas.dto.CuentasRequest;
import com.manudev.microservices.cuentas.cuentas.dto.CuentasResponse;
import com.manudev.microservices.cuentas.cuentas.entities.Cuentas;
import org.springframework.stereotype.Component;

@Component
public class CuentasMapper {
    public Cuentas toEntity(CuentasRequest request) {
        Cuentas cuenta = new Cuentas();
        updateEntity(cuenta, request);
        return cuenta;
    }

    public CuentasResponse toResponse(Cuentas cuenta) {
        return CuentasResponse.builder()
                .id(cuenta.getId())
                .socioId(cuenta.getSocio_id())
                .numeroCuenta(cuenta.getNumeroCuenta())
                .tipoCuenta(cuenta.getTipoCuenta())
                .fecha_apertura(cuenta.getFecha_apertura())
                .saldo_actual(cuenta.getSaldo_actual())
                .estado(cuenta.getEstado())
                .build();
    }

    public void updateEntity(Cuentas cuenta, CuentasRequest request) {
        cuenta.setSocio_id(request.getSocioId());
        cuenta.setNumeroCuenta(request.getNumeroCuenta());
        cuenta.setTipoCuenta(request.getTipoCuenta());
        cuenta.setFecha_apertura(request.getFecha_apertura());
        cuenta.setSaldo_actual(request.getSaldo_actual());
        cuenta.setEstado(request.getEstado());
    }
}
