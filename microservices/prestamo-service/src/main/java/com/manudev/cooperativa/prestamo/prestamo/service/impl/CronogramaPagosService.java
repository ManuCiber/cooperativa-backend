package com.manudev.cooperativa.prestamo.prestamo.service.impl;

import com.manudev.cooperativa.prestamo.PagoPrestamos.entities.EstadoPagosPrestamo;
import com.manudev.cooperativa.prestamo.PagoPrestamos.entities.PagoPrestamos;
import com.manudev.cooperativa.prestamo.PagoPrestamos.repositories.PagoPrestamosRepository;
import com.manudev.cooperativa.prestamo.prestamo.entities.Prestamos;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CronogramaPagosService {
    private static final int ESCALA_MONETARIA = 2;
    private static final RoundingMode REDONDEO = RoundingMode.HALF_UP;
    private static final MathContext PRECISION = new MathContext(24, REDONDEO);
    private static final BigDecimal DOCE = BigDecimal.valueOf(12);
    private static final BigDecimal CIEN = BigDecimal.valueOf(100);

    private final PagoPrestamosRepository pagosRepository;

    public List<PagoPrestamos> generar(Prestamos prestamo) {
        if (prestamo.getMonto_aprobado() == null || prestamo.getFecha_desembolso() == null) {
            throw new IllegalStateException("El préstamo debe estar aprobado y desembolsado para generar cuotas");
        }

        BigDecimal capitalPendiente = prestamo.getMonto_aprobado().setScale(ESCALA_MONETARIA, REDONDEO);
        BigDecimal tasaMensual = prestamo.getTasa_interes().divide(CIEN, PRECISION).divide(DOCE, PRECISION);
        BigDecimal cuotaFija = calcularCuota(capitalPendiente, tasaMensual, prestamo.getPlazo_meses());
        LocalDate desembolso = prestamo.getFecha_desembolso().toLocalDate();
        int diaAncla = desembolso.getDayOfMonth();
        YearMonth mesAncla = YearMonth.from(desembolso);
        List<PagoPrestamos> cuotas = new ArrayList<>(prestamo.getPlazo_meses());

        for (int numeroCuota = 1; numeroCuota <= prestamo.getPlazo_meses(); numeroCuota++) {
            BigDecimal interes = capitalPendiente.multiply(tasaMensual, PRECISION)
                    .setScale(ESCALA_MONETARIA, REDONDEO);
            boolean ultimaCuota = numeroCuota == prestamo.getPlazo_meses();
            BigDecimal capital = ultimaCuota
                    ? capitalPendiente
                    : cuotaFija.subtract(interes).setScale(ESCALA_MONETARIA, REDONDEO);

            if (capital.signum() <= 0 || (!ultimaCuota && capital.compareTo(capitalPendiente) >= 0)) {
                throw new IllegalArgumentException(
                        "El capital y la tasa no permiten amortizar el préstamo con cuotas de dos decimales");
            }

            BigDecimal montoCuota = capital.add(interes).setScale(ESCALA_MONETARIA, REDONDEO);
            LocalDate vencimiento = fechaVencimiento(mesAncla, diaAncla, numeroCuota);
            cuotas.add(PagoPrestamos.builder()
                    .prestamo_id(prestamo.getPrestamo_id())
                    .numero_cuota(numeroCuota)
                    .monto_cuota(montoCuota)
                    .monto_capital(capital)
                    .monto_interes(interes)
                    .fecha_vencimiento(vencimiento)
                    .estado(EstadoPagosPrestamo.PENDIENTE)
                    .build());
            capitalPendiente = capitalPendiente.subtract(capital).setScale(ESCALA_MONETARIA, REDONDEO);
        }

        return pagosRepository.saveAll(cuotas);
    }

    private BigDecimal calcularCuota(BigDecimal capital, BigDecimal tasaMensual, int plazo) {
        if (tasaMensual.signum() == 0) {
            return capital.divide(BigDecimal.valueOf(plazo), ESCALA_MONETARIA, REDONDEO);
        }

        BigDecimal factor = BigDecimal.ONE.add(tasaMensual).pow(plazo, PRECISION);
        BigDecimal denominador = BigDecimal.ONE.subtract(BigDecimal.ONE.divide(factor, PRECISION));
        return capital.multiply(tasaMensual, PRECISION)
                .divide(denominador, PRECISION)
                .setScale(ESCALA_MONETARIA, REDONDEO);
    }

    private LocalDate fechaVencimiento(YearMonth mesDesembolso, int diaAncla, int numeroCuota) {
        YearMonth mesVencimiento = mesDesembolso.plusMonths(numeroCuota);
        return mesVencimiento.atDay(Math.min(diaAncla, mesVencimiento.lengthOfMonth()));
    }
}
