package com.cesde.nexou.repository;
import com.cesde.nexou.model.entity.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface CategoriaRepository extends JpaRepository<Categoria, Long> {

    // Método personalizado: buscar una categoría por nombre sin importar mayúsculas
    Optional<Categoria> findByNombreIgnoreCase(String nombre);
}
