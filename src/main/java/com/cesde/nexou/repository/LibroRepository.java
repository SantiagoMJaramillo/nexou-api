package com.cesde.nexou.repository;
import com.cesde.nexou.model.entity.Libro;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface LibroRepository extends JpaRepository<Libro, Long> {

    // Método personalizado: buscar un libro por su ISBN
    Optional<Libro> findByIsbn(String isbn);
}
