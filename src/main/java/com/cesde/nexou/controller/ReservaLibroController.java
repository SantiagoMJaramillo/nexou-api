package com.cesde.nexou.controller;
import com.cesde.nexou.dto.request.ActualizarReservaLibroRequest;
import com.cesde.nexou.dto.request.CrearReservaLibroRequest;
import com.cesde.nexou.dto.request.RenovarLibroRequest;
import com.cesde.nexou.dto.response.ReservaLibroResponse;
import com.cesde.nexou.service.ReservaLibroService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Reservas de libros", description = "Préstamo, renovación, devolución y consulta de reservas de libros por usuario")
@RestController
@RequestMapping("/api/reservas-libros")
@RequiredArgsConstructor
public class ReservaLibroController {
    private final ReservaLibroService service;

    @Operation(summary = "Listar todas las reservas de libros")
    @GetMapping
    public List<ReservaLibroResponse> obtenerTodos() {
        return service.obtenerTodos();
    }

    @Operation(summary = "Obtener una reserva de libro por su ID")
    @GetMapping("/{id}")
    public ResponseEntity<ReservaLibroResponse> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(service.obtenerPorId(id));
    }

    @Operation(summary = "Prestar un libro", description = "Valida usuario activo, stock, días máximos y el límite de 3 reservas activas")
    @PostMapping
    public ResponseEntity<ReservaLibroResponse> crear(@Valid @RequestBody CrearReservaLibroRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crear(request));
    }

    @Operation(summary = "Actualizar tipo de préstamo y propósito de una reserva")
    @PutMapping("/{id}")
    public ResponseEntity<ReservaLibroResponse> actualizar(@PathVariable Long id, @Valid @RequestBody ActualizarReservaLibroRequest request) {
        return ResponseEntity.ok(service.actualizar(id, request));
    }

    @Operation(summary = "Eliminar una reserva que no esté activa")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    // Método personalizado: reservas de libros de un usuario
    @Operation(summary = "Listar las reservas de libros de un usuario")
    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<List<ReservaLibroResponse>> obtenerPorUsuarioId(@PathVariable Long usuarioId) {
        return ResponseEntity.ok(service.obtenerPorUsuarioId(usuarioId));
    }

    @Operation(summary = "Registrar la devolución de un libro")
    @PatchMapping("/{id}/devolucion")
    public ResponseEntity<ReservaLibroResponse> devolver(@PathVariable Long id) {
        return ResponseEntity.ok(service.devolver(id));
    }

    @Operation(summary = "Renovar una reserva activa (máximo 10 días extra)")
    @PatchMapping("/{id}/renovacion")
    public ResponseEntity<ReservaLibroResponse> renovar(@PathVariable Long id, @Valid @RequestBody RenovarLibroRequest request) {
        return ResponseEntity.ok(service.renovar(id, request));
    }
}
