package com.cesde.nexou.repository;
import com.cesde.nexou.model.entity.ConfiguracionUsuario;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ConfiguracionUsuarioRepository extends JpaRepository<ConfiguracionUsuario, Long> {

    // Método personalizado: obtener la configuración de un usuario
    Optional<ConfiguracionUsuario> findByUsuarioId(Long usuarioId);

    boolean existsByUsuarioId(Long usuarioId);
}
