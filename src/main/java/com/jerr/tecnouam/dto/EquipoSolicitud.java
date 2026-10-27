package com.jerr.tecnouam.dto;

import com.jerr.tecnouam.entity.EstadoEquipo;
import com.jerr.tecnouam.entity.TipoEquipo;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record EquipoSolicitud(
        @NotBlank @Size(max = 50) String codigoPatrimonial,
        @NotBlank @Size(max = 100) String nombre,
        @NotNull TipoEquipo tipo,
        @NotNull EstadoEquipo estado
) {
}
