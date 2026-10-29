package com.manudev.microservices.socio.entities;


import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "Socios", uniqueConstraints ={
        @UniqueConstraint(name = "uk_socio_no_socio", columnNames = "no_socio"),
        @UniqueConstraint(name = "uk_socio_dni", columnNames = "dni_cedula"),
        @UniqueConstraint(name = "uk_socio_email", columnNames = "email"),

})

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class Socio {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "socio_id")
    private long socio_id;

    @Column(name = "no_socio", nullable = false, length = 20)
    private String no_socio;

    @Column(nullable = false, length = 100)
    private String nombre;
    @Column(nullable = false, length = 100)
    private String apellidos;

    @Column(name = "dni_cedula", nullable = false, length = 20)
    private String dni_cedula;

    @Column(name = "fecha_nacimiento")
    private LocalDate fecha_nacimiento;

    @Column(length = 255)
    private String direccion;

    @Column(length = 20)
    private String telefono;

    @Column(length = 150)
    private String email;

    @Column(name = "fecha_ingreso")
    private LocalDate fecha_entrada;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, name = "tipo_socio", length = 30)
    private TipoSocio tipo_socio;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_socio", nullable = false, length = 20)
    private EstadoSocio estado_socio;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        LocalDateTime now =  LocalDateTime.now();
        createdAt = now;
        updatedAt = now;

        if(fecha_entrada == null){
            fecha_entrada=LocalDate.now();
        }
        if(estado_socio == null){
            estado_socio =  EstadoSocio.ACTIVO;
        }
    }

    @PreUpdate
    public void preUpdate(){
        updatedAt = LocalDateTime.now();
    }
}
