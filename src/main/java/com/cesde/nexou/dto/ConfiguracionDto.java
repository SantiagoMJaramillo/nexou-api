package com.cesde.nexou.dto;

import com.cesde.nexou.model.entity.ConfiguracionUsuario;
import com.cesde.nexou.model.entity.Usuario;

import jakarta.validation.constraints.Size;

public final class ConfiguracionDto {

    private ConfiguracionDto() {
    }

    /** usuarioId solo se usa al crear. */
    public record Request(
            @Size(max = 20, message = "no puede superar los 20 caracteres") String idioma,
            @Size(max = 20, message = "no puede superar los 20 caracteres") String tema,
            Boolean notificacionesActivas,
            Long usuarioId) {

        public ConfiguracionUsuario aEntidad() {
            ConfiguracionUsuario c = new ConfiguracionUsuario();
            c.setIdioma(idioma);
            c.setTema(tema);
            c.setNotificacionesActivas(notificacionesActivas);
            if (usuarioId != null) {
                Usuario u = new Usuario();
                u.setId(usuarioId);
                c.setUsuario(u);
            }
            return c;
        }
    }

    public record Response(Long id, Long usuarioId, String idioma, String tema, Boolean notificacionesActivas) {

        public static Response de(ConfiguracionUsuario c) {
            return new Response(c.getId(), c.getUsuario().getId(), c.getIdioma(), c.getTema(),
                    c.getNotificacionesActivas());
        }
    }
}
