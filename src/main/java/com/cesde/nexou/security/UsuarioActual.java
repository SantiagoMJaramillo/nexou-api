package com.cesde.nexou.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.cesde.nexou.exception.AccesoDenegadoException;

/** Acceso cómodo al usuario autenticado desde controladores y servicios. */
@Component
public class UsuarioActual {

    public UsuarioAutenticado obtener() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof UsuarioAutenticado usuario)) {
            throw new AccesoDenegadoException("Debes iniciar sesión");
        }
        return usuario;
    }

    /** Un USER solo puede tocar lo suyo; un ADMIN puede tocar todo. */
    public void exigirPropietarioOAdmin(Long propietarioId) {
        UsuarioAutenticado actual = obtener();
        if (!actual.esAdmin() && !actual.id().equals(propietarioId)) {
            throw new AccesoDenegadoException("No tienes permiso sobre este recurso");
        }
    }
}
