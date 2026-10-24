package com.cesde.nexou.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cesde.nexou.model.entity.Usuario;
import com.cesde.nexou.service.UsuarioService;
import com.cesde.nexou.service.ValidacionGlobalService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "Usuarios", description = "Gestión de usuarios (estudiantes) y búsqueda por correo")
@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;
    private final ValidacionGlobalService validacionGlobalService;

    @Operation(summary = "Listar todos los usuarios")
    @GetMapping
    public List<Usuario> obtenerTodos() {
        return usuarioService.obtenerTodos();
    }

    @Operation(summary = "Obtener un usuario por su ID")
    @GetMapping("/{id}")
    public ResponseEntity<Usuario> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(usuarioService.obtenerPorId(id));
    }

    @Operation(summary = "Crear un usuario (opcionalmente con su configuración)")
    @PostMapping
    public ResponseEntity<Usuario> crear(@Valid @RequestBody Usuario usuario) {
        return ResponseEntity.status(HttpStatus.CREATED).body(usuarioService.crear(usuario));
    }

    @Operation(summary = "Actualizar un usuario por su ID")
    @PutMapping("/{id}")
    public ResponseEntity<Usuario> actualizar(@PathVariable Long id, @Valid @RequestBody Usuario usuario) {
        return ResponseEntity.ok(usuarioService.actualizar(id, usuario));
    }

    @Operation(summary = "Eliminar un usuario sin reservas registradas")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        usuarioService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    // Método personalizado: buscar usuario por correo
    @Operation(summary = "Buscar un usuario por su correo")
    @GetMapping("/correo/{correo}")
    public ResponseEntity<Usuario> obtenerPorCorreo(@PathVariable String correo) {
        return ResponseEntity.ok(usuarioService.obtenerPorCorreo(correo));
    }

    @Operation(summary = "Validar si el usuario puede hacer más reservas (máximo 3 activas)")
    @GetMapping("/{id}/validar-limite")
    public ResponseEntity<Map<String, String>> validarLimite(@PathVariable Long id) {
        usuarioService.obtenerPorId(id);
        validacionGlobalService.validarLimiteGlobal(id);
        return ResponseEntity.ok(Map.of("mensaje", "El usuario es apto para más préstamos."));
    }
}
