package com.manudev.microservices.socio.repository;

import com.manudev.microservices.socio.entities.EstadoSocio;
import com.manudev.microservices.socio.entities.Socio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface SocioRepository extends JpaRepository<Socio, Long> {
    @Query("SELECT COUNT(s) > 0 FROM Socio s WHERE s.dni_cedula = ?1")
    boolean existsByDniCedula(String dni_cedula);
    @Query("SELECT COUNT(s) > 0 FROM Socio s WHERE s.email = ?1")
    boolean existsByEmail(String email);
    @Query(
            value = "SELECT nextval('socios.socio_no_seq')",
            nativeQuery = true
    )
    Long getNextSocioNumber();
    @Query("SELECT s FROM Socio s WHERE s.estado_socio = ?1")
    List<Socio> findAllByEstadoSocio(EstadoSocio estado_socio);
}
