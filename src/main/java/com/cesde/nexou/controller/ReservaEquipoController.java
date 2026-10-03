package com.cesde.nexou.controller;
import com.cesde.nexou.model.entity.ReservaEquipo;
import com.cesde.nexou.service.ReservaEquipoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Reservas de equipos", description = "Préstamo, devolución y consulta de reservas de equipos por usuario")
@RestController
@RequestMapping("/api/reservas-equipos")
@RequiredArgsConstructor
public class ReservaEquipoController {
    private final ReservaEquipoService service;

    @Operation(summary = "Listar todas las reservas de equipos")
    @GetMapping
    public List<ReservaEquipo> obtenerTodos() {
        return service.obtenerTodos();
    }

    @Operation(summary = "Obtener una reserva de equipo por su ID")
    @GetMapping("/{id}")
    public ResponseEntity<ReservaEquipo> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(service.obtenerPorId(id));
    }

    @Operation(summary = "Prestar un equipo", description = "Valida usuario activo, stock, horario, duración máxima y el límite de 3 reservas activas")
    @PostMapping
    public ResponseEntity<ReservaEquipo> crear(@Valid @RequestBody ReservaEquipo request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crear(request));
    }

    @Operation(summary = "Actualizar lugar de entrega y propósito de una reserva")
    @PutMapping("/{id}")
    public ResponseEntity<ReservaEquipo> actualizar(@PathVariable Long id, @Valid @RequestBody ReservaEquipo request) {
        return ResponseEntity.ok(service.actualizar(id, request));
    }

    @Operation(summary = "Eliminar una reserva que no esté activa")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    // Método personalizado: reservas de equipos de un usuario
    @Operation(summary = "Listar las reservas de equipos de un usuario")
    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<List<ReservaEquipo>> obtenerPorUsuarioId(@PathVariable Long usuarioId) {
        return ResponseEntity.ok(service.obtenerPorUsuarioId(usuarioId));
    }

    @Operation(summary = "Registrar la devolución de un equipo")
    @PatchMapping("/{id}/devolucion")
    public ResponseEntity<ReservaEquipo> devolver(@PathVariable Long id) {
        return ResponseEntity.ok(service.devolver(id));
    }
}
