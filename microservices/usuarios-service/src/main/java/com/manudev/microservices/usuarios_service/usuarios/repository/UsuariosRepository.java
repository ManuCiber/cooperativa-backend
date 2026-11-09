package com.manudev.microservices.usuarios_service.usuarios.repository;

import com.manudev.microservices.usuarios_service.usuarios.entities.Usuarios;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UsuariosRepository extends JpaRepository<Usuarios, Long> {
    Optional<Usuarios> findByNombreUsuario(String nombreUsuario);

    boolean existsByNombreUsuario(String nombreUsuario);

    boolean existsByNombreUsuarioAndUsuarioIdNot(String nombreUsuario, Long usuarioId);

    @Query("""
            SELECT COUNT(u) > 0
            FROM Usuarios u
            WHERE u.empleado.empleado_id = :empleadoId
            """)
    boolean existsByEmpleadoId(@Param("empleadoId") Long empleadoId);

    @Query("""
            SELECT COUNT(u) > 0
            FROM Usuarios u
            WHERE u.empleado.empleado_id = :empleadoId
              AND u.usuarioId <> :usuarioId
            """)
    boolean existsByEmpleadoIdAndUsuarioIdNot(
            @Param("empleadoId") Long empleadoId,
            @Param("usuarioId") Long usuarioId);
}
