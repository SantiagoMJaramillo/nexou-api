package com.cesde.nexou.repository;
import com.cesde.nexou.model.entity.Libro;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface LibroRepository extends JpaRepository<Libro, Long> {
    Optional<Libro> findByIdAndCantidadDisponibleGreaterThan(Long id, Integer cantidadDisponible);
}
