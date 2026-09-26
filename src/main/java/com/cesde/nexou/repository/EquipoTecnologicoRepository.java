package com.cesde.nexou.repository;
import com.cesde.nexou.model.entity.EquipoTecnologico;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface EquipoTecnologicoRepository extends JpaRepository<EquipoTecnologico, Long> {
    Optional<EquipoTecnologico> findByIdAndCantidadDisponibleGreaterThan(Long id, Integer cantidadDisponible);
}