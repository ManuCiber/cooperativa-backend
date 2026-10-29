package com.manudev.microservices.usuarios_service.empleados.repository;

import com.manudev.microservices.usuarios_service.empleados.entities.Empleado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface EmpleadoRepository extends JpaRepository<Empleado, Long> {
    @Query("SELECT e FROM Empleado e WHERE e.dni_cedula = :dniCedula")
    Optional<Empleado> findByDniCedula(@Param("dniCedula") String dniCedula);

    @Query("SELECT COUNT(e) > 0 FROM Empleado e WHERE e.dni_cedula = :dniCedula")
    boolean existsByDniCedula(@Param("dniCedula") String dniCedula);
}
