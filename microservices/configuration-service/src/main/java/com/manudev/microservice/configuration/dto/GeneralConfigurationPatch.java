package com.manudev.microservice.configuration.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record GeneralConfigurationPatch(
        @Size(min = 1, max = 150) String cooperativeName,
        @Size(min = 1, max = 30) String rnc,
        @Size(min = 1, max = 30) String phone,
        @Size(min = 1, max = 250) String address,
        @DecimalMin("0.00") BigDecimal mandatoryMonthlyContribution,
        @Pattern(regexp = "^[A-Z]{3}$") String currency) {
}
