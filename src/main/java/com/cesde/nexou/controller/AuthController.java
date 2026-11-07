package com.cesde.nexou.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cesde.nexou.dto.AuthDto;
import com.cesde.nexou.dto.UsuarioDto;
import com.cesde.nexou.security.UsuarioActual;
import com.cesde.nexou.service.AuthService;
import com.cesde.nexou.service.UsuarioService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "Autenticación", description = "Registro (solo usuario y contraseña), inicio de sesión y perfil propio")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final UsuarioService usuarioService;
    private final UsuarioActual usuarioActual;

    @Operation(summary = "Registrarse", description = "Solo pide usuario y contraseña. Crea una cuenta con rol USER y devuelve el token JWT.")
    @PostMapping("/registro")
    public ResponseEntity<AuthDto.Respuesta> registro(@Valid @RequestBody AuthDto.Registro datos) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.registrar(datos));
    }

    @Operation(summary = "Iniciar sesión", description = "Devuelve el token JWT que se envía como 'Authorization: Bearer <token>'.")
    @PostMapping("/login")
    public AuthDto.Respuesta login(@Valid @RequestBody AuthDto.Login datos) {
        return authService.login(datos);
    }

    @Operation(summary = "Completar mi perfil (semestre y programa, opcionales)")
    @PutMapping("/perfil")
    public UsuarioDto.Response perfil(@Valid @RequestBody AuthDto.Perfil datos) {
        return UsuarioDto.Response.de(usuarioService.actualizarPerfil(usuarioActual.obtener().id(), datos.semestre(), datos.programa()));
    }

    @Operation(summary = "Datos del usuario autenticado")
    @GetMapping("/yo")
    public UsuarioDto.Response yo() {
        return UsuarioDto.Response.de(usuarioService.obtenerPorId(usuarioActual.obtener().id()));
    }
}
