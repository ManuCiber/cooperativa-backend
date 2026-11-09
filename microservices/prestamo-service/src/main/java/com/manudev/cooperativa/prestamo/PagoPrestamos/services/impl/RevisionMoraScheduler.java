package com.manudev.cooperativa.prestamo.PagoPrestamos.services.impl;

import com.manudev.cooperativa.prestamo.PagoPrestamos.services.PagoPrestamosService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RevisionMoraScheduler {
    private final PagoPrestamosService pagoPrestamosService;

    @Scheduled(cron = "0 5 0 * * *")
    public void revisarMoraDiariamente() {
        pagoPrestamosService.revisarEstadosMora();
    }
}
