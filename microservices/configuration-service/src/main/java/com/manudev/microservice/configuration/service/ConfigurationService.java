package com.manudev.microservice.configuration.service;

import com.manudev.microservice.configuration.dto.GeneralConfigurationPatch;
import com.manudev.microservice.configuration.dto.InitialConfigurationRequest;
import com.manudev.microservice.configuration.dto.LoanPoliciesPatch;
import com.manudev.microservice.configuration.dto.NotificationsPatch;
import com.manudev.microservice.configuration.entity.ConfigurationHistory;
import com.manudev.microservice.configuration.entity.SystemConfig;
import com.manudev.microservice.configuration.repository.ConfigurationHistoryRepository;
import com.manudev.microservice.configuration.repository.SystemConfigRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class ConfigurationService {
    private final SystemConfigRepository configRepository;
    private final ConfigurationHistoryRepository historyRepository;

    public SystemConfig getActiveConfiguration() {
        return configRepository.findById(SystemConfig.ACTIVE_CONFIGURATION_ID)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "La configuración todavía no ha sido inicializada"));
    }

    public SystemConfig initialize(InitialConfigurationRequest request, String actor) {
        if (configRepository.existsById(SystemConfig.ACTIVE_CONFIGURATION_ID)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "La configuración ya fue inicializada");
        }

        Instant now = Instant.now();
        SystemConfig config = new SystemConfig(
                SystemConfig.ACTIVE_CONFIGURATION_ID,
                null,
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
                now,
                now,
                actor);
        SystemConfig saved = configRepository.save(config);
        recordHistory("CREATE", "all", actor, null, copy(saved));
        return saved;
    }

    public SystemConfig patchGeneral(GeneralConfigurationPatch patch, String actor) {
        if (patch == null || allNull(
                patch.cooperativeName(), patch.rnc(), patch.phone(), patch.address(),
                patch.mandatoryMonthlyContribution(), patch.currency())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Debe indicar al menos un campo para actualizar");
        }
        SystemConfig config = getActiveConfiguration();
        SystemConfig before = copy(config);
        var general = config.getGeneral();
        if (patch.cooperativeName() != null) general.setCooperativeName(patch.cooperativeName());
        if (patch.rnc() != null) general.setRnc(patch.rnc());
        if (patch.phone() != null) general.setPhone(patch.phone());
        if (patch.address() != null) general.setAddress(patch.address());
        if (patch.mandatoryMonthlyContribution() != null) {
            general.setMandatoryMonthlyContribution(patch.mandatoryMonthlyContribution());
        }
        if (patch.currency() != null) general.setCurrency(patch.currency());
        return saveAndAudit(config, "general", actor, before);
    }

    public SystemConfig patchLoanPolicies(LoanPoliciesPatch patch, String actor) {
        if (patch == null || allNull(
                patch.defaultLateInterestRate(), patch.graceDays(),
                patch.maxPersonalLoanAmount(), patch.maxMicroCreditAmount())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Debe indicar al menos un campo para actualizar");
        }
        SystemConfig config = getActiveConfiguration();
        SystemConfig before = copy(config);
        var policies = config.getLoanPolicies();
        if (patch.defaultLateInterestRate() != null) {
            policies.setDefaultLateInterestRate(patch.defaultLateInterestRate());
        }
        if (patch.graceDays() != null) policies.setGraceDays(patch.graceDays());
        if (patch.maxPersonalLoanAmount() != null) {
            policies.setMaxPersonalLoanAmount(patch.maxPersonalLoanAmount());
        }
        if (patch.maxMicroCreditAmount() != null) {
            policies.setMaxMicroCreditAmount(patch.maxMicroCreditAmount());
        }
        return saveAndAudit(config, "loanPolicies", actor, before);
    }

    public SystemConfig patchNotifications(NotificationsPatch patch, String actor) {
        if (patch == null || allNull(
                patch.overduePayments(), patch.newRequests(),
                patch.pendingContributions(), patch.weeklyReports())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Debe indicar al menos un campo para actualizar");
        }
        SystemConfig config = getActiveConfiguration();
        SystemConfig before = copy(config);
        var notifications = config.getNotifications();
        if (patch.overduePayments() != null) notifications.setOverduePayments(patch.overduePayments());
        if (patch.newRequests() != null) notifications.setNewRequests(patch.newRequests());
        if (patch.pendingContributions() != null) {
            notifications.setPendingContributions(patch.pendingContributions());
        }
        if (patch.weeklyReports() != null) notifications.setWeeklyReports(patch.weeklyReports());
        return saveAndAudit(config, "notifications", actor, before);
    }

    public Page<ConfigurationHistory> getHistory(int page, int size) {
        return historyRepository.findAllByOrderByChangedAtDesc(PageRequest.of(page, size));
    }

    private SystemConfig saveAndAudit(SystemConfig config, String section, String actor, SystemConfig before) {
        config.setUpdatedAt(Instant.now());
        config.setUpdatedBy(actor);
        SystemConfig saved = configRepository.save(config);
        recordHistory("UPDATE", section, actor, before, copy(saved));
        return saved;
    }

    private void recordHistory(String action, String section, String actor, SystemConfig before, SystemConfig after) {
        historyRepository.save(new ConfigurationHistory(null, action, section, actor, Instant.now(), before, after));
    }

    private SystemConfig copy(SystemConfig config) {
        return new SystemConfig(
                config.getId(),
                config.getVersion(),
                new SystemConfig.GeneralConfig(
                        config.getGeneral().getCooperativeName(),
                        config.getGeneral().getRnc(),
                        config.getGeneral().getPhone(),
                        config.getGeneral().getAddress(),
                        config.getGeneral().getMandatoryMonthlyContribution(),
                        config.getGeneral().getCurrency()),
                new SystemConfig.LoanPoliciesConfig(
                        config.getLoanPolicies().getDefaultLateInterestRate(),
                        config.getLoanPolicies().getGraceDays(),
                        config.getLoanPolicies().getMaxPersonalLoanAmount(),
                        config.getLoanPolicies().getMaxMicroCreditAmount()),
                new SystemConfig.NotificationsConfig(
                        config.getNotifications().getOverduePayments(),
                        config.getNotifications().getNewRequests(),
                        config.getNotifications().getPendingContributions(),
                        config.getNotifications().getWeeklyReports()),
                config.getCreatedAt(),
                config.getUpdatedAt(),
                config.getUpdatedBy());
    }

    private boolean allNull(Object... values) {
        return java.util.Arrays.stream(values).allMatch(Objects::isNull);
    }
}
