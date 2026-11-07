package com.cesde.nexou.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cesde.nexou.dto.UsuarioDto;
import com.cesde.nexou.service.UsuarioService;
import com.cesde.nexou.service.ValidacionGlobalService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "Usuarios", description = "Gestión de usuarios (solo ADMIN)")
@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class UsuarioController {

    private final UsuarioService usuarioService;
    private final ValidacionGlobalService validacionGlobalService;

    @Operation(summary = "Listar todos los usuarios")
    @GetMapping
    public List<UsuarioDto.Response> obtenerTodos() {
        return usuarioService.obtenerTodos().stream().map(UsuarioDto.Response::de).toList();
    }

    @Operation(summary = "Obtener un usuario por su ID")
    @GetMapping("/{id}")
    public UsuarioDto.Response obtenerPorId(@PathVariable Long id) {
        return UsuarioDto.Response.de(usuarioService.obtenerPorId(id));
    }

    @Operation(summary = "Crear un usuario (puede ser otro administrador)")
    @PostMapping
    public ResponseEntity<UsuarioDto.Response> crear(@Valid @RequestBody UsuarioDto.Request datos) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(UsuarioDto.Response.de(usuarioService.crear(datos.aEntidad())));
    }

    @Operation(summary = "Actualizar un usuario (rol, estado, datos de perfil)")
    @PutMapping("/{id}")
    public UsuarioDto.Response actualizar(@PathVariable Long id, @Valid @RequestBody UsuarioDto.Request datos) {
        return UsuarioDto.Response.de(usuarioService.actualizar(id, datos.aEntidad()));
    }

    @Operation(summary = "Eliminar un usuario sin reservas registradas")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        usuarioService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Buscar un usuario por su correo")
    @GetMapping("/correo/{correo}")
    public UsuarioDto.Response obtenerPorCorreo(@PathVariable String correo) {
        return UsuarioDto.Response.de(usuarioService.obtenerPorCorreo(correo));
    }

    @Operation(summary = "Validar si el usuario puede hacer más reservas (máximo 3 activas)")
    @GetMapping("/{id}/validar-limite")
    public Map<String, String> validarLimite(@PathVariable Long id) {
        usuarioService.obtenerPorId(id);
        validacionGlobalService.validarLimiteGlobal(id);
        return Map.of("mensaje", "El usuario es apto para más préstamos.");
    }
}
