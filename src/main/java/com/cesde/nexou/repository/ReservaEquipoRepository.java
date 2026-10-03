package com.cesde.nexou.repository;

import com.cesde.nexou.model.entity.ReservaEquipo;
import com.cesde.nexou.model.enums.EstadoReserva;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReservaEquipoRepository extends JpaRepository<ReservaEquipo, Long> {
    long countByUsuarioIdAndEstadoReserva(Long usuarioId, EstadoReserva estadoReserva);

    boolean existsByUsuarioId(Long usuarioId);
}
