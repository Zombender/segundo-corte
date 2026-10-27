package com.jerr.tecnouam.dto;

import com.jerr.tecnouam.entity.Equipo;
import com.jerr.tecnouam.entity.EstadoPrestamo;
import com.jerr.tecnouam.entity.Prestamo;

import java.time.LocalDate;

// Se construye dentro de la transaccion para no serializar el proxy LAZY de JPA.
public record PrestamoRespuesta(Long id, Equipo equipo, String nombreSolicitante,
                                LocalDate fechaPrestamo, LocalDate fechaPrevistaDevolucion,
                                EstadoPrestamo estado) {
    public static PrestamoRespuesta desde(Prestamo prestamo) {
        Equipo equipo = prestamo.getEquipo();
        Equipo copia = Equipo.builder().id(equipo.getId())
                .codigoPatrimonial(equipo.getCodigoPatrimonial()).nombre(equipo.getNombre())
                .tipo(equipo.getTipo()).estado(equipo.getEstado()).build();
        return new PrestamoRespuesta(prestamo.getId(), copia, prestamo.getNombreSolicitante(),
                prestamo.getFechaPrestamo(), prestamo.getFechaPrevistaDevolucion(), prestamo.getEstado());
    }
}
