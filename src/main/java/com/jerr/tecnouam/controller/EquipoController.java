package com.jerr.tecnouam.controller;

import com.jerr.tecnouam.dto.EquipoSolicitud;
import com.jerr.tecnouam.entity.Equipo;
import com.jerr.tecnouam.service.EquipoService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/equipos")
public class EquipoController {
    private final EquipoService servicio;

    public EquipoController(EquipoService servicio) {
        this.servicio = servicio;
    }

    @GetMapping
    public List<Equipo> listar() {
        return servicio.listar();
    }

    @GetMapping("/{id}")
    public Equipo buscar(@PathVariable Long id) {
        return servicio.buscar(id);
    }

    @PostMapping
    public ResponseEntity<Equipo> crear(@Valid @RequestBody EquipoSolicitud solicitud) {
        Equipo equipo = servicio.crear(solicitud);
        URI ubicacion = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                .buildAndExpand(equipo.getId()).toUri();
        return ResponseEntity.created(ubicacion).body(equipo);
    }

    @PutMapping("/{id}")
    public Equipo actualizar(@PathVariable Long id, @Valid @RequestBody EquipoSolicitud solicitud) {
        return servicio.actualizar(id, solicitud);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        servicio.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
