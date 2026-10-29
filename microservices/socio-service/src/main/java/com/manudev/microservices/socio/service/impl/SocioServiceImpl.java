package com.manudev.microservices.socio.service.impl;

import com.manudev.microservices.socio.dto.SocioRequest;
import com.manudev.microservices.socio.dto.SocioResponse;
import com.manudev.microservices.socio.entities.EstadoSocio;
import com.manudev.microservices.socio.entities.Socio;
import com.manudev.microservices.socio.mapper.SocioMapper;
import com.manudev.microservices.socio.repository.SocioRepository;
import com.manudev.microservices.socio.service.ISocioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SocioServiceImpl implements ISocioService {
    private final SocioRepository socioRepository;
    private final SocioMapper socioMapper;

    @Override
    @Transactional
    public SocioResponse createSocio (SocioRequest lv_request){
        validateDuplicateSocio(lv_request);
        Socio lv_socio = socioMapper.toEntity(lv_request);
        lv_socio.setNo_socio(String.valueOf(socioRepository.getNextSocioNumber()));
        lv_socio.setEstado_socio(EstadoSocio.ACTIVO);
        Socio lv_socioSaved = socioRepository.save(lv_socio);

        return socioMapper.toResponse(lv_socioSaved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SocioResponse> getSocios(){
        List<Socio> lv_socios = socioRepository.findAllByEstadoSocio(EstadoSocio.ACTIVO);
        return lv_socios
                .stream()
                .map(lv_socio -> socioMapper.toResponse(lv_socio))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public SocioResponse getSocioById(Long id){
        Socio lv_socio = findSocioById(id);
        return socioMapper.toResponse(lv_socio);
    }

    public SocioResponse updateSocio(Long id, SocioRequest lv_request){
        Socio lv_socio = findSocioById(id);
        socioMapper.updateEntity(lv_socio, lv_request);
        Socio lv_socioUpdated = socioRepository.save(lv_socio);
        return socioMapper.toResponse(lv_socioUpdated);
    }

    /*
     * Borrado Lógico del Socio (Es un cambio de estado de "ACTIVO" a "INACTIVO"
     * */

    @Override
    @Transactional
    public SocioResponse deleteSocio(Long id){
        Socio lv_socio = findSocioById(id);
        if(lv_socio.getEstado_socio() == EstadoSocio.INACTIVO) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Socio no encontrado");
        }
        lv_socio.setEstado_socio(EstadoSocio.INACTIVO);
        socioRepository.save(lv_socio);
        return null;
    }

    /**
     * Metodos Privados de la implementación para Validar datos y/o existencia del Socio
     * */

    private Socio findSocioById(Long id) {
        return socioRepository
                .findById(id)
                .orElseThrow(
                        () -> new ResponseStatusException(
                                HttpStatus.NOT_FOUND, "Socio no encontrado con ID: " + id
                        )
                );
    }

    private void validateDuplicateSocio(SocioRequest lv_request){
        boolean DniCedulaExits = socioRepository.existsByDniCedula(
                lv_request.getDni_cedula()
        );
        if(DniCedulaExits){
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El DNI o cédula ya está registrado");
        }

        if(lv_request.getEmail() != null && !lv_request.getEmail().isBlank()){
            boolean emailExits = socioRepository.existsByEmail(lv_request.getEmail());
            if(emailExits){
                throw new ResponseStatusException(HttpStatus.CONFLICT, "El correo electrónico ya está registrado");
            }
        }
    }
}
