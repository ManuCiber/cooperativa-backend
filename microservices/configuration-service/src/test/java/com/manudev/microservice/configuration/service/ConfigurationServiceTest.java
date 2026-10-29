package com.manudev.microservice.configuration.service;

import com.manudev.microservice.configuration.dto.GeneralConfigurationPatch;
import com.manudev.microservice.configuration.dto.InitialConfigurationRequest;
import com.manudev.microservice.configuration.entity.ConfigurationHistory;
import com.manudev.microservice.configuration.entity.SystemConfig;
import com.manudev.microservice.configuration.repository.ConfigurationHistoryRepository;
import com.manudev.microservice.configuration.repository.SystemConfigRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ConfigurationServiceTest {
    private SystemConfigRepository configRepository;
    private ConfigurationHistoryRepository historyRepository;
    private ConfigurationService service;

    @BeforeEach
    void setUp() {
        configRepository = mock(SystemConfigRepository.class);
        historyRepository = mock(ConfigurationHistoryRepository.class);
        service = new ConfigurationService(configRepository, historyRepository);
    }

    @Test
    void initializesTheSingleConfigurationAndRecordsHistory() {
        when(configRepository.existsById(SystemConfig.ACTIVE_CONFIGURATION_ID)).thenReturn(false);
        when(configRepository.save(any(SystemConfig.class))).thenAnswer(invocation -> invocation.getArgument(0));

        SystemConfig saved = service.initialize(initialRequest(), "admin");

        assertEquals("Cooperativa Central", saved.getGeneral().getCooperativeName());
        assertEquals(SystemConfig.ACTIVE_CONFIGURATION_ID, saved.getId());
        assertEquals("admin", saved.getUpdatedBy());
        verify(historyRepository).save(any(ConfigurationHistory.class));
    }

    @Test
    void rejectsASecondInitialization() {
        when(configRepository.existsById(SystemConfig.ACTIVE_CONFIGURATION_ID)).thenReturn(true);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> service.initialize(initialRequest(), "admin"));

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        verify(configRepository, never()).save(any(SystemConfig.class));
        verify(historyRepository, never()).save(any(ConfigurationHistory.class));
    }

    @Test
    void partiallyUpdatesGeneralSettingsAndAuditsTheChange() {
        SystemConfig config = initializedConfig();
        when(configRepository.findById(SystemConfig.ACTIVE_CONFIGURATION_ID)).thenReturn(Optional.of(config));
        when(configRepository.save(any(SystemConfig.class))).thenAnswer(invocation -> invocation.getArgument(0));
        GeneralConfigurationPatch patch = new GeneralConfigurationPatch(
                "Cooperativa Nueva", null, null, null, null, null);

        SystemConfig updated = service.patchGeneral(patch, "admin");

        assertEquals("Cooperativa Nueva", updated.getGeneral().getCooperativeName());
        assertEquals("123456789", updated.getGeneral().getRnc());
        assertEquals("admin", updated.getUpdatedBy());
        verify(historyRepository).save(any(ConfigurationHistory.class));
    }

    @Test
    void rejectsAnEmptyPatch() {
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> service.patchGeneral(new GeneralConfigurationPatch(null, null, null, null, null, null), "admin"));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        verify(configRepository, never()).findById(any());
    }

    private InitialConfigurationRequest initialRequest() {
        return new InitialConfigurationRequest(
                new InitialConfigurationRequest.General(
                        "Cooperativa Central",
                        "123456789",
                        "8095551234",
                        "Calle Principal 10",
                        new BigDecimal("100.00"),
                        "DOP"),
                new InitialConfigurationRequest.LoanPolicies(
                        new BigDecimal("2.50"),
                        5,
                        new BigDecimal("100000.00"),
                        new BigDecimal("25000.00")),
                new InitialConfigurationRequest.Notifications(true, true, false, false));
    }

    private SystemConfig initializedConfig() {
        InitialConfigurationRequest request = initialRequest();
        return new SystemConfig(
                SystemConfig.ACTIVE_CONFIGURATION_ID,
                0L,
                new SystemConfig.GeneralConfig(
                        request.general().cooperativeName(),
                        request.general().rnc(),
                        request.general().phone(),
                        request.general().address(),
                        request.general().mandatoryMonthlyContribution(),
                        request.general().currency()),
                new SystemConfig.LoanPoliciesConfig(
                        request.loanPolicies().defaultLateInterestRate(),
                        request.loanPolicies().graceDays(),
                        request.loanPolicies().maxPersonalLoanAmount(),
                        request.loanPolicies().maxMicroCreditAmount()),
                new SystemConfig.NotificationsConfig(
                        request.notifications().overduePayments(),
                        request.notifications().newRequests(),
                        request.notifications().pendingContributions(),
                        request.notifications().weeklyReports()),
                null,
                null,
                "admin");
    }
}
