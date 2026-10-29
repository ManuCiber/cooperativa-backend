package com.manudev.microservices.directiva.service.impl;

import com.manudev.microservices.directiva.dto.DirectivaRequest;
import com.manudev.microservices.directiva.dto.DirectivaResponse;
import com.manudev.microservices.directiva.entities.Directiva;
import com.manudev.microservices.directiva.entities.EstadoDirectiva;
import com.manudev.microservices.directiva.mapper.DirectivaMapper;
import com.manudev.microservices.directiva.repository.DirectivaRepository;
import com.manudev.microservices.directiva.service.IDirectivaService;
import com.manudev.microservices.socio.entities.Socio;
import com.manudev.microservices.socio.repository.SocioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DirectivaServiceImpl implements IDirectivaService {
    private final DirectivaRepository directivaRepository;
    private final SocioRepository socioRepository;
    private final DirectivaMapper directivaMapper;

    @Override
    @Transactional
    public DirectivaResponse createDirectiva(DirectivaRequest request) {
        validateRequest(request);
        Socio socio = findSocioById(request.getSocioId());
        Directiva directiva = directivaMapper.toEntity(request, socio);
        return directivaMapper.toResponse(directivaRepository.save(directiva));
    }

    @Override
    public List<DirectivaResponse> getDirectivas() {
        return directivaRepository.findAllByEstado(EstadoDirectiva.ACTIVO)
                .stream()
                .map(directivaMapper::toResponse)
                .toList();
    }

    @Override
    public DirectivaResponse getDirectivaById(Long id) {
        return directivaMapper.toResponse(findDirectivaById(id));
    }

    @Override
    @Transactional
    public DirectivaResponse updateDirectiva(Long id, DirectivaRequest request) {
        validateRequest(request);
        Directiva directiva = findDirectivaById(id);
        Socio socio = findSocioById(request.getSocioId());
        directivaMapper.updateEntity(directiva, request, socio);
        return directivaMapper.toResponse(directivaRepository.save(directiva));
    }

    @Override
    @Transactional
    public DirectivaResponse deleteDirectiva(Long id) {
        Directiva directiva = findDirectivaById(id);
        directiva.setEstado(EstadoDirectiva.INACTIVO);
        return directivaMapper.toResponse(directivaRepository.save(directiva));
    }

    private Directiva findDirectivaById(Long id) {
        if (id == null || id <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El ID de directiva debe ser positivo");
        }
        return directivaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Directiva no encontrada con ID: " + id));
    }

    private Socio findSocioById(Long id) {
        return socioRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Socio no encontrado con ID: " + id));
    }

    private void validateRequest(DirectivaRequest request) {
        if (request == null || request.getSocioId() == null || request.getSocioId() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El ID del socio debe ser positivo");
        }
        if (request.getCargo() == null || request.getCargo().isBlank() || request.getCargo().length() > 255) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El cargo es obligatorio y no debe superar los 255 caracteres");
        }
        if (request.getFecha_inicio() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La fecha de inicio es obligatoria");
        }
        if (request.getFecha_final() != null && request.getFecha_final().isBefore(request.getFecha_inicio())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La fecha final no puede ser anterior a la fecha de inicio");
        }
        if (request.getEstado() != null) {
            try {
                EstadoDirectiva.valueOf(request.getEstado());
            } catch (IllegalArgumentException exception) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El estado debe ser ACTIVO o INACTIVO", exception);
            }
        }
    }
}
