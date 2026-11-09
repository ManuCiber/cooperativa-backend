package com.manudev.cooperativa.prestamo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
public class TimeConfiguration {
    @Bean
    Clock applicationClock() {
        return Clock.systemDefaultZone();
    }
}
