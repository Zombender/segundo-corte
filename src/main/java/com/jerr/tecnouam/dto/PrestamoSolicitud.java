package com.jerr.tecnouam.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record PrestamoSolicitud(
        @NotNull @Positive Long equipoId,
        @NotBlank @Size(max = 150) String nombreSolicitante,
        @NotNull LocalDate fechaPrestamo,
        @NotNull LocalDate fechaPrevistaDevolucion
) {
    @AssertTrue(message = "La fecha prevista de devolucion no puede ser anterior a la fecha del prestamo")
    public boolean isFechasValidas() {
        return fechaPrestamo == null || fechaPrevistaDevolucion == null
                || !fechaPrevistaDevolucion.isBefore(fechaPrestamo);
    }
}
