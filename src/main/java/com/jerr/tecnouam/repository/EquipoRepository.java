package com.jerr.tecnouam.repository;

import com.jerr.tecnouam.entity.Equipo;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface EquipoRepository extends JpaRepository<Equipo, Long> {
    boolean existsByCodigoPatrimonial(String codigoPatrimonial);

    boolean existsByCodigoPatrimonialAndIdNot(String codigoPatrimonial, Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from Equipo e where e.id = :id")
    Optional<Equipo> findByIdForUpdate(@Param("id") Long id);
}
