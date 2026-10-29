package com.manudev.microservices.cuentas.transacciones.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.manudev.microservices.cuentas.transacciones.entities.TipoTransacciones;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransaccionesRequest {
    @NotNull(message = "La cuenta es obligatoria")
    @Positive(message = "El ID de cuenta debe ser positivo")
    private Long cuentaId;

    @NotNull(message = "El tipo de transaccion es obligatorio")
    private TipoTransacciones tipo_transaccion;

    @NotNull(message = "El monto es obligatorio")
    @Positive(message = "El monto debe ser mayor que cero")
    @Digits(integer = 16, fraction = 2, message = "El monto admite hasta 16 enteros y 2 decimales")
    private BigDecimal monto;

    // Si se omite, la entidad asigna la fecha antes de persistir.
    private LocalDateTime fecha;

    @Size(max = 255, message = "La descripcion no debe superar los 255 caracteres")
    private String descripcion;

    @Positive(message = "El ID de cuenta destino debe ser positivo")
    private Long cuentaDestinoId;

    @JsonIgnore
    @AssertTrue(message = "La transferencia requiere una cuenta destino distinta de la cuenta origen")
    public boolean isTransferenciaValida() {
        if (tipo_transaccion != TipoTransacciones.TRANSFERENCIA) {
            return true;
        }
        return cuentaId != null && cuentaDestinoId != null && !cuentaId.equals(cuentaDestinoId);
    }
}
