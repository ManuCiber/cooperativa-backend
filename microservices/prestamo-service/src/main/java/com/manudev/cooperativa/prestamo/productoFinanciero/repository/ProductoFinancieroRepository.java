package com.manudev.cooperativa.prestamo.productoFinanciero.repository;

import com.manudev.cooperativa.prestamo.productoFinanciero.entity.EstadoProducto;
import com.manudev.cooperativa.prestamo.productoFinanciero.entity.ProductoFinanciero;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductoFinancieroRepository extends JpaRepository<ProductoFinanciero, Long> {

    List<ProductoFinanciero> findAllByEstado(EstadoProducto estado);

    boolean existsByNombre(String nombre);

    boolean existsByNombreAndIdNot(String nombre, Long id);

    Optional<ProductoFinanciero> findByNombre(String nombre);
}
