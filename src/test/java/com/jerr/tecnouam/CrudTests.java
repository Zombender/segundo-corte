package com.jerr.tecnouam;

import com.jerr.tecnouam.controller.ApiExceptionHandler;
import com.jerr.tecnouam.controller.EquipoController;
import com.jerr.tecnouam.controller.PrestamoController;
import com.jerr.tecnouam.entity.*;
import com.jerr.tecnouam.repository.EquipoRepository;
import com.jerr.tecnouam.repository.PrestamoRepository;
import com.jerr.tecnouam.service.EquipoService;
import com.jerr.tecnouam.service.PrestamoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// Verifica el contrato HTTP y las reglas del rol 2 sin arrancar seguridad ni usar la BD del equipo.
class CrudTests {
    private EquipoRepository equipos;
    private PrestamoRepository prestamos;
    private MockMvc mvc;
    private Equipo equipo;
    private Prestamo prestamo;

    private static final String EQUIPO = """
            {"codigoPatrimonial":"UAM-001","nombre":"Laptop","tipo":"LAPTOP","estado":"DISPONIBLE"}
            """;
    private static final String PRESTAMO = """
            {"equipoId":1,"nombreSolicitante":"Ana","fechaPrestamo":"2026-10-26",
             "fechaPrevistaDevolucion":"2026-10-28"}
            """;

    @BeforeEach
    void preparar() {
        equipos = mock(EquipoRepository.class);
        prestamos = mock(PrestamoRepository.class);
        equipo = Equipo.builder().id(1L).codigoPatrimonial("UAM-001").nombre("Laptop")
                .tipo(TipoEquipo.LAPTOP).estado(EstadoEquipo.DISPONIBLE).build();
        prestamo = Prestamo.builder().id(2L).equipo(equipo).nombreSolicitante("Ana")
                .fechaPrestamo(LocalDate.of(2026, 10, 26))
                .fechaPrevistaDevolucion(LocalDate.of(2026, 10, 28)).estado(EstadoPrestamo.ACTIVO).build();
        when(equipos.findById(1L)).thenReturn(Optional.of(equipo));
        when(equipos.findByIdForUpdate(1L)).thenReturn(Optional.of(equipo));
        when(equipos.saveAndFlush(any())).thenAnswer(invocacion -> {
            Equipo guardado = invocacion.getArgument(0);
            guardado.setId(1L);
            return guardado;
        });
        when(prestamos.saveAndFlush(any())).thenAnswer(invocacion -> {
            Prestamo guardado = invocacion.getArgument(0);
            guardado.setId(2L);
            return guardado;
        });
        when(prestamos.findEquipoIdById(2L)).thenReturn(Optional.of(1L));
        when(prestamos.findByIdForUpdate(2L)).thenReturn(Optional.of(prestamo));
        when(prestamos.findById(2L)).thenReturn(Optional.of(prestamo));
        mvc = MockMvcBuilders.standaloneSetup(
                new EquipoController(new EquipoService(equipos, prestamos)),
                new PrestamoController(new PrestamoService(prestamos, equipos)))
                .setControllerAdvice(new ApiExceptionHandler()).build();
    }

