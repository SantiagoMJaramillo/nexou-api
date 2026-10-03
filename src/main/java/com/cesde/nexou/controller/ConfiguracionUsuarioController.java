package com.cesde.nexou.controller;

import java.util.List;

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

import com.cesde.nexou.model.entity.ConfiguracionUsuario;
import com.cesde.nexou.service.ConfiguracionUsuarioService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "Configuración de usuario", description = "Preferencias del usuario (idioma, tema, notificaciones)")
@RestController
@RequestMapping("/api/configuraciones")
@RequiredArgsConstructor
public class ConfiguracionUsuarioController {

    private final ConfiguracionUsuarioService configuracionService;

    @Operation(summary = "Listar todas las configuraciones")
    @GetMapping
    public List<ConfiguracionUsuario> obtenerTodos() {
        return configuracionService.obtenerTodos();
    }

    @Operation(summary = "Obtener una configuración por su ID")
    @GetMapping("/{id}")
    public ResponseEntity<ConfiguracionUsuario> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(configuracionService.obtenerPorId(id));
    }

    @Operation(summary = "Crear la configuración de un usuario", description = "Body de ejemplo: {\"idioma\":\"es\",\"tema\":\"oscuro\",\"notificacionesActivas\":true,\"usuario\":{\"id\":1}}")
    @PostMapping
    public ResponseEntity<ConfiguracionUsuario> crear(@RequestBody ConfiguracionUsuario configuracion) {
        return ResponseEntity.status(HttpStatus.CREATED).body(configuracionService.crear(configuracion));
    }

    @Operation(summary = "Actualizar una configuración por su ID")
    @PutMapping("/{id}")
    public ResponseEntity<ConfiguracionUsuario> actualizar(@PathVariable Long id, @RequestBody ConfiguracionUsuario configuracion) {
        return ResponseEntity.ok(configuracionService.actualizar(id, configuracion));
    }

    @Operation(summary = "Eliminar una configuración por su ID")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        configuracionService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    // Método personalizado: configuración de un usuario
    @Operation(summary = "Obtener la configuración de un usuario")
    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<ConfiguracionUsuario> obtenerPorUsuarioId(@PathVariable Long usuarioId) {
        return ResponseEntity.ok(configuracionService.obtenerPorUsuarioId(usuarioId));
    }
}
