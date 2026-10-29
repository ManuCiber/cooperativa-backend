package com.manudev.microservices.cuentas.transacciones.service.impl;

import com.manudev.microservices.cuentas.cuentas.entities.Cuentas;
import com.manudev.microservices.cuentas.cuentas.entities.EstadoCuenta;
import com.manudev.microservices.cuentas.cuentas.repository.CuentasRepository;
import com.manudev.microservices.cuentas.transacciones.dto.*;
import com.manudev.microservices.cuentas.transacciones.entities.TipoTransacciones;
import com.manudev.microservices.cuentas.transacciones.mapper.TransaccionesMapper;
import com.manudev.microservices.cuentas.transacciones.repository.TransaccionesRepository;
import com.manudev.microservices.cuentas.transacciones.service.ITransaccionesService;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TransaccionesServiceImpl implements ITransaccionesService {
    private static final BigDecimal MAX_SALDO = new BigDecimal("9999999999999999.99");
    private final TransaccionesRepository transaccionesRepository;
    private final CuentasRepository cuentasRepository;
    private final TransaccionesMapper mapper;
    private final Validator validator;

    @Override
    @Transactional
    public TransaccionesResponse createTransaccion(TransaccionesRequest request) {
        validateRequest(request);
        Long origenId = request.getCuentaId();
        Long destinoId = request.getCuentaDestinoId();
        Cuentas origen;
        Cuentas destino = null;

        // Todas las transferencias bloquean en el mismo orden para evitar bloqueos cruzados.
        if (destinoId != null && destinoId < origenId) {
            destino = lockCuenta(destinoId);
            origen = lockCuenta(origenId);
        } else {
            origen = lockCuenta(origenId);
            if (destinoId != null) {
                destino = lockCuenta(destinoId);
            }
        }

        BigDecimal saldoOrigen = checkedSaldo(origen);
        BigDecimal saldoDestino = destino == null ? null : checkedSaldo(destino);
        BigDecimal nuevoOrigen = switch (request.getTipo_transaccion()) {
            case DEPOSITO, AJUSTE_CREDITO -> saldoOrigen.add(request.getMonto());
            case RETIRO, AJUSTE_DEBITO, TRANSFERENCIA -> saldoOrigen.subtract(request.getMonto());
        };
        validateResult(nuevoOrigen);
        BigDecimal nuevoDestino = null;
        if (destino != null) {
            nuevoDestino = saldoDestino.add(request.getMonto());
            validateResult(nuevoDestino);
        }

        // Validar ambos resultados antes de modificar las entidades.
        origen.setSaldo_actual(nuevoOrigen);
        cuentasRepository.save(origen);
        if (destino != null) {
            destino.setSaldo_actual(nuevoDestino);
            cuentasRepository.save(destino);
        }
        var transaction = mapper.toEntity(request, origen, destino);
        // save ejecuta @PrePersist y completa la fecha cuando se omite.
        return mapper.toResponse(transaccionesRepository.save(transaction));
    }

    @Override
    public List<TransaccionesResponse> getTransacciones() {
        return transaccionesRepository.findAll(Sort.by(Sort.Direction.DESC, "fecha", "transaccion_id"))
                .stream().map(mapper::toResponse).toList();
    }

    @Override
    public TransaccionesResponse getTransaccionById(Long id) {
        if (id == null || id <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El ID de transaccion debe ser positivo");
        }
        return mapper.toResponse(transaccionesRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Transaccion no encontrada con ID: " + id)));
    }

    @Override
    public List<TransaccionesResponse> getTransacciones(Long socioId, TipoTransacciones tipo) {
        if (socioId != null && socioId <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El ID de socio debe ser positivo");
        }
        if (socioId == null && tipo == null) {
            return getTransacciones();
        }
        return transaccionesRepository.findByFiltros(socioId, tipo).stream().map(mapper::toResponse).toList();
    }
    private Cuentas lockCuenta(Long id) {
        return cuentasRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Cuenta no encontrada con ID: " + id));
    }

    private BigDecimal checkedSaldo(Cuentas cuenta) {
        if (cuenta.getEstado() != EstadoCuenta.ACTIVO) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "La cuenta debe estar activa: " + cuenta.getId());
        }
        if (cuenta.getSaldo_actual() == null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "La cuenta no tiene saldo definido");
        }
        return cuenta.getSaldo_actual();
    }

    private void validateResult(BigDecimal saldo) {
        if (saldo.signum() < 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Saldo insuficiente");
        }
        if (saldo.compareTo(MAX_SALDO) > 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El saldo resultante supera el limite permitido");
        }
    }

    private void validateRequest(TransaccionesRequest request) {
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Los datos de la transaccion son obligatorios");
        }
        var violations = validator.validate(request);
        if (!violations.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, violations.stream()
                    .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                    .sorted().collect(Collectors.joining("; ")));
        }
        if (request.getTipo_transaccion() != TipoTransacciones.TRANSFERENCIA && request.getCuentaDestinoId() != null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "La cuenta destino solo corresponde a una transferencia");
        }
    }
}
