package com.manudev.microservices.cuentas.aportaciones.repository;

import com.manudev.microservices.cuentas.aportaciones.entities.Aportaciones;
import com.manudev.microservices.cuentas.aportaciones.entities.EstadoAportes;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AportacionesRepository extends JpaRepository<Aportaciones, Long> {
    @Query("""
    SELECT a FROM Aportaciones a
    WHERE a.estado = :estado
    ORDER BY a.fecha_aportacion DESC
    """)
    List<Aportaciones> findAllByEstadoOrderByFechaAportacionDesc(@Param("estado") EstadoAportes estado);

    @Query("""
    SELECT a FROM Aportaciones a
    WHERE a.socio_id = :socioId
    ORDER BY a.fecha_aportacion DESC
    """)
    List<Aportaciones> findAllBySocioId(@Param("socioId") Long socioId);
}
