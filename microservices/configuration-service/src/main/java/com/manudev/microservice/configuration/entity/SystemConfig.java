package com.manudev.microservice.configuration.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.Instant;

@Document(collection = "system_configurations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SystemConfig {
    public static final String ACTIVE_CONFIGURATION_ID = "active";

    @Id
    private String id = ACTIVE_CONFIGURATION_ID;

    @Version
    private Long version;

    private GeneralConfig general;
    private LoanPoliciesConfig loanPolicies;
    private NotificationsConfig notifications;
    private Instant createdAt;
    private Instant updatedAt;
    private String updatedBy;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GeneralConfig {
        private String cooperativeName;
        private String rnc;
        private String phone;
        private String address;
        private BigDecimal mandatoryMonthlyContribution;
        private String currency;

    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LoanPoliciesConfig {
        private BigDecimal defaultLateInterestRate;
        private Integer graceDays;
        private BigDecimal maxPersonalLoanAmount;
        private BigDecimal maxMicroCreditAmount;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NotificationsConfig {
        private Boolean overduePayments;
        private Boolean newRequests;
        private Boolean pendingContributions;
        private Boolean weeklyReports;
    }
}
