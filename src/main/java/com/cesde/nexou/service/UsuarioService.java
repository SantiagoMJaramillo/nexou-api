package com.cesde.nexou.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cesde.nexou.exception.RecursoNoEncontradoException;
import com.cesde.nexou.exception.ReglaDeNegocioException;
import com.cesde.nexou.model.entity.Usuario;
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

    public List<Usuario> obtenerTodos() {
        return usuarioRepository.findAll();
    }

    public Usuario obtenerPorId(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado con id: " + id));
    }

    @Transactional
    public Usuario crear(Usuario usuario) {
        validarCamposObligatorios(usuario);
        if (usuario.getContrasena() == null || usuario.getContrasena().isBlank()) {
            throw new ReglaDeNegocioException("La contraseña es obligatoria");
        }
        // Regla de negocio: no permitir dos usuarios con el mismo correo
        if (usuarioRepository.findByCorreo(usuario.getCorreo()).isPresent()) {
            throw new ReglaDeNegocioException("Ya existe un usuario con el correo: " + usuario.getCorreo());
        }
        // Si viene la configuración en el mismo JSON, se enlaza con el usuario (cascade ALL)
        if (usuario.getConfiguracionUsuario() != null) {
            usuario.getConfiguracionUsuario().setUsuario(usuario);
        }
        return usuarioRepository.save(usuario);
    }

    @Transactional
    public Usuario actualizar(Long id, Usuario datosUsuario) {
        Usuario usuario = obtenerPorId(id);
        validarCamposObligatorios(datosUsuario);

        // Regla de negocio: si cambia el correo, el nuevo no puede estar ocupado
        if (!datosUsuario.getCorreo().equalsIgnoreCase(usuario.getCorreo())
                && usuarioRepository.findByCorreo(datosUsuario.getCorreo()).isPresent()) {
            throw new ReglaDeNegocioException("El correo " + datosUsuario.getCorreo() + " ya está en uso por otro usuario");
        }

        usuario.setNombreUsuario(datosUsuario.getNombreUsuario());
        usuario.setCorreo(datosUsuario.getCorreo());
        usuario.setCodigoEstudiante(datosUsuario.getCodigoEstudiante());
        usuario.setPrograma(datosUsuario.getPrograma());
        usuario.setSemestre(datosUsuario.getSemestre());
        usuario.setTelefono(datosUsuario.getTelefono());
        if (datosUsuario.getEstadoActivo() != null) {
            usuario.setEstadoActivo(datosUsuario.getEstadoActivo());
        }
        // La contraseña solo se cambia si se envía una nueva
        if (datosUsuario.getContrasena() != null && !datosUsuario.getContrasena().isBlank()) {
            usuario.setContrasena(datosUsuario.getContrasena());
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
        return usuarioRepository.findByCorreo(correo)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado con correo: " + correo));
    }

    private void validarCamposObligatorios(Usuario usuario) {
        if (usuario.getNombreUsuario() == null || usuario.getNombreUsuario().isBlank()) {
            throw new ReglaDeNegocioException("El nombre del usuario es obligatorio");
        }
        if (usuario.getCorreo() == null || usuario.getCorreo().isBlank()) {
            throw new ReglaDeNegocioException("El correo del usuario es obligatorio");
        }
    }
}
