package com.cesde.nexou.config;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import com.cesde.nexou.model.entity.Usuario;
import com.cesde.nexou.model.enums.Rol;
import com.cesde.nexou.repository.UsuarioRepository;

/**
 * Deja la base de datos lista para la seguridad en cada arranque (es idempotente):
 * 1. El correo pasa a ser opcional (el registro solo pide usuario y contraseña).
 * 2. Los usuarios antiguos sin rol quedan como USER y sus contraseñas en texto plano se encriptan.
 * 3. Si no existe ningún administrador, se crea uno con ADMIN_USER / ADMIN_PASSWORD.
 */
@Configuration
public class InicializadorSeguridad {

    private static final Logger log = LoggerFactory.getLogger(InicializadorSeguridad.class);

    @Bean
    @Order(2)
    CommandLineRunner prepararSeguridad(UsuarioRepository usuarioRepository,
                                        PasswordEncoder passwordEncoder,
                                        JdbcTemplate jdbc,
                                        @Value("${app.admin.usuario:admin}") String adminUsuario,
                                        @Value("${app.admin.contrasena:}") String adminContrasena) {
        return args -> {
            try {
                jdbc.execute("ALTER TABLE usuarios ALTER COLUMN correo DROP NOT NULL");
            } catch (Exception e) {
                log.debug("No se pudo relajar la columna correo (puede que ya sea opcional): {}", e.getMessage());
            }
            migrarUsuarios(usuarioRepository, passwordEncoder);
            crearAdministrador(usuarioRepository, passwordEncoder, adminUsuario, adminContrasena);
        };
    }

    @Transactional
    void migrarUsuarios(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        List<Usuario> usuarios = usuarioRepository.findAll();
        for (Usuario u : usuarios) {
            boolean cambio = false;
            if (u.getRol() == null) {
                u.setRol(Rol.USER);
                cambio = true;
            }
            if (u.getContrasena() != null && !u.getContrasena().startsWith("$2")) {
                u.setContrasena(passwordEncoder.encode(u.getContrasena()));
                cambio = true;
            }
            if (cambio) {
                usuarioRepository.save(u);
            }
        }
    }

    private void crearAdministrador(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder,
                                    String adminUsuario, String adminContrasena) {
        if (usuarioRepository.existsByRol(Rol.ADMIN)) {
            return;
        }
        if (adminContrasena == null || adminContrasena.isBlank()) {
            adminContrasena = "admin1234";
            log.warn("ADMIN_PASSWORD no está definida: el administrador se crea con la contraseña por defecto. Cámbiala en producción.");
        }
        // Si ya hay un usuario con ese nombre, se le asciende a administrador en vez de duplicarlo
        Usuario admin = usuarioRepository.findByNombreUsuarioIgnoreCase(adminUsuario).orElseGet(Usuario::new);
        admin.setNombreUsuario(adminUsuario);
        admin.setContrasena(passwordEncoder.encode(adminContrasena));
        admin.setRol(Rol.ADMIN);
        usuarioRepository.save(admin);
        log.info(">>> Administrador '{}' creado.", adminUsuario);
    }
}
