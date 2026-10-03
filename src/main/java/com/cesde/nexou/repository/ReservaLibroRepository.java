package com.cesde.nexou.repository;

import com.cesde.nexou.model.entity.ReservaLibro;
import com.cesde.nexou.model.enums.EstadoReserva;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReservaLibroRepository extends JpaRepository<ReservaLibro, Long> {
    long countByUsuarioIdAndEstadoReserva(Long usuarioId, EstadoReserva estadoReserva);

    boolean existsByUsuarioId(Long usuarioId);

    boolean existsByLibroId(Long libroId);

    // Método personalizado: historial de reservas de libros de un usuario
    List<ReservaLibro> findByUsuarioId(Long usuarioId);
}
