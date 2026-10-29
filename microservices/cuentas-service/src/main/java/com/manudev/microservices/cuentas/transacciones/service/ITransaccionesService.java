package com.manudev.microservices.cuentas.transacciones.service;

import com.manudev.microservices.cuentas.transacciones.dto.TransaccionesRequest;
import com.manudev.microservices.cuentas.transacciones.dto.TransaccionesResponse;
import java.util.List;

public interface ITransaccionesService {
    TransaccionesResponse createTransaccion(TransaccionesRequest request);
    List<TransaccionesResponse> getTransacciones();
    List<TransaccionesResponse> getTransacciones(Long socioId, com.manudev.microservices.cuentas.transacciones.entities.TipoTransacciones tipo);
    TransaccionesResponse getTransaccionById(Long id);
}
