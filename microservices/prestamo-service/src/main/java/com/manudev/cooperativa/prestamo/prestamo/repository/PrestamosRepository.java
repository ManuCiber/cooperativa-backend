package com.manudev.cooperativa.prestamo.prestamo.repository;

import com.manudev.cooperativa.prestamo.prestamo.entities.Prestamos;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PrestamosRepository extends JpaRepository<Prestamos, Long> {
}
