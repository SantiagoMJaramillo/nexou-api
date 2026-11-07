package com.cesde.nexou.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.cesde.nexou.dto.AuthDto;
import com.cesde.nexou.dto.UsuarioDto;
import com.cesde.nexou.exception.CredencialesInvalidasException;
import com.cesde.nexou.model.entity.Usuario;
import com.cesde.nexou.model.enums.Rol;
import com.cesde.nexou.repository.UsuarioRepository;
import com.cesde.nexou.security.JwtService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioService usuarioService;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    /** Crea la cuenta y devuelve el token, así el usuario queda dentro sin hacer login aparte. */
    public AuthDto.Respuesta registrar(AuthDto.Registro datos) {
        Usuario usuario = usuarioService.registrar(datos.usuario(), datos.contrasena());
        return respuesta(usuario);
    }

    public AuthDto.Respuesta login(AuthDto.Login datos) {
        Usuario usuario = usuarioRepository.findByNombreUsuarioIgnoreCase(datos.usuario().trim())
                .filter(u -> Boolean.TRUE.equals(u.getEstadoActivo()))
                .filter(u -> passwordEncoder.matches(datos.contrasena(), u.getContrasena()))
                .orElseThrow(() -> new CredencialesInvalidasException("Usuario o contraseña incorrectos"));
        return respuesta(usuario);
    }

    private AuthDto.Respuesta respuesta(Usuario usuario) {
        if (usuario.getRol() == null) {
            usuario.setRol(Rol.USER);
        }
        return new AuthDto.Respuesta(jwtService.generar(usuario), UsuarioDto.Response.de(usuario));
    }
}
