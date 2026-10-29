package com.manudev.microservices.cuentas.cuentas.repository;

import com.manudev.microservices.cuentas.cuentas.entities.Cuentas;
import com.manudev.microservices.cuentas.cuentas.entities.EstadoCuenta;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CuentasRepository extends JpaRepository<Cuentas, Long> {
    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query("SELECT c FROM Cuentas c WHERE c.id = :id")
    java.util.Optional<Cuentas> findByIdForUpdate(@org.springframework.data.repository.query.Param("id") Long id);
    List<Cuentas> findAllByEstado(EstadoCuenta estado);
    boolean existsByNumeroCuenta(String numeroCuenta);
    boolean existsByNumeroCuentaAndIdNot(String numeroCuenta, Long id);
}
