package com.jerr.tecnouam.controller;

import com.jerr.tecnouam.dto.PrestamoRespuesta;
import com.jerr.tecnouam.dto.PrestamoSolicitud;
import com.jerr.tecnouam.service.PrestamoService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/prestamos")
public class PrestamoController {
    private final PrestamoService servicio;

    public PrestamoController(PrestamoService servicio) {
        this.servicio = servicio;
    }

    @GetMapping
    public List<PrestamoRespuesta> listar() {
        return servicio.listar();
    }

    @GetMapping("/{id}")
    public PrestamoRespuesta buscar(@PathVariable Long id) {
        return servicio.buscar(id);
    }

    @PostMapping
    public ResponseEntity<PrestamoRespuesta> registrar(@Valid @RequestBody PrestamoSolicitud solicitud) {
        PrestamoRespuesta prestamo = servicio.registrar(solicitud);
        URI ubicacion = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                .buildAndExpand(prestamo.id()).toUri();
        return ResponseEntity.created(ubicacion).body(prestamo);
    }

    @PutMapping("/{id}/devolver")
    public PrestamoRespuesta devolver(@PathVariable Long id) {
        return servicio.devolver(id);
    }
}
