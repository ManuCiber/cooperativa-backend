package com.manudev.cooperativa.prestamo.Garantias.repository;

import com.manudev.cooperativa.prestamo.Garantias.entities.EstadoGarantia;
import com.manudev.cooperativa.prestamo.Garantias.entities.Garantias;
import com.manudev.cooperativa.prestamo.Garantias.entities.TipoGarantia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface IGarantiasRepository extends JpaRepository<Garantias, Long> {

    @Query("SELECT g FROM Garantias g WHERE g.estado = :estado ORDER BY g.garantia_id ASC")
    List<Garantias> findByEstado(@Param("estado") EstadoGarantia estado);

    @Query("SELECT g FROM Garantias g WHERE g.prestamo_id = :prestamo_id AND g.estado = :estado ORDER BY g.garantia_id ASC")
    List<Garantias> findByPrestamoId(@Param("prestamo_id") Long prestamo_id,
                                     @Param("estado") EstadoGarantia estado);

    @Query("SELECT g FROM Garantias g WHERE g.prestamo_id = :prestamo_id AND g.tipo_garantia = :tipo_garantia AND g.estado = :estado ORDER BY g.garantia_id ASC")
    List<Garantias> findByPrestamoIdAndTipoGarantia(
            @Param("prestamo_id") Long prestamo_id,
            @Param("tipo_garantia") TipoGarantia tipo_garantia,
            @Param("estado") EstadoGarantia estado);

    @Query("SELECT COALESCE(SUM(g.valor_estimado), 0) FROM Garantias g WHERE g.prestamo_id = :prestamo_id AND g.estado = :estado")
    BigDecimal sumValorEstimadoByPrestamoId(@Param("prestamo_id") Long prestamo_id,
                                            @Param("estado") EstadoGarantia estado);

    @Query("SELECT COUNT(g) > 0 FROM Garantias g WHERE g.prestamo_id = :prestamo_id AND g.estado = :estado")
    boolean existsByPrestamoId(@Param("prestamo_id") Long prestamo_id,
                               @Param("estado") EstadoGarantia estado);
}
