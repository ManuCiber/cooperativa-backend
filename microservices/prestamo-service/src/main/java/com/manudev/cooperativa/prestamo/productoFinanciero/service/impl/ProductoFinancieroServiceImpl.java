package com.manudev.cooperativa.prestamo.productoFinanciero.service.impl;

import com.manudev.cooperativa.prestamo.productoFinanciero.dto.ProductoFinancieroRequest;
import com.manudev.cooperativa.prestamo.productoFinanciero.dto.ProductoFinancieroResponse;
import com.manudev.cooperativa.prestamo.productoFinanciero.entity.EstadoProducto;
import com.manudev.cooperativa.prestamo.productoFinanciero.entity.ProductoFinanciero;
import com.manudev.cooperativa.prestamo.productoFinanciero.mapper.ProductoFinancieroMapper;
import com.manudev.cooperativa.prestamo.productoFinanciero.repository.ProductoFinancieroRepository;
import com.manudev.cooperativa.prestamo.productoFinanciero.service.IProductoFinancieroService;
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
public class ProductoFinancieroServiceImpl implements IProductoFinancieroService {

    private final ProductoFinancieroRepository productoFinancieroRepository;
    private final ProductoFinancieroMapper productoFinancieroMapper;
    private final Validator validator;

    @Override
    @Transactional
    public ProductoFinancieroResponse createProducto(ProductoFinancieroRequest request) {
        validateRequest(request);
        validateBusinessRules(request, null);

        ProductoFinanciero producto = productoFinancieroMapper.toEntity(request);
        if (producto.getEstado() == null) {
            producto.setEstado(EstadoProducto.ACTIVO);
        }

        return productoFinancieroMapper.toResponse(productoFinancieroRepository.saveAndFlush(producto));
    }

    @Override
    public List<ProductoFinancieroResponse> getProductos() {
        return productoFinancieroRepository.findAll()
                .stream()
                .map(productoFinancieroMapper::toResponse)
                .toList();
    }

    @Override
    public List<ProductoFinancieroResponse> getProductosByEstado(EstadoProducto estado) {
        return productoFinancieroRepository.findAllByEstado(estado)
                .stream()
                .map(productoFinancieroMapper::toResponse)
                .toList();
    }

    @Override
    public ProductoFinancieroResponse getProductoById(Long id) {
        return productoFinancieroMapper.toResponse(findById(id));
    }

    @Override
    @Transactional
    public ProductoFinancieroResponse updateProducto(Long id, ProductoFinancieroRequest request) {
        validateRequest(request);
        validateBusinessRules(request, id);

        ProductoFinanciero producto = findById(id);
        productoFinancieroMapper.updateEntity(producto, request);

        return productoFinancieroMapper.toResponse(productoFinancieroRepository.saveAndFlush(producto));
    }

    @Override
    @Transactional
    public ProductoFinancieroResponse cambiarEstado(Long id, EstadoProducto estado) {
        if (estado == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El nuevo estado no puede ser nulo");
        }
        ProductoFinanciero producto = findById(id);
        producto.setEstado(estado);
        return productoFinancieroMapper.toResponse(productoFinancieroRepository.saveAndFlush(producto));
    }

    private ProductoFinanciero findById(Long id) {
        if (id == null || id <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El ID del producto debe ser positivo");
        }
        return productoFinancieroRepository.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto financiero no encontrado con ID: " + id));
    }

    private void validateBusinessRules(ProductoFinancieroRequest request, Long currentId) {
        if (currentId == null) {
            if (productoFinancieroRepository.existsByNombre(request.getNombre())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "Ya existe un producto financiero con el nombre: " + request.getNombre());
            }
        } else {
            if (productoFinancieroRepository.existsByNombreAndIdNot(request.getNombre(), currentId)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "Ya existe otro producto financiero con el nombre: " + request.getNombre());
            }
        }

        if (request.getPlazoMinimo() != null && request.getPlazoMaximo() != null
                && request.getPlazoMinimo() > request.getPlazoMaximo()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "El plazo mínimo no puede ser mayor que el plazo máximo");
        }

        if (request.getMontoMinimo() != null && request.getMontoMaximo() != null
                && request.getMontoMinimo().compareTo(request.getMontoMaximo()) > 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "El monto mínimo no puede ser mayor que el monto máximo");
        }
    }

    private void validateRequest(ProductoFinancieroRequest request) {
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Los datos del producto financiero son obligatorios");
        }
        var violations = validator.validate(request);
        if (!violations.isEmpty()) {
            String message = violations.stream()
                    .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                    .sorted().collect(Collectors.joining("; "));
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
        }
    }
}
