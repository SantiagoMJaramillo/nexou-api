package com.cesde.nexou.security;

import com.cesde.nexou.model.enums.Rol;

/** Datos del usuario que hizo la petición (se obtienen del token JWT). */
public record UsuarioAutenticado(Long id, String usuario, Rol rol) {

    public boolean esAdmin() {
        return rol == Rol.ADMIN;
    }
}
