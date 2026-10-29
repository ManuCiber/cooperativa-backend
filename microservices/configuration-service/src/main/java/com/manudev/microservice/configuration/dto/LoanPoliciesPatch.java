package com.manudev.microservice.configuration.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record LoanPoliciesPatch(
        @DecimalMin("0.00") @DecimalMax("100.00") BigDecimal defaultLateInterestRate,
        @Min(0) Integer graceDays,
        @Positive BigDecimal maxPersonalLoanAmount,
        @Positive BigDecimal maxMicroCreditAmount) {
}
