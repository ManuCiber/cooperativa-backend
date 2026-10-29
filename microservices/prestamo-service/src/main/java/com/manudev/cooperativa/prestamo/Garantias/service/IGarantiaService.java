package com.manudev.cooperativa.prestamo.Garantias.service;

import com.manudev.cooperativa.prestamo.Garantias.dto.GarantiaResponse;
import com.manudev.cooperativa.prestamo.Garantias.dto.GarantiasRequest;
import com.manudev.cooperativa.prestamo.Garantias.entities.TipoGarantia;

import java.util.List;

public interface IGarantiaService {
    GarantiaResponse crear(GarantiasRequest request);

    GarantiaResponse obtenerPorId(Long garantiaId);

    List<GarantiaResponse> listarTodas();

    List<GarantiaResponse> listarPorPrestamo(Long prestamoId, TipoGarantia tipoGarantia);

    GarantiaResponse actualizar(Long garantiaId, GarantiasRequest request);

    void eliminar(Long garantiaId);
}
