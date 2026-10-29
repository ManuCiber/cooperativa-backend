package com.manudev.microservices.cuentas.Ahorros_Programados.repository;

import com.manudev.microservices.cuentas.Ahorros_Programados.entities.AhorrosProgramados;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AhorrosProgramadosRepository extends JpaRepository<AhorrosProgramados, Long> {
}
