package com.jerr.tecnouam.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "prestamos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Prestamo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "El equipo es obligatorio")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "equipo_id", nullable = false)
    private Equipo equipo;

    @NotBlank(message = "El nombre del solicitante es obligatorio")
    @Column(name = "nombre_solicitante", nullable = false, length = 150)
    private String nombreSolicitante;

    @NotNull(message = "La fecha de préstamo es obligatoria")
    @Column(name = "fecha_prestamo", nullable = false)
    private LocalDate fechaPrestamo;

    @NotNull(message = "La fecha prevista de devolución es obligatoria")
    @Column(name = "fecha_prevista_devolucion", nullable = false)
    private LocalDate fechaPrevistaDevolucion;

    @NotNull(message = "El estado del préstamo es obligatorio")
    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    private EstadoPrestamo estado;
}
