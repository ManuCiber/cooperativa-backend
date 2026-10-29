package com.manudev.cooperativa.prestamo.prestamo.service.impl;

import com.manudev.cooperativa.prestamo.prestamo.dto.*;
import com.manudev.cooperativa.prestamo.prestamo.entities.*;
import com.manudev.cooperativa.prestamo.prestamo.mapper.PrestamosMapper;
import com.manudev.cooperativa.prestamo.prestamo.repository.PrestamosRepository;
import com.manudev.cooperativa.prestamo.prestamo.service.IPrestamosService;
import com.manudev.cooperativa.prestamo.productoFinanciero.entity.EstadoProducto;
import com.manudev.cooperativa.prestamo.productoFinanciero.entity.ProductoFinanciero;
import com.manudev.cooperativa.prestamo.productoFinanciero.repository.ProductoFinancieroRepository;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PrestamosServiceImpl implements IPrestamosService {
    private final PrestamosRepository prestamosRepository;
    private final PrestamosMapper prestamosMapper;
    private final Validator validator;
    private final ProductoFinancieroRepository productoFinancieroRepository;

    @Override
    @Transactional
    public PrestamosResponse createPrestamo(PrestamosRequest request) {
        validateRequest(request);
        validateProductoFinanciero(request);
        Prestamos prestamo = prestamosMapper.toEntity(request);
        prestamo.setEstado(EstadoPrestamos.SOLICITADO);
        return prestamosMapper.toResponse(prestamosRepository.saveAndFlush(prestamo));
    }

    @Override
    public List<PrestamosResponse> getPrestamos() {
        return prestamosRepository.findAll().stream().map(prestamosMapper::toResponse).toList();
    }

    @Override
    public PrestamosResponse getPrestamoById(Long id) {
        return prestamosMapper.toResponse(findById(id));
    }

    @Override
    @Transactional
    public PrestamosResponse updatePrestamo(Long id, PrestamosRequest request) {
        validateRequest(request);
        validateProductoFinanciero(request);
        Prestamos prestamo = findById(id);
        requireSolicitado(prestamo);
        prestamosMapper.updateEntity(prestamo, request);
        return prestamosMapper.toResponse(prestamosRepository.saveAndFlush(prestamo));
    }

    @Override
    @Transactional
    public PrestamosResponse cancelarPrestamo(Long id) {
        Prestamos prestamo = findById(id);
        requireSolicitado(prestamo);
        prestamo.setEstado(EstadoPrestamos.CANCELADO);
        return prestamosMapper.toResponse(prestamosRepository.saveAndFlush(prestamo));
    }

    private Prestamos findById(Long id) {
        if (id == null || id <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El ID del prestamo debe ser positivo");
        }
        return prestamosRepository.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Prestamo no encontrado con ID: " + id));
    }

    private void requireSolicitado(Prestamos prestamo) {
        if (prestamo.getEstado() != EstadoPrestamos.SOLICITADO) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Solo se pueden modificar o cancelar prestamos en estado SOLICITADO");
        }
    }

    private void validateRequest(PrestamosRequest request) {
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Los datos del prestamo son obligatorios");
        }
        var violations = validator.validate(request);
        if (!violations.isEmpty()) {
            String message = violations.stream()
                    .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                    .sorted().collect(Collectors.joining("; "));
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
        }
    }

    private ProductoFinanciero validateProductoFinanciero(PrestamosRequest request) {
        if (request.getProductoId() == null || request.getProductoId() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El ID del producto financiero debe ser positivo");
        }

        ProductoFinanciero producto = productoFinancieroRepository.findById(request.getProductoId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Producto financiero no encontrado con ID: " + request.getProductoId()));

        if (producto.getEstado() != EstadoProducto.ACTIVO) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "No se pueden solicitar préstamos de un producto financiero inactivo");
        }

        if (request.getMonto_solicitado() != null) {
            if (producto.getMontoMinimo() != null && request.getMonto_solicitado().compareTo(producto.getMontoMinimo()) < 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        String.format("El monto solicitado (%s) es menor al monto mínimo permitido (%s) para el producto '%s'",
                                request.getMonto_solicitado(), producto.getMontoMinimo(), producto.getNombre()));
            }
            if (producto.getMontoMaximo() != null && request.getMonto_solicitado().compareTo(producto.getMontoMaximo()) > 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        String.format("El monto solicitado (%s) excede el monto máximo permitido (%s) para el producto '%s'",
                                request.getMonto_solicitado(), producto.getMontoMaximo(), producto.getNombre()));
            }
        }

        if (request.getPlazo_meses() != null) {
            if (producto.getPlazoMinimo() != null && request.getPlazo_meses() < producto.getPlazoMinimo()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        String.format("El plazo solicitado (%d meses) es menor al plazo mínimo permitido (%d meses) para el producto '%s'",
                                request.getPlazo_meses(), producto.getPlazoMinimo(), producto.getNombre()));
            }
            if (producto.getPlazoMaximo() != null && request.getPlazo_meses() > producto.getPlazoMaximo()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        String.format("El plazo solicitado (%d meses) excede el plazo máximo permitido (%d meses) para el producto '%s'",
                                request.getPlazo_meses(), producto.getPlazoMaximo(), producto.getNombre()));
            }
        }

        return producto;
    }
}
