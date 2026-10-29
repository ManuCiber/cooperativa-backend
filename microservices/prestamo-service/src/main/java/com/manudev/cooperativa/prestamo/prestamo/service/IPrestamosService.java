package com.manudev.cooperativa.prestamo.prestamo.service;

import com.manudev.cooperativa.prestamo.prestamo.dto.*;
import java.util.List;

public interface IPrestamosService {
    PrestamosResponse createPrestamo(PrestamosRequest request);
    List<PrestamosResponse> getPrestamos();
    PrestamosResponse getPrestamoById(Long id);
    PrestamosResponse updatePrestamo(Long id, PrestamosRequest request);
    PrestamosResponse cancelarPrestamo(Long id);
}
