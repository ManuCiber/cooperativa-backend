package com.manudev.cooperativa.prestamo.PagoPrestamos.services.impl;

import com.manudev.cooperativa.prestamo.PagoPrestamos.services.PagoPrestamosService;
import org.junit.jupiter.api.Test;
import org.springframework.scheduling.annotation.Scheduled;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class RevisionMoraSchedulerTest {
    @Test
    void runsTheMoraReviewDailyAtFivePastMidnight() throws NoSuchMethodException {
        PagoPrestamosService service = mock(PagoPrestamosService.class);
        RevisionMoraScheduler scheduler = new RevisionMoraScheduler(service);

        scheduler.revisarMoraDiariamente();

        verify(service).revisarEstadosMora();
        Scheduled schedule = RevisionMoraScheduler.class
                .getMethod("revisarMoraDiariamente")
                .getAnnotation(Scheduled.class);
        assertEquals("0 5 0 * * *", schedule.cron());
    }
}
