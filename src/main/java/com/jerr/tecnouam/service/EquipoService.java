package com.jerr.tecnouam.service;

import com.jerr.tecnouam.dto.EquipoSolicitud;
import com.jerr.tecnouam.entity.Equipo;
import com.jerr.tecnouam.entity.EstadoEquipo;
import com.jerr.tecnouam.entity.EstadoPrestamo;
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
public class EquipoService {
    private final EquipoRepository equipos;
    private final PrestamoRepository prestamos;

    public EquipoService(EquipoRepository equipos, PrestamoRepository prestamos) {
        this.equipos = equipos;
        this.prestamos = prestamos;
    }

    public List<Equipo> listar() {
        return equipos.findAll();
    }

    public Equipo buscar(Long id) {
        return equipos.findById(id).orElseThrow(() -> noEncontrado(id));
    }

    @Transactional
    public Equipo crear(@NotNull @Valid EquipoSolicitud solicitud) {
        if (solicitud.estado() != EstadoEquipo.DISPONIBLE) {
            throw conflicto("Un equipo nuevo debe estar DISPONIBLE; registre un prestamo para prestarlo");
        }
        String codigo = solicitud.codigoPatrimonial().strip();
        if (equipos.existsByCodigoPatrimonial(codigo)) {
            throw conflicto("Ya existe un equipo con ese codigo patrimonial");
        }
        Equipo equipo = new Equipo();
        aplicar(equipo, solicitud, codigo);
        return equipos.saveAndFlush(equipo);
    }

    @Transactional
    public Equipo actualizar(Long id, @NotNull @Valid EquipoSolicitud solicitud) {
        Equipo equipo = equipos.findByIdForUpdate(id).orElseThrow(() -> noEncontrado(id));
        String codigo = solicitud.codigoPatrimonial().strip();
        if (equipos.existsByCodigoPatrimonialAndIdNot(codigo, id)) {
            throw conflicto("Ya existe un equipo con ese codigo patrimonial");
        }
        EstadoEquipo estadoReal = prestamos.existsByEquipoIdAndEstado(id, EstadoPrestamo.ACTIVO)
                ? EstadoEquipo.PRESTADO : EstadoEquipo.DISPONIBLE;
        if (solicitud.estado() != estadoReal) {
            throw conflicto("El estado debe corresponder a los prestamos; utilice registrar prestamo o devolver");
        }
        aplicar(equipo, solicitud, codigo);
        return equipos.saveAndFlush(equipo);
    }

    @Transactional
    public void eliminar(Long id) {
        Equipo equipo = equipos.findByIdForUpdate(id).orElseThrow(() -> noEncontrado(id));
        if (prestamos.existsByEquipoIdAndEstado(id, EstadoPrestamo.ACTIVO)
                || equipo.getEstado() == EstadoEquipo.PRESTADO) {
            throw conflicto("No se puede eliminar un equipo con prestamos activos");
        }
        // La relacion existente es obligatoria: al eliminar fisicamente el equipo
        // tambien se eliminan sus prestamos devueltos para respetar la clave foranea.
        prestamos.deleteByEquipoAndEstado(id, EstadoPrestamo.DEVUELTO);
        equipos.delete(equipo);
        equipos.flush();
    }

    private void aplicar(Equipo equipo, EquipoSolicitud solicitud, String codigo) {
        equipo.setCodigoPatrimonial(codigo);
        equipo.setNombre(solicitud.nombre().strip());
        equipo.setTipo(solicitud.tipo());
        equipo.setEstado(solicitud.estado());
    }

    private ResponseStatusException noEncontrado(Long id) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, "Equipo no encontrado: " + id);
    }

    private ResponseStatusException conflicto(String mensaje) {
        return new ResponseStatusException(HttpStatus.CONFLICT, mensaje);
    }
}
