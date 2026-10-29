package com.manudev.microservices.cuentas.transacciones.mapper;

import com.manudev.microservices.cuentas.cuentas.entities.Cuentas;
import com.manudev.microservices.cuentas.transacciones.dto.TransaccionesRequest;
import com.manudev.microservices.cuentas.transacciones.dto.TransaccionesResponse;
import com.manudev.microservices.cuentas.transacciones.entities.Transacciones;
import org.springframework.stereotype.Component;

@Component
public class TransaccionesMapper {
    public Transacciones toEntity(TransaccionesRequest request, Cuentas cuenta, Cuentas destino) {
        return Transacciones.builder().cuenta(cuenta).cuenta_destino(destino)
                .socioId(cuenta.getSocio_id()).socioDestinoId(destino == null ? null : destino.getSocio_id())
                .tipo_transaccion(request.getTipo_transaccion()).monto(request.getMonto())
                .fecha(request.getFecha()).descripcion(request.getDescripcion()).build();
    }

    public TransaccionesResponse toResponse(Transacciones transaction) {
        return TransaccionesResponse.builder()
                .transaccionId(transaction.getTransaccion_id())
                .realizadaPorUsuarioId(transaction.getRealizadaPorUsuarioId())
                .cuentaId(transaction.getCuenta().getId())
                .socioId(transaction.getSocioId()).socioDestinoId(transaction.getSocioDestinoId())
                .cuentaDestinoId(transaction.getCuenta_destino() == null
                        ? null : transaction.getCuenta_destino().getId())
                .tipo_transaccion(transaction.getTipo_transaccion())
                .monto(transaction.getMonto()).fecha(transaction.getFecha())
                .descripcion(transaction.getDescripcion()).build();
    }
}
