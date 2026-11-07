package com.cesde.nexou.security;

import java.io.IOException;
import java.util.List;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.cesde.nexou.model.enums.Rol;
import com.cesde.nexou.repository.UsuarioRepository;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

/** Lee el header Authorization (Bearer token) y autentica la petición. */
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UsuarioRepository usuarioRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            Long usuarioId = jwtService.validarYObtenerUsuarioId(header.substring(7));
            if (usuarioId != null) {
                // Se consulta la BD para respetar usuarios desactivados y cambios de rol
                usuarioRepository.findByIdAndEstadoActivoTrue(usuarioId).ifPresent(usuario -> {
                    Rol rol = usuario.getRol() != null ? usuario.getRol() : Rol.USER;
                    var principal = new UsuarioAutenticado(usuario.getId(), usuario.getNombreUsuario(), rol);
                    var autenticacion = new UsernamePasswordAuthenticationToken(
                            principal, null, List.of(new SimpleGrantedAuthority("ROLE_" + rol.name())));
                    SecurityContextHolder.getContext().setAuthentication(autenticacion);
                });
            }
        }
        chain.doFilter(request, response);
    }
}
