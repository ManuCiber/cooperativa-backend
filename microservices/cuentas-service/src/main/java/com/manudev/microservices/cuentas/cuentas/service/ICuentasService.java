package com.manudev.microservices.cuentas.cuentas.service;

import com.manudev.microservices.cuentas.cuentas.dto.CuentasRequest;
import com.manudev.microservices.cuentas.cuentas.dto.CuentasResponse;
import java.util.List;

public interface ICuentasService {
    CuentasResponse createCuenta(CuentasRequest request);
    List<CuentasResponse> getCuentas();
    List<CuentasResponse> getCuentasInactivas();
    CuentasResponse getCuentaById(Long id);
    CuentasResponse updateCuenta(Long id, CuentasRequest request);
    CuentasResponse deleteCuenta(Long id);
}
