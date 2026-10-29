package com.manudev.microservices.usuarios_service.empleados.service;

import com.manudev.microservices.usuarios_service.empleados.dto.EmpleadoRequest;
import com.manudev.microservices.usuarios_service.empleados.dto.EmpleadoResponse;

import java.util.List;

public interface IEmpleadoService {
    EmpleadoResponse createEmpleado(EmpleadoRequest request);
    List<EmpleadoResponse> getEmpleados();
    EmpleadoResponse getEmpleadoById(Long id);
    EmpleadoResponse updateEmpleado(Long id, EmpleadoRequest request);
    EmpleadoResponse deleteEmpleado(Long id);
}
