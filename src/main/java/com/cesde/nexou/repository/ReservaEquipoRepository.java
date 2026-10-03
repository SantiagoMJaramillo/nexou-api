package com.cesde.nexou.repository;

import com.cesde.nexou.model.entity.ReservaEquipo;
import com.cesde.nexou.model.enums.EstadoReserva;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReservaEquipoRepository extends JpaRepository<ReservaEquipo, Long> {
    long countByUsuarioIdAndEstadoReserva(Long usuarioId, EstadoReserva estadoReserva);

    boolean existsByUsuarioId(Long usuarioId);

    boolean existsByEquipoId(Long equipoId);

    // Método personalizado: historial de reservas de equipos de un usuario
    List<ReservaEquipo> findByUsuarioId(Long usuarioId);
}
