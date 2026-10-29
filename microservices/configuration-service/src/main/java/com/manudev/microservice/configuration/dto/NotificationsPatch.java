package com.manudev.microservice.configuration.dto;

public record NotificationsPatch(
        Boolean overduePayments,
        Boolean newRequests,
        Boolean pendingContributions,
        Boolean weeklyReports) {
}
