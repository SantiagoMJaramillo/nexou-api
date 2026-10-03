package com.cesde.nexou.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cesde.nexou.exception.RecursoNoEncontradoException;
import com.cesde.nexou.exception.ReglaDeNegocioException;
import com.cesde.nexou.model.entity.ConfiguracionUsuario;
import com.cesde.nexou.model.entity.Usuario;
import com.cesde.nexou.repository.ConfiguracionUsuarioRepository;
import com.cesde.nexou.repository.UsuarioRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ConfiguracionUsuarioService {

    private final ConfiguracionUsuarioRepository configuracionRepository;
    private final UsuarioRepository usuarioRepository;

    public List<ConfiguracionUsuario> obtenerTodos() {
        return configuracionRepository.findAll();
    }

    public ConfiguracionUsuario obtenerPorId(Long id) {
        return configuracionRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Configuración no encontrada con id: " + id));
    }

    @Transactional
    public ConfiguracionUsuario crear(ConfiguracionUsuario configuracion) {
        if (configuracion.getUsuario() == null || configuracion.getUsuario().getId() == null) {
            throw new ReglaDeNegocioException("La configuración debe estar asociada a un usuario con ID válido");
        }
        Long usuarioId = configuracion.getUsuario().getId();
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado con id: " + usuarioId));

        // Regla de negocio: relación 1 a 1, un usuario solo tiene una configuración
        if (configuracionRepository.existsByUsuarioId(usuarioId)) {
            throw new ReglaDeNegocioException("El usuario con id " + usuarioId + " ya tiene una configuración. Use PUT para modificarla");
        }
        configuracion.setUsuario(usuario);
        return configuracionRepository.save(configuracion);
    }

    @Transactional
    public ConfiguracionUsuario actualizar(Long id, ConfiguracionUsuario datos) {
        ConfiguracionUsuario configuracion = obtenerPorId(id);
        configuracion.setIdioma(datos.getIdioma());
        configuracion.setTema(datos.getTema());
        if (datos.getNotificacionesActivas() != null) {
            configuracion.setNotificacionesActivas(datos.getNotificacionesActivas());
        }
        return configuracionRepository.save(configuracion);
    }

    @Transactional
    public void eliminar(Long id) {
        ConfiguracionUsuario configuracion = obtenerPorId(id);
        // Se rompe el enlace bidireccional para que el cascade del usuario no la vuelva a guardar
        if (configuracion.getUsuario() != null) {
            configuracion.getUsuario().setConfiguracionUsuario(null);
        }
        configuracionRepository.delete(configuracion);
    }

    // Uso del método personalizado del repositorio
    public ConfiguracionUsuario obtenerPorUsuarioId(Long usuarioId) {
        if (!usuarioRepository.existsById(usuarioId)) {
            throw new RecursoNoEncontradoException("Usuario no encontrado con id: " + usuarioId);
        }
        return configuracionRepository.findByUsuarioId(usuarioId)
                .orElseThrow(() -> new RecursoNoEncontradoException("El usuario con id " + usuarioId + " no tiene configuración"));
    }
}
