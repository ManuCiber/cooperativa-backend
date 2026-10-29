package com.manudev.microservice.configuration.controller;

import com.manudev.microservice.configuration.dto.GeneralConfigurationPatch;
import com.manudev.microservice.configuration.dto.InitialConfigurationRequest;
import com.manudev.microservice.configuration.dto.LoanPoliciesPatch;
import com.manudev.microservice.configuration.dto.NotificationsPatch;
import com.manudev.microservice.configuration.entity.ConfigurationHistory;
import com.manudev.microservice.configuration.entity.SystemConfig;
import com.manudev.microservice.configuration.service.ConfigurationService;
import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
@RequestMapping("/api/v1/configuracion")
@RequiredArgsConstructor
public class ConfigurationController {
    private final ConfigurationService configurationService;

    @PostMapping
    public ResponseEntity<SystemConfig> initialize(
            @NotNull @Valid @RequestBody InitialConfigurationRequest request,
            Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(configurationService.initialize(request, authentication.getName()));
    }

    @GetMapping
    public SystemConfig getActiveConfiguration() {
        return configurationService.getActiveConfiguration();
    }

    @PatchMapping("/general")
    public SystemConfig patchGeneral(
            @NotNull @Valid @RequestBody GeneralConfigurationPatch patch,
            Authentication authentication) {
        return configurationService.patchGeneral(patch, authentication.getName());
    }

    @PatchMapping("/politicas-prestamo")
    public SystemConfig patchLoanPolicies(
            @NotNull @Valid @RequestBody LoanPoliciesPatch patch,
            Authentication authentication) {
        return configurationService.patchLoanPolicies(patch, authentication.getName());
    }

    @PatchMapping("/notificaciones")
    public SystemConfig patchNotifications(
            @NotNull @Valid @RequestBody NotificationsPatch patch,
            Authentication authentication) {
        return configurationService.patchNotifications(patch, authentication.getName());
    }

    @GetMapping("/historial")
    public Page<ConfigurationHistory> getHistory(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return configurationService.getHistory(page, size);
    }
}
