package com.manudev.cooperativa.prestamo.prestamo.repository;

import com.manudev.cooperativa.prestamo.prestamo.entities.Prestamos;
import com.manudev.cooperativa.prestamo.prestamo.entities.EstadoPrestamos;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface PrestamosRepository extends JpaRepository<Prestamos, Long> {
    List<Prestamos> findByEstadoIn(Collection<EstadoPrestamos> estados);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Prestamos p WHERE p.prestamo_id = :id")
    Optional<Prestamos> findByIdForUpdate(@Param("id") Long id);
}
