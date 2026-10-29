package com.manudev.microservice.configuration.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record InitialConfigurationRequest(
        @NotNull @Valid General general,
        @NotNull @Valid LoanPolicies loanPolicies,
        @NotNull @Valid Notifications notifications) {

    public record General(
            @NotBlank @Size(max = 150) String cooperativeName,
            @NotBlank @Size(max = 30) String rnc,
            @NotBlank @Size(max = 30) String phone,
            @NotBlank @Size(max = 250) String address,
            @NotNull @DecimalMin("0.00") BigDecimal mandatoryMonthlyContribution,
            @NotBlank @Pattern(regexp = "^[A-Z]{3}$") String currency) {
    }

    public record LoanPolicies(
            @NotNull @DecimalMin("0.00") @DecimalMax("100.00") BigDecimal defaultLateInterestRate,
            @NotNull @Min(0) Integer graceDays,
            @NotNull @Positive BigDecimal maxPersonalLoanAmount,
            @NotNull @Positive BigDecimal maxMicroCreditAmount) {
    }

    public record Notifications(
            @NotNull Boolean overduePayments,
            @NotNull Boolean newRequests,
            @NotNull Boolean pendingContributions,
            @NotNull Boolean weeklyReports) {
    }
}
