package com.manudev.microservices.directiva.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor(onConstructor_ = @JsonCreator)
@AllArgsConstructor
@Builder
public class DirectivaRequest {
    @NotNull(message = "El socio es obligatorio")
    @Positive(message = "El ID del socio debe ser positivo")
    private Long socioId;

    @NotBlank(message = "El cargo es obligatorio")
    @Size(max = 255, message = "El cargo no debe superar los 255 caracteres")
    private String cargo;

    @NotNull(message = "La fecha de inicio es obligatoria")
    private LocalDate fecha_inicio;

    private LocalDate fecha_final;

    @NotBlank(message = "El estado es obligatorio")
    @Size(max = 255, message = "El estado no debe superar los 255 caracteres")
    @Builder.Default
    private String estado = "ACTIVO";
}
