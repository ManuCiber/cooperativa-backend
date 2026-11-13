package com.manudev.cooperativa.prestamo.PagoPrestamos.services.impl;

import com.manudev.cooperativa.prestamo.PagoPrestamos.dto.PagoPrestamosRequest;
import com.manudev.cooperativa.prestamo.PagoPrestamos.dto.PagoPrestamosResponse;
import com.manudev.cooperativa.prestamo.PagoPrestamos.entities.EstadoPagosPrestamo;
import com.manudev.cooperativa.prestamo.PagoPrestamos.entities.PagoPrestamos;
import com.manudev.cooperativa.prestamo.PagoPrestamos.mapper.PagoPrestamosMapper;
import com.manudev.cooperativa.prestamo.PagoPrestamos.repositories.PagoPrestamosRepository;
import com.manudev.cooperativa.prestamo.PagoPrestamos.services.PagoPrestamosService;
import com.manudev.cooperativa.prestamo.prestamo.entities.EstadoPrestamos;
import com.manudev.cooperativa.prestamo.prestamo.entities.Prestamos;
import com.manudev.cooperativa.prestamo.prestamo.repository.PrestamosRepository;
import lombok.RequiredArgsConstructor;
import java.time.Clock;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PagoPrestamoServiceImpl implements PagoPrestamosService {

    private final PagoPrestamosRepository repository;
    private final PagoPrestamosMapper mapper;
    private final PrestamosRepository prestamosRepository;
    private final Clock clock;

    @Override
    @Transactional
    public PagoPrestamosResponse crear(PagoPrestamosRequest request) {
        throw new ResponseStatusException(HttpStatus.CONFLICT,
                "Las cuotas se generan automáticamente al desembolsar el préstamo");
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
        return repository.findVencidas(EstadoPagosPrestamo.PENDIENTE, LocalDate.now(clock)).stream()
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

        Prestamos prestamo = obtenerPrestamo(pago.getPrestamo_id());
        if (prestamo.getEstado() != EstadoPrestamos.ACTIVO
                && prestamo.getEstado() != EstadoPrestamos.EN_MORA) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Solo se pueden registrar pagos de préstamos activos o en mora");
        }
        pago.setEstado(EstadoPagosPrestamo.PAGADO);
        pago.setFecha_pago(LocalDateTime.now(clock));
        PagoPrestamos guardado = repository.save(pago);
        actualizarEstadoPrestamo(prestamo);
        return mapper.toResponse(guardado);
    }

    @Override
    @Transactional
    public void eliminarPago(Long pagoId) {
        findOrThrow(pagoId);
        throw new ResponseStatusException(HttpStatus.CONFLICT,
                "Las cuotas del cronograma desembolsado no se pueden eliminar");
    }

    @Override
    @Transactional
    public void revisarEstadosMora() {
        List<Prestamos> prestamos = prestamosRepository.findByEstadoIn(
                List.of(EstadoPrestamos.ACTIVO, EstadoPrestamos.EN_MORA));
        prestamos.forEach(this::actualizarEstadoPrestamo);
    }

    private void actualizarEstadoPrestamo(Prestamos prestamo) {
        if (prestamo.getEstado() != EstadoPrestamos.ACTIVO
                && prestamo.getEstado() != EstadoPrestamos.EN_MORA) {
            return;
        }

        boolean tienePendientes = repository.existsByPrestamoAndEstado(
                prestamo.getPrestamo_id(), EstadoPagosPrestamo.PENDIENTE);
        if (!tienePendientes) {
            boolean tienePagosRegistrados = repository.existsByPrestamoAndEstado(
                    prestamo.getPrestamo_id(), EstadoPagosPrestamo.PAGADO);
            prestamo.setEstado(tienePagosRegistrados ? EstadoPrestamos.PAGADO : EstadoPrestamos.ACTIVO);
        } else if (repository.existsVencidaByPrestamo(
                prestamo.getPrestamo_id(), EstadoPagosPrestamo.PENDIENTE, LocalDate.now(clock))) {
            prestamo.setEstado(EstadoPrestamos.EN_MORA);
        } else {
            prestamo.setEstado(EstadoPrestamos.ACTIVO);
        }
        prestamosRepository.save(prestamo);
    }

    private Prestamos obtenerPrestamo(Long prestamoId) {
        return prestamosRepository.findById(prestamoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "No existe el préstamo con id " + prestamoId));
    }

    private PagoPrestamos findOrThrow(Long pagoId) {
        return repository.findById(pagoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "No existe el pago con id " + pagoId));
    }
}