    @Test
    void crearEquipoDevuelve201YUbicacion() throws Exception {
        mvc.perform(post("/api/equipos").contentType(MediaType.APPLICATION_JSON).content(EQUIPO))
                .andExpect(status().isCreated()).andExpect(header().string("Location", "http://localhost/api/equipos/1"))
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void consultarListasYRecursosDevuelve200() throws Exception {
        when(equipos.findAll()).thenReturn(List.of(equipo));
        when(prestamos.findAll()).thenReturn(List.of(prestamo));
        mvc.perform(get("/api/equipos")).andExpect(status().isOk()).andExpect(jsonPath("$[0].id").value(1));
        mvc.perform(get("/api/equipos/1")).andExpect(status().isOk());
        mvc.perform(get("/api/prestamos")).andExpect(status().isOk()).andExpect(jsonPath("$[0].equipo.id").value(1));
        mvc.perform(get("/api/prestamos/2")).andExpect(status().isOk());
    }

    @Test
    void actualizarEquipoDevuelve200() throws Exception {
        mvc.perform(put("/api/equipos/1").contentType(MediaType.APPLICATION_JSON)
                        .content(EQUIPO.replace("Laptop", "Laptop nueva")))
                .andExpect(status().isOk()).andExpect(jsonPath("$.nombre").value("Laptop nueva"));
    }

    @Test
    void codigoDuplicadoDevuelve409AlCrearYActualizar() throws Exception {
        when(equipos.existsByCodigoPatrimonial("UAM-001")).thenReturn(true);
        when(equipos.existsByCodigoPatrimonialAndIdNot("UAM-001", 1L)).thenReturn(true);
        mvc.perform(post("/api/equipos").contentType(MediaType.APPLICATION_JSON).content(EQUIPO))
                .andExpect(status().isConflict());
        mvc.perform(put("/api/equipos/1").contentType(MediaType.APPLICATION_JSON).content(EQUIPO))
                .andExpect(status().isConflict());
        verify(equipos, never()).saveAndFlush(any());
    }

    @Test
    void datosObligatoriosLongitudesYEnumsInvalidosDevuelven400() throws Exception {
        for (String datos : List.of("{}", EQUIPO.replace("UAM-001", " "),
                EQUIPO.replace("Laptop", "a".repeat(101)), EQUIPO.replace("LAPTOP", "TELEVISOR"))) {
            mvc.perform(post("/api/equipos").contentType(MediaType.APPLICATION_JSON).content(datos))
                    .andExpect(status().isBadRequest());
        }
        verify(equipos, never()).saveAndFlush(any());
    }

    @Test
    void recursosInexistentesDevuelven404() throws Exception {
        mvc.perform(get("/api/equipos/99")).andExpect(status().isNotFound());
        mvc.perform(get("/api/prestamos/99")).andExpect(status().isNotFound());
        mvc.perform(delete("/api/equipos/99")).andExpect(status().isNotFound());
        mvc.perform(put("/api/equipos/99").contentType(MediaType.APPLICATION_JSON).content(EQUIPO))
                .andExpect(status().isNotFound());
        mvc.perform(put("/api/prestamos/99/devolver")).andExpect(status().isNotFound());
        mvc.perform(post("/api/prestamos").contentType(MediaType.APPLICATION_JSON)
                .content(PRESTAMO.replace("\"equipoId\":1", "\"equipoId\":99"))).andExpect(status().isNotFound());
    }

    @Test
    void prestarDevuelve201YCambiaEstado() throws Exception {
        mvc.perform(post("/api/prestamos").contentType(MediaType.APPLICATION_JSON).content(PRESTAMO))
                .andExpect(status().isCreated()).andExpect(header().string("Location", "http://localhost/api/prestamos/2"))
                .andExpect(jsonPath("$.estado").value("ACTIVO"))
                .andExpect(jsonPath("$.equipo.estado").value("PRESTADO"));
        assertEquals(EstadoEquipo.PRESTADO, equipo.getEstado());
    }

    @Test
    void segundoPrestamoDevuelve409() throws Exception {
        mvc.perform(post("/api/prestamos").contentType(MediaType.APPLICATION_JSON).content(PRESTAMO))
                .andExpect(status().isCreated());
        mvc.perform(post("/api/prestamos").contentType(MediaType.APPLICATION_JSON).content(PRESTAMO))
                .andExpect(status().isConflict());
        verify(prestamos, times(1)).saveAndFlush(any());
    }

    @Test
    void prestamoActivoBloqueaAunqueEstadoEquipoSeaInconsistente() throws Exception {
        when(prestamos.existsByEquipoIdAndEstado(1L, EstadoPrestamo.ACTIVO)).thenReturn(true);
        mvc.perform(post("/api/prestamos").contentType(MediaType.APPLICATION_JSON).content(PRESTAMO))
                .andExpect(status().isConflict());
    }

    @Test
    void fechasInvalidasYCamposAusentesDevuelven400() throws Exception {
        for (String datos : List.of("{}", PRESTAMO.replace("2026-10-28", "2026-10-25"),
                PRESTAMO.replace("2026-10-28", "2026-02-30"), PRESTAMO.replace("Ana", " "),
                PRESTAMO.replace("\"equipoId\":1", "\"equipoId\":0"))) {
            mvc.perform(post("/api/prestamos").contentType(MediaType.APPLICATION_JSON).content(datos))
                    .andExpect(status().isBadRequest());
        }
        verify(prestamos, never()).saveAndFlush(any());
    }

    @Test
    void devolverLiberaEquipoYPermiteNuevoPrestamo() throws Exception {
        equipo.setEstado(EstadoEquipo.PRESTADO);
        mvc.perform(put("/api/prestamos/2/devolver")).andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("DEVUELTO"))
                .andExpect(jsonPath("$.equipo.estado").value("DISPONIBLE"));
        assertEquals(EstadoEquipo.DISPONIBLE, equipo.getEstado());
        assertEquals(EstadoPrestamo.DEVUELTO, prestamo.getEstado());
        mvc.perform(post("/api/prestamos").contentType(MediaType.APPLICATION_JSON).content(PRESTAMO))
                .andExpect(status().isCreated());
    }

    @Test
    void dobleDevolucionDevuelve409() throws Exception {
        prestamo.setEstado(EstadoPrestamo.DEVUELTO);
        mvc.perform(put("/api/prestamos/2/devolver")).andExpect(status().isConflict());
        verify(prestamos, never()).flush();
    }

    @Test
    void eliminarConPrestamoActivoDevuelve409() throws Exception {
        when(prestamos.existsByEquipoIdAndEstado(1L, EstadoPrestamo.ACTIVO)).thenReturn(true);
        mvc.perform(delete("/api/equipos/1")).andExpect(status().isConflict());
        verify(equipos, never()).delete(any());
        verify(prestamos, never()).deleteByEquipoAndEstado(anyLong(), any());
    }

    @Test
    void eliminarSinActivosDevuelve204YLimpiaSoloPrestamosDevueltos() throws Exception {
        mvc.perform(delete("/api/equipos/1")).andExpect(status().isNoContent()).andExpect(content().string(""));
        var orden = inOrder(prestamos, equipos);
        orden.verify(prestamos).deleteByEquipoAndEstado(1L, EstadoPrestamo.DEVUELTO);
        orden.verify(equipos).delete(equipo);
    }

    @Test
    void noPermiteManipularEstadoParaSaltarFlujoDePrestamos() throws Exception {
        mvc.perform(post("/api/equipos").contentType(MediaType.APPLICATION_JSON)
                .content(EQUIPO.replace("DISPONIBLE", "PRESTADO"))).andExpect(status().isConflict());
        when(prestamos.existsByEquipoIdAndEstado(1L, EstadoPrestamo.ACTIVO)).thenReturn(true);
        mvc.perform(put("/api/equipos/1").contentType(MediaType.APPLICATION_JSON).content(EQUIPO))
                .andExpect(status().isConflict());
    }

    @Test
    void conflictoDeIntegridadDevuelve409() throws Exception {
        doThrow(new DataIntegrityViolationException("codigo duplicado")).when(equipos).saveAndFlush(any());
        mvc.perform(post("/api/equipos").contentType(MediaType.APPLICATION_JSON).content(EQUIPO))
                .andExpect(status().isConflict());
    }
}
