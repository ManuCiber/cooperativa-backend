package com.manudev.cooperativa.prestamo.PagoPrestamos.services.impl;

import com.manudev.cooperativa.prestamo.PagoPrestamos.dto.PagoPrestamosRequest;
import com.manudev.cooperativa.prestamo.PagoPrestamos.dto.PagoPrestamosResponse;
import com.manudev.cooperativa.prestamo.PagoPrestamos.entities.EstadoPagosPrestamo;
import com.manudev.cooperativa.prestamo.PagoPrestamos.entities.PagoPrestamos;
import com.manudev.cooperativa.prestamo.PagoPrestamos.mapper.PagoPrestamosMapper;
import com.manudev.cooperativa.prestamo.PagoPrestamos.repositories.PagoPrestamosRepository;
import com.manudev.cooperativa.prestamo.PagoPrestamos.services.PagoPrestamosService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PagoPrestamoServiceImpl implements PagoPrestamosService {

    private final PagoPrestamosRepository repository;
    private final PagoPrestamosMapper mapper;

    @Override
    @Transactional
    public PagoPrestamosResponse crear(PagoPrestamosRequest request) {
        if (repository.existsCuota(request.getPrestamoId(), request.getNumeroCuota(), EstadoPagosPrestamo.ELIMINADO)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Ya existe la cuota " + request.getNumeroCuota() + " para el prestamo " + request.getPrestamoId());
        }
        if (request.getMontoInteres().compareTo(BigDecimal.valueOf(request.getNumeroCuota())) > 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "El monto de interes no puede ser mayor que el monto de la cuota");
        }
        PagoPrestamos guardado = repository.save(mapper.toEntity(request));
        return mapper.toResponse(guardado);
    }

    @Override
    @Transactional(readOnly = true)
    public PagoPrestamosResponse obtenerPorId(Long pagoId) {
        // Devuelve el pago con su estado real, incluso si esta ELIMINADO
        return mapper.toResponse(findOrThrow(pagoId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<PagoPrestamosResponse> listarPorPrestamo(Long prestamoId, EstadoPagosPrestamo estado) {
        List<PagoPrestamos> pagos = (estado == null)
                ? repository.findByPrestamoAndEstadoNot(prestamoId, EstadoPagosPrestamo.ELIMINADO)
                : repository.findByPrestamoAndEstado(prestamoId, estado);

        return pagos.stream().map(mapper::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PagoPrestamosResponse> listarVencidas() {
        return repository.findVencidas(EstadoPagosPrestamo.PENDIENTE, LocalDate.now()).stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public PagoPrestamosResponse registrarPago(Long pagoId) {
        PagoPrestamos pago = findOrThrow(pagoId);

        if (pago.getEstado() == EstadoPagosPrestamo.ELIMINADO) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "No se puede pagar una cuota eliminada");
        }
        if (pago.getEstado() == EstadoPagosPrestamo.PAGADO) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "La cuota ya fue pagada");
        }

        pago.setEstado(EstadoPagosPrestamo.PAGADO);
        pago.setFecha_pago(LocalDateTime.now());
        return mapper.toResponse(repository.save(pago));
    }

    @Override
    @Transactional
    public void eliminarPago(Long pagoId) {
        PagoPrestamos pago = findOrThrow(pagoId);

        if (pago.getEstado() == EstadoPagosPrestamo.PAGADO) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "No se puede eliminar una cuota ya pagada");
        }
        if (pago.getEstado() == EstadoPagosPrestamo.ELIMINADO) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "La cuota ya esta eliminada");
        }

        // Soft delete: solo cambia el estado, los datos se conservan
        pago.setEstado(EstadoPagosPrestamo.ELIMINADO);
        repository.save(pago);
    }

    private PagoPrestamos findOrThrow(Long pagoId) {
        return repository.findById(pagoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "No existe el pago con id " + pagoId));
    }
}