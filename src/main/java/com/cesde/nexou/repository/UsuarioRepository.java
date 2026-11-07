package com.cesde.nexou.repository;
import com.cesde.nexou.model.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByIdAndEstadoActivoTrue(Long id);

    // Método personalizado: buscar un usuario por su correo institucional (sin distinguir mayúsculas)
    Optional<Usuario> findByCorreoIgnoreCase(String correo);

    // Login y registro: el nombre de usuario es único sin distinguir mayúsculas
    Optional<Usuario> findByNombreUsuarioIgnoreCase(String nombreUsuario);

    boolean existsByNombreUsuarioIgnoreCase(String nombreUsuario);

    boolean existsByRol(com.cesde.nexou.model.enums.Rol rol);
}
