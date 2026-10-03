package com.cesde.nexou.repository;
import com.cesde.nexou.model.entity.EquipoTecnologico;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface EquipoTecnologicoRepository extends JpaRepository<EquipoTecnologico, Long> {

    // Método personalizado: equipos con unidades disponibles para préstamo
    List<EquipoTecnologico> findByCantidadDisponibleGreaterThan(Integer cantidad);
}
