package com.manudev.cooperativa.prestamo.Garantias.entities;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TipoGarantia {
    APORTACIONES("Aportaciones"),
    AVAL_PERSONAL("Aval Personal"),
    HIPOTECA_INMUEBLE("Hiporteca Inmueble"),
    PRENDA_VEHICULAR("Prenda Vehicular");

    private final String etiqueta;
}
