package com.jerr.tecnouam.repository;

import com.jerr.tecnouam.entity.EstadoPrestamo;
import com.jerr.tecnouam.entity.Prestamo;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PrestamoRepository extends JpaRepository<Prestamo, Long> {
    @Override
    @EntityGraph(attributePaths = "equipo")
    List<Prestamo> findAll();

    boolean existsByEquipoIdAndEstado(Long equipoId, EstadoPrestamo estado);

    @Query("select p.equipo.id from Prestamo p where p.id = :id")
    Optional<Long> findEquipoIdById(@Param("id") Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Prestamo p where p.id = :id")
    Optional<Prestamo> findByIdForUpdate(@Param("id") Long id);

    @Modifying
    @Query("delete from Prestamo p where p.equipo.id = :equipoId and p.estado = :estado")
    void deleteByEquipoAndEstado(@Param("equipoId") Long equipoId,
                                @Param("estado") EstadoPrestamo estado);
}
