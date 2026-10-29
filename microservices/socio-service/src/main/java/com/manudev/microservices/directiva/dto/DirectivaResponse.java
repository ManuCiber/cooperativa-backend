package com.manudev.microservices.directiva.dto;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DirectivaResponse {
    private long directivaId;
    private long socioId;
    private String cargo;
    private LocalDate fecha_inicio;
    private LocalDate fecha_final;
    private String estado;
}
