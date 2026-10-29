package com.manudev.cooperativa.prestamo.Garantias.entities;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;


@Entity
@Table(name = "garantias", indexes = @Index(name = "idx_garantia_prestamo_id", columnList = "prestamo_id") )
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class Garantias {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "garantia_id", nullable = false)
    private Long garantia_id;

    @Column(name = "prestamo_id", nullable = false)
    private Long prestamo_id;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_garantia", nullable = false)
    private TipoGarantia tipo_garantia;

    @Column(name = "valor_estimado", precision = 16, scale = 2)
    private BigDecimal valor_estimado;

    @Column(name = "documentos", nullable = false)
    private String documentos;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false)
    private EstadoGarantia estado;

    @PrePersist
    public void prePersist(){
        LocalDateTime now = LocalDateTime.now();
        createAt = now;
        updatedAt = now;
        if(tipo_garantia==null){
            tipo_garantia = TipoGarantia.APORTACIONES;
        }
        if(estado==null){
            estado = EstadoGarantia.ACTIVO;
        }
    }

    @PreUpdate
    public void preUpdate(){ updatedAt=LocalDateTime.now(); }

}
