package com.jerr.tecnouam.service;

import com.jerr.tecnouam.dto.PrestamoRespuesta;
import com.jerr.tecnouam.dto.PrestamoSolicitud;
import com.jerr.tecnouam.entity.Equipo;
import com.jerr.tecnouam.entity.EstadoEquipo;
import com.jerr.tecnouam.entity.EstadoPrestamo;
import com.jerr.tecnouam.entity.Prestamo;
import com.jerr.tecnouam.repository.EquipoRepository;
import com.jerr.tecnouam.repository.PrestamoRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@Validated
@Transactional(readOnly = true)
public class PrestamoService {
    private final PrestamoRepository prestamos;
    private final EquipoRepository equipos;

    public PrestamoService(PrestamoRepository prestamos, EquipoRepository equipos) {
        this.prestamos = prestamos;
        this.equipos = equipos;
    }

    public List<PrestamoRespuesta> listar() {
        return prestamos.findAll().stream().map(PrestamoRespuesta::desde).toList();
    }

    public PrestamoRespuesta buscar(Long id) {
        return PrestamoRespuesta.desde(buscarEntidad(id));
    }

    @Transactional
    public PrestamoRespuesta registrar(@NotNull @Valid PrestamoSolicitud solicitud) {
        Equipo equipo = equipos.findByIdForUpdate(solicitud.equipoId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Equipo no encontrado"));
        if (equipo.getEstado() == EstadoEquipo.PRESTADO
                || prestamos.existsByEquipoIdAndEstado(equipo.getId(), EstadoPrestamo.ACTIVO)) {
            throw conflicto("El equipo ya tiene un prestamo activo");
        }
        Prestamo prestamo = Prestamo.builder().equipo(equipo)
                .nombreSolicitante(solicitud.nombreSolicitante().strip())
                .fechaPrestamo(solicitud.fechaPrestamo())
                .fechaPrevistaDevolucion(solicitud.fechaPrevistaDevolucion())
                .estado(EstadoPrestamo.ACTIVO).build();
        equipo.setEstado(EstadoEquipo.PRESTADO);
        return PrestamoRespuesta.desde(prestamos.saveAndFlush(prestamo));
    }

    @Transactional
    public PrestamoRespuesta devolver(Long id) {
        // Todas las escrituras bloquean primero el equipo para serializar
        // prestamos, devoluciones, actualizaciones y eliminaciones concurrentes.
        Long equipoId = prestamos.findEquipoIdById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Prestamo no encontrado: " + id));
        Equipo equipo = equipos.findByIdForUpdate(equipoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Equipo no encontrado"));
        Prestamo prestamo = prestamos.findByIdForUpdate(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Prestamo no encontrado: " + id));
        if (prestamo.getEstado() != EstadoPrestamo.ACTIVO) {
            throw conflicto("El prestamo ya fue devuelto");
        }
        prestamo.setEstado(EstadoPrestamo.DEVUELTO);
        equipo.setEstado(EstadoEquipo.DISPONIBLE);
        prestamos.flush();
        return PrestamoRespuesta.desde(prestamo);
    }

    private Prestamo buscarEntidad(Long id) {
        return prestamos.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Prestamo no encontrado: " + id));
    }

    private ResponseStatusException conflicto(String mensaje) {
        return new ResponseStatusException(HttpStatus.CONFLICT, mensaje);
    }
}
