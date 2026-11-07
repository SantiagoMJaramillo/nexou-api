package com.cesde.nexou.controller;

import java.util.List;

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

import com.cesde.nexou.dto.ConfiguracionDto;
import com.cesde.nexou.service.ConfiguracionUsuarioService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "Configuración de usuario", description = "Preferencias de cada usuario (solo ADMIN)")
@RestController
@RequestMapping("/api/configuraciones")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class ConfiguracionUsuarioController {

    private final ConfiguracionUsuarioService service;

    @Operation(summary = "Listar todas las configuraciones")
    @GetMapping
    public List<ConfiguracionDto.Response> obtenerTodos() {
        return service.obtenerTodos().stream().map(ConfiguracionDto.Response::de).toList();
    }

    @Operation(summary = "Obtener una configuración por su ID")
    @GetMapping("/{id}")
    public ConfiguracionDto.Response obtenerPorId(@PathVariable Long id) {
        return ConfiguracionDto.Response.de(service.obtenerPorId(id));
    }

    @Operation(summary = "Crear la configuración de un usuario (usuarioId obligatorio)")
    @PostMapping
    public ResponseEntity<ConfiguracionDto.Response> crear(@Valid @RequestBody ConfiguracionDto.Request datos) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ConfiguracionDto.Response.de(service.crear(datos.aEntidad())));
    }

    @Operation(summary = "Actualizar una configuración")
    @PutMapping("/{id}")
    public ConfiguracionDto.Response actualizar(@PathVariable Long id, @Valid @RequestBody ConfiguracionDto.Request datos) {
        return ConfiguracionDto.Response.de(service.actualizar(id, datos.aEntidad()));
    }

    @Operation(summary = "Eliminar una configuración")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Obtener la configuración de un usuario")
    @GetMapping("/usuario/{usuarioId}")
    public ConfiguracionDto.Response obtenerPorUsuario(@PathVariable Long usuarioId) {
        return ConfiguracionDto.Response.de(service.obtenerPorUsuarioId(usuarioId));
    }
}
