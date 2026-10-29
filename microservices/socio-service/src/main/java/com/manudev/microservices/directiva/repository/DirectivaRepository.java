package com.manudev.microservices.directiva.repository;

import com.manudev.microservices.directiva.entities.Directiva;
import com.manudev.microservices.directiva.entities.EstadoDirectiva;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DirectivaRepository extends JpaRepository<Directiva, Long> {
    List<Directiva> findAllByEstado(EstadoDirectiva estado);
}
