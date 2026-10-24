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

import com.cesde.nexou.model.entity.EquipoTecnologico;
import com.cesde.nexou.service.EquipoTecnologicoService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "Equipos tecnológicos", description = "Inventario de equipos para préstamo y consulta de disponibles")
@RestController
@RequestMapping("/api/equipos")
@RequiredArgsConstructor
public class EquipoTecnologicoController {

    private final EquipoTecnologicoService equipoService;

    @Operation(summary = "Listar todos los equipos")
    @GetMapping
    public List<EquipoTecnologico> obtenerTodos() {
        return equipoService.obtenerTodos();
    }

    @Operation(summary = "Obtener un equipo por su ID")
    @GetMapping("/{id}")
    public ResponseEntity<EquipoTecnologico> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(equipoService.obtenerPorId(id));
    }

    @Operation(summary = "Crear un equipo")
    @PostMapping
    public ResponseEntity<EquipoTecnologico> crear(@Valid @RequestBody EquipoTecnologico equipo) {
        return ResponseEntity.status(HttpStatus.CREATED).body(equipoService.crear(equipo));
    }

    @Operation(summary = "Actualizar un equipo por su ID")
    @PutMapping("/{id}")
    public ResponseEntity<EquipoTecnologico> actualizar(@PathVariable Long id, @Valid @RequestBody EquipoTecnologico equipo) {
        return ResponseEntity.ok(equipoService.actualizar(id, equipo));
    }

    @Operation(summary = "Eliminar un equipo sin reservas registradas")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        equipoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    // Método personalizado: equipos con unidades disponibles
    @Operation(summary = "Listar equipos con unidades disponibles")
    @GetMapping("/disponibles")
    public List<EquipoTecnologico> obtenerDisponibles() {
        return equipoService.obtenerDisponibles();
    }
}
