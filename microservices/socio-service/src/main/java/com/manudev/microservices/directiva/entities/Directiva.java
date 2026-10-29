package com.manudev.microservices.directiva.entities;

import com.manudev.microservices.socio.entities.Socio;
import jakarta.persistence.*;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "Directivas")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class Directiva {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long directiva_id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "socio_id", nullable = false)
    private Socio socio;
    @Size(max = 255)
    @Column(name = "cargo", nullable = false, length = 255)
    private String cargo;
    @Column(name = "periodo_inicio", nullable = false)
    private LocalDate fecha_inicio;
    @Column(name = "periodo_fin")
    private LocalDate fecha_final;
    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    @Builder.Default
    private EstadoDirectiva estado = EstadoDirectiva.ACTIVO;

    @PrePersist
    public void prePersist() {
        if (estado == null) {
            estado = EstadoDirectiva.ACTIVO;
        }
    }

    @AssertTrue(message = "La fecha final no puede ser anterior a la fecha de inicio")
    public boolean isPeriodoValido() {
        return fecha_inicio == null || fecha_final == null || !fecha_final.isBefore(fecha_inicio);
    }
}
