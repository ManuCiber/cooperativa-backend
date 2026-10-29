package com.manudev.microservices.cuentas.transacciones.repository;

import com.manudev.microservices.cuentas.transacciones.entities.Transacciones;
import com.manudev.microservices.cuentas.transacciones.entities.TipoTransacciones;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface TransaccionesRepository extends JpaRepository<Transacciones, Long> {
    @Query("""
            SELECT t FROM Transacciones t
            WHERE (:socioId IS NULL OR t.socioId = :socioId OR t.socioDestinoId = :socioId)
              AND (:tipo IS NULL OR t.tipo_transaccion = :tipo)
            ORDER BY t.fecha DESC, t.transaccion_id DESC
            """)
    List<Transacciones> findByFiltros(@Param("socioId") Long socioId, @Param("tipo") TipoTransacciones tipo);
}
