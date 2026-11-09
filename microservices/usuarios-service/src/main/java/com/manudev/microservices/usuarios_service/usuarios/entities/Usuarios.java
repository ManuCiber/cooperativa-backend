package com.manudev.microservices.usuarios_service.usuarios.entities;

import com.manudev.microservices.usuarios_service.empleados.entities.Empleado;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Table(name = "Usuarios")
public class Usuarios {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long usuarioId;

    @OneToOne(optional = false)
    @JoinColumn(name = "empleado_id", nullable = false, unique = true)
    private Empleado empleado;
    @Column(name = "nombre_usuario")
    private String nombreUsuario;
    @Column(name = "password")
    private String password;
    @Column(name = "rol_usuario")
    private String rol;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_usuario", nullable = false)
    private EstadoUsuario estadoUsuario;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        LocalDateTime now =  LocalDateTime.now();
        createdAt = now;
        updatedAt = now;

        if(estadoUsuario == null){
            estadoUsuario =  EstadoUsuario.ACTIVO;
        }
    }

    @PreUpdate
    public void preUpdate(){
        updatedAt = LocalDateTime.now();
    }
}
