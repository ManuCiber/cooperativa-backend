package com.manudev.cooperativa.prestamo.Garantias.service;

import com.manudev.cooperativa.prestamo.Garantias.dto.GarantiasRequest;
import com.manudev.cooperativa.prestamo.Garantias.entities.EstadoGarantia;
import com.manudev.cooperativa.prestamo.Garantias.entities.Garantias;
import com.manudev.cooperativa.prestamo.Garantias.entities.TipoGarantia;
import com.manudev.cooperativa.prestamo.Garantias.mapper.GarantiasMapper;
import com.manudev.cooperativa.prestamo.Garantias.repository.IGarantiasRepository;
import com.manudev.cooperativa.prestamo.Garantias.service.Impl.GarantiaServiceImpl;
import com.manudev.cooperativa.prestamo.prestamo.repository.PrestamosRepository;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class GarantiaServiceImplTest {
    private static ValidatorFactory validatorFactory;

    private IGarantiasRepository garantiasRepository;
    private PrestamosRepository prestamosRepository;
    private GarantiaServiceImpl service;

    @BeforeAll
    static void initializeValidator() {
        validatorFactory = Validation.buildDefaultValidatorFactory();
    }

    @AfterAll
    static void closeValidator() {
        validatorFactory.close();
    }

    @BeforeEach
    void setUp() {
        garantiasRepository = mock(IGarantiasRepository.class);
        prestamosRepository = mock(PrestamosRepository.class);
        Validator validator = validatorFactory.getValidator();
        service = new GarantiaServiceImpl(
                garantiasRepository, prestamosRepository, new GarantiasMapper(), validator);
    }

    @Test
    void createsActiveGuaranteeAndReturnsItsState() {
        when(prestamosRepository.existsById(1L)).thenReturn(true);
        when(garantiasRepository.saveAndFlush(any(Garantias.class))).thenAnswer(invocation -> {
            Garantias saved = invocation.getArgument(0);
            saved.setGarantia_id(10L);
            saved.prePersist();
            return saved;
        });

        var response = service.crear(request());

        assertEquals(10L, response.getGarantia_id());
        assertEquals(EstadoGarantia.ACTIVO, response.getEstado());
        assertEquals(TipoGarantia.APORTACIONES, response.getTipo_garantia());
    }

    @Test
    void rejectsInvalidRequestAndMissingLoanBeforeSaving() {
        assertStatus(HttpStatus.BAD_REQUEST, () -> service.crear(null));
        assertStatus(HttpStatus.BAD_REQUEST, () -> service.crear(GarantiasRequest.builder().build()));

        when(prestamosRepository.existsById(1L)).thenReturn(false);
        assertStatus(HttpStatus.NOT_FOUND, () -> service.crear(request()));

        verifyNoInteractions(garantiasRepository);
    }

    @Test
    void listsOnlyActiveGuarantees() {
        Garantias active = stored(EstadoGarantia.ACTIVO);
        when(garantiasRepository.findByEstado(EstadoGarantia.ACTIVO)).thenReturn(List.of(active));

        var result = service.listarTodas();

        assertEquals(1, result.size());
        assertEquals(EstadoGarantia.ACTIVO, result.get(0).getEstado());
        verify(garantiasRepository).findByEstado(EstadoGarantia.ACTIVO);
        verify(garantiasRepository, never()).findAll();
    }

    @Test
    void listsActiveGuaranteesByLoanWithOptionalTypeFilter() {
        when(prestamosRepository.existsById(1L)).thenReturn(true);
        when(garantiasRepository.findByPrestamoId(1L, EstadoGarantia.ACTIVO))
                .thenReturn(List.of(stored(EstadoGarantia.ACTIVO)));
        when(garantiasRepository.findByPrestamoIdAndTipoGarantia(
                1L, TipoGarantia.APORTACIONES, EstadoGarantia.ACTIVO))
                .thenReturn(List.of(stored(EstadoGarantia.ACTIVO)));

        assertEquals(1, service.listarPorPrestamo(1L, null).size());
        assertEquals(1, service.listarPorPrestamo(1L, TipoGarantia.APORTACIONES).size());
        verify(garantiasRepository).findByPrestamoId(1L, EstadoGarantia.ACTIVO);
        verify(garantiasRepository).findByPrestamoIdAndTipoGarantia(
                1L, TipoGarantia.APORTACIONES, EstadoGarantia.ACTIVO);
    }

    @Test
    void rejectsInvalidLoanIdOrMissingLoanWhenListing() {
        assertStatus(HttpStatus.BAD_REQUEST, () -> service.listarPorPrestamo(0L, null));
        when(prestamosRepository.existsById(99L)).thenReturn(false);
        assertStatus(HttpStatus.NOT_FOUND, () -> service.listarPorPrestamo(99L, null));

        verifyNoInteractions(garantiasRepository);
    }

    @Test
    void getsGuaranteeByIdIncludingInactiveRecords() {
        Garantias inactive = stored(EstadoGarantia.INACTIVO);
        when(garantiasRepository.findById(10L)).thenReturn(Optional.of(inactive));

        var response = service.obtenerPorId(10L);

        assertEquals(EstadoGarantia.INACTIVO, response.getEstado());
        assertEquals("Documentos de respaldo", response.getDocumentos());
    }

    @Test
    void rejectsInvalidAndUnknownGuaranteeIds() {
        assertStatus(HttpStatus.BAD_REQUEST, () -> service.obtenerPorId(0L));
        when(garantiasRepository.findById(10L)).thenReturn(Optional.empty());
        assertStatus(HttpStatus.NOT_FOUND, () -> service.obtenerPorId(10L));
    }

    @Test
    void updatesActiveGuaranteeWithoutChangingItsStateOrCreationTime() {
        Garantias active = stored(EstadoGarantia.ACTIVO);
        LocalDateTime createdAt = active.getCreateAt();
        when(prestamosRepository.existsById(1L)).thenReturn(true);
        when(garantiasRepository.findById(10L)).thenReturn(Optional.of(active));
        when(garantiasRepository.saveAndFlush(any(Garantias.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        GarantiasRequest update = request();
        update.setValor_estimado(new BigDecimal("250.00"));
        update.setDocumentacion("Documentos actualizados");
        var response = service.actualizar(10L, update);

        assertEquals(new BigDecimal("250.00"), response.getValor_estimado());
        assertEquals("Documentos actualizados", response.getDocumentos());
        assertEquals(EstadoGarantia.ACTIVO, response.getEstado());
        assertEquals(createdAt, response.getCreatedAt());
    }

    @Test
    void doesNotUpdateInactiveGuarantee() {
        when(prestamosRepository.existsById(1L)).thenReturn(true);
        when(garantiasRepository.findById(10L)).thenReturn(Optional.of(stored(EstadoGarantia.INACTIVO)));

        assertStatus(HttpStatus.CONFLICT, () -> service.actualizar(10L, request()));

        verify(garantiasRepository, never()).saveAndFlush(any(Garantias.class));
    }

    @Test
    void softDeletesGuaranteeAndRetainsItsData() {
        Garantias active = stored(EstadoGarantia.ACTIVO);
        when(garantiasRepository.findById(10L)).thenReturn(Optional.of(active));
        when(garantiasRepository.saveAndFlush(any(Garantias.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        service.eliminar(10L);

        assertEquals(EstadoGarantia.INACTIVO, active.getEstado());
        assertEquals(1L, active.getPrestamo_id());
        assertEquals(new BigDecimal("125.50"), active.getValor_estimado());
        assertEquals("Documentos de respaldo", active.getDocumentos());
        verify(garantiasRepository).saveAndFlush(active);
        verify(garantiasRepository, never()).delete(any(Garantias.class));
        verify(garantiasRepository, never()).deleteById(any());
    }

    @Test
    void rejectsRepeatedSoftDelete() {
        when(garantiasRepository.findById(10L)).thenReturn(Optional.of(stored(EstadoGarantia.INACTIVO)));

        assertStatus(HttpStatus.CONFLICT, () -> service.eliminar(10L));

        verify(garantiasRepository, never()).saveAndFlush(any(Garantias.class));
        verify(garantiasRepository, never()).delete(any(Garantias.class));
    }

    private GarantiasRequest request() {
        return GarantiasRequest.builder()
                .prestamo_id(1L)
                .valor_estimado(new BigDecimal("125.50"))
                .documentacion("Documentos de respaldo")
                .build();
    }

    private Garantias stored(EstadoGarantia estado) {
        Garantias garantia = Garantias.builder()
                .garantia_id(10L)
                .prestamo_id(1L)
                .tipo_garantia(TipoGarantia.APORTACIONES)
                .valor_estimado(new BigDecimal("125.50"))
                .documentos("Documentos de respaldo")
                .estado(estado)
                .build();
        garantia.prePersist();
        return garantia;
    }

    private void assertStatus(HttpStatus expected, Runnable action) {
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, action::run);
        assertEquals(expected, exception.getStatusCode());
    }
}
