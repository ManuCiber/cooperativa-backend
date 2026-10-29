package com.manudev.cooperativa.prestamo.PagoPrestamos.repositories;

import com.manudev.cooperativa.prestamo.PagoPrestamos.entities.EstadoPagosPrestamo;
import com.manudev.cooperativa.prestamo.PagoPrestamos.entities.PagoPrestamos;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface PagoPrestamosRepository extends JpaRepository<PagoPrestamos, Long> {

    // Se usa @Query porque los campos tienen guion bajo (prestamo_id, numero_cuota...)
    // y Spring Data lo interpreta como separador de propiedades en los metodos derivados.

    /** Cuotas de un prestamo con un estado exacto (tambien sirve para ver las ELIMINADO). */
    @Query("SELECT p FROM PagoPrestamos p WHERE p.prestamo_id = :prestamoId AND p.estado = :estado ORDER BY p.numero_cuota ASC")
    List<PagoPrestamos> findByPrestamoAndEstado(@Param("prestamoId") Long prestamoId,
                                                @Param("estado") EstadoPagosPrestamo estado);

    /** Cuotas de un prestamo excluyendo un estado (por defecto, ELIMINADO). */
    @Query("SELECT p FROM PagoPrestamos p WHERE p.prestamo_id = :prestamoId AND p.estado <> :excluido ORDER BY p.numero_cuota ASC")
    List<PagoPrestamos> findByPrestamoAndEstadoNot(@Param("prestamoId") Long prestamoId,
                                                   @Param("excluido") EstadoPagosPrestamo excluido);

    /** Una cuota especifica de un prestamo, ignorando las del estado excluido. */
    @Query("SELECT p FROM PagoPrestamos p WHERE p.prestamo_id = :prestamoId AND p.numero_cuota = :numeroCuota AND p.estado <> :excluido")
    Optional<PagoPrestamos> findCuota(@Param("prestamoId") Long prestamoId,
                                      @Param("numeroCuota") Integer numeroCuota,
                                      @Param("excluido") EstadoPagosPrestamo excluido);

    /** Para evitar duplicar una cuota al crearla (las ELIMINADO no cuentan). */
    @Query("SELECT COUNT(p) > 0 FROM PagoPrestamos p WHERE p.prestamo_id = :prestamoId AND p.numero_cuota = :numeroCuota AND p.estado <> :excluido")
    boolean existsCuota(@Param("prestamoId") Long prestamoId,
                        @Param("numeroCuota") Integer numeroCuota,
                        @Param("excluido") EstadoPagosPrestamo excluido);

    /** Cuotas vencidas: en el estado dado (PENDIENTE) y con vencimiento anterior a la fecha. */
    @Query("SELECT p FROM PagoPrestamos p WHERE p.estado = :estado AND p.fecha_vencimiento < :fecha ORDER BY p.fecha_vencimiento ASC")
    List<PagoPrestamos> findVencidas(@Param("estado") EstadoPagosPrestamo estado,
                                     @Param("fecha") LocalDate fecha);

    /** Proxima cuota con el estado dado (requiere Hibernate 6.5+ por el LIMIT). */
    @Query("SELECT p FROM PagoPrestamos p WHERE p.prestamo_id = :prestamoId AND p.estado = :estado ORDER BY p.numero_cuota ASC LIMIT 1")
    Optional<PagoPrestamos> findProximaCuota(@Param("prestamoId") Long prestamoId,
                                             @Param("estado") EstadoPagosPrestamo estado);
}