package com.cesde.nexou.repository;
import com.cesde.nexou.model.entity.Libro;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LibroRepository extends JpaRepository<Libro, Long> {
}
