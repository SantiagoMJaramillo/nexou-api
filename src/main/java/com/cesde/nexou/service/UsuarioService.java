package com.cesde.nexou.service;

import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cesde.nexou.exception.RecursoNoEncontradoException;
import com.cesde.nexou.exception.ReglaDeNegocioException;
import com.cesde.nexou.model.entity.Usuario;
import com.cesde.nexou.model.enums.Rol;
import com.cesde.nexou.repository.ReservaEquipoRepository;
import com.cesde.nexou.repository.ReservaLibroRepository;
import com.cesde.nexou.repository.UsuarioRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final ReservaLibroRepository reservaLibroRepository;
    private final ReservaEquipoRepository reservaEquipoRepository;
    private final PasswordEncoder passwordEncoder;

    public List<Usuario> obtenerTodos() {
        return usuarioRepository.findAll();
    }

    public Usuario obtenerPorId(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado con id: " + id));
    }

    /** Alta hecha por un administrador (puede indicar rol y datos opcionales). */
    @Transactional
    public Usuario crear(Usuario usuario) {
        validarNombreUsuario(usuario);
        if (usuario.getContrasena() == null || usuario.getContrasena().isBlank()) {
            throw new ReglaDeNegocioException("La contraseña es obligatoria");
        }
        if (usuarioRepository.existsByNombreUsuarioIgnoreCase(usuario.getNombreUsuario())) {
            throw new ReglaDeNegocioException("El usuario '" + usuario.getNombreUsuario() + "' ya está registrado");
        }
        validarCorreoLibre(usuario.getCorreo(), null);
        // Si viene la configuración en el mismo JSON, se enlaza con el usuario (cascade ALL)
        if (usuario.getConfiguracionUsuario() != null) {
            usuario.getConfiguracionUsuario().setUsuario(usuario);
            usuario.getConfiguracionUsuario().setId(null);
        }
        if (usuario.getRol() == null) {
            usuario.setRol(Rol.USER);
        }
        usuario.setContrasena(passwordEncoder.encode(usuario.getContrasena()));
        // Un POST siempre crea un registro nuevo: se ignora cualquier id recibido en el body
        usuario.setId(null);
        return usuarioRepository.save(usuario);
    }

    /** Registro público: solo usuario y contraseña; siempre con rol USER. */
    @Transactional
    public Usuario registrar(String nombreUsuario, String contrasena) {
        String nombre = nombreUsuario == null ? "" : nombreUsuario.trim();
        if (nombre.isEmpty()) {
            throw new ReglaDeNegocioException("El usuario es obligatorio");
        }
        if (usuarioRepository.existsByNombreUsuarioIgnoreCase(nombre)) {
            throw new ReglaDeNegocioException("El usuario '" + nombre + "' ya está registrado");
        }
        Usuario usuario = new Usuario();
        usuario.setNombreUsuario(nombre);
        usuario.setContrasena(passwordEncoder.encode(contrasena));
        usuario.setRol(Rol.USER);
        return usuarioRepository.save(usuario);
    }

    /** El propio usuario completa su semestre y programa (ambos opcionales). */
    @Transactional
    public Usuario actualizarPerfil(Long id, Integer semestre, String programa) {
        Usuario usuario = obtenerPorId(id);
        usuario.setSemestre(semestre);
        usuario.setPrograma(programa == null || programa.isBlank() ? null : programa.trim());
        return usuarioRepository.save(usuario);
    }

    @Transactional
    public Usuario actualizar(Long id, Usuario datosUsuario) {
        Usuario usuario = obtenerPorId(id);
        validarNombreUsuario(datosUsuario);

        // Regla de negocio: si cambia el nombre de usuario, el nuevo no puede estar ocupado
        if (!datosUsuario.getNombreUsuario().equalsIgnoreCase(usuario.getNombreUsuario())
                && usuarioRepository.existsByNombreUsuarioIgnoreCase(datosUsuario.getNombreUsuario())) {
            throw new ReglaDeNegocioException("El usuario '" + datosUsuario.getNombreUsuario() + "' ya está en uso");
        }
        validarCorreoLibre(datosUsuario.getCorreo(), usuario);

        usuario.setNombreUsuario(datosUsuario.getNombreUsuario());
        usuario.setCorreo(datosUsuario.getCorreo());
        usuario.setCodigoEstudiante(datosUsuario.getCodigoEstudiante());
        usuario.setPrograma(datosUsuario.getPrograma());
        usuario.setSemestre(datosUsuario.getSemestre());
        usuario.setTelefono(datosUsuario.getTelefono());
        if (datosUsuario.getRol() != null) {
            usuario.setRol(datosUsuario.getRol());
        }
        if (datosUsuario.getEstadoActivo() != null) {
            usuario.setEstadoActivo(datosUsuario.getEstadoActivo());
        }
        // La contraseña solo se cambia si se envía una nueva
        if (datosUsuario.getContrasena() != null && !datosUsuario.getContrasena().isBlank()) {
            usuario.setContrasena(passwordEncoder.encode(datosUsuario.getContrasena()));
        }
        return usuarioRepository.save(usuario);
    }

    @Transactional
    public void eliminar(Long id) {
        Usuario usuario = obtenerPorId(id);
        // Regla de negocio: no se elimina un usuario con historial de reservas
        if (reservaLibroRepository.existsByUsuarioId(id) || reservaEquipoRepository.existsByUsuarioId(id)) {
            throw new ReglaDeNegocioException("No se puede eliminar el usuario porque tiene reservas registradas. Desactívelo (estadoActivo = false)");
        }
        usuarioRepository.delete(usuario);
    }

    // Uso del método personalizado del repositorio
    public Usuario obtenerPorCorreo(String correo) {
        return usuarioRepository.findByCorreoIgnoreCase(correo)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado con correo: " + correo));
    }

    private void validarNombreUsuario(Usuario usuario) {
        if (usuario.getNombreUsuario() == null || usuario.getNombreUsuario().isBlank()) {
            throw new ReglaDeNegocioException("El usuario es obligatorio");
        }
    }

    // El correo es opcional, pero si se envía no puede repetirse
    private void validarCorreoLibre(String correo, Usuario actual) {
        if (correo == null || correo.isBlank()) {
            return;
        }
        usuarioRepository.findByCorreoIgnoreCase(correo).ifPresent(existente -> {
            if (actual == null || !existente.getId().equals(actual.getId())) {
                throw new ReglaDeNegocioException("El correo " + correo + " ya está en uso por otro usuario");
            }
        });
    }
}
