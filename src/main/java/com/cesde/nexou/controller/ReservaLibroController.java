package com.cesde.nexou.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.cesde.nexou.dto.ReservaLibroDto;
import com.cesde.nexou.security.UsuarioActual;
import com.cesde.nexou.security.UsuarioAutenticado;
import com.cesde.nexou.service.ReservaLibroService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "Reservas de libros", description = "Préstamo, renovación y devolución. Un USER solo ve y modifica las suyas.")
@RestController
@RequestMapping("/api/reservas-libros")
@RequiredArgsConstructor
public class ReservaLibroController {

    private final ReservaLibroService service;
    private final UsuarioActual usuarioActual;

    @Operation(summary = "Listar todas las reservas de libros (ADMIN)")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public List<ReservaLibroDto.Response> obtenerTodos() {
        return service.obtenerTodos().stream().map(ReservaLibroDto.Response::de).toList();
    }

    @Operation(summary = "Listar mis reservas de libros")
    @GetMapping("/mis-reservas")
    public List<ReservaLibroDto.Response> misReservas() {
        return service.obtenerPorUsuarioId(usuarioActual.obtener().id()).stream()
                .map(ReservaLibroDto.Response::de).toList();
    }

    @Operation(summary = "Obtener una reserva de libro por su ID (dueño o ADMIN)")
    @GetMapping("/{id}")
    public ReservaLibroDto.Response obtenerPorId(@PathVariable Long id) {
        var reserva = service.obtenerPorId(id);
        usuarioActual.exigirPropietarioOAdmin(reserva.getUsuario().getId());
        return ReservaLibroDto.Response.de(reserva);
    }

    @Operation(summary = "Prestar un libro", description = "Valida stock, días máximos y el límite de 3 reservas activas. Un USER siempre reserva a su nombre.")
    @PostMapping
    public ResponseEntity<ReservaLibroDto.Response> crear(@Valid @RequestBody ReservaLibroDto.Request datos) {
        UsuarioAutenticado actual = usuarioActual.obtener();
        Long usuarioId = actual.esAdmin() && datos.usuarioId() != null ? datos.usuarioId() : actual.id();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ReservaLibroDto.Response.de(service.crear(datos.aEntidad(usuarioId))));
    }

    @Operation(summary = "Actualizar tipo de préstamo y propósito de una reserva")
    @PutMapping("/{id}")
    public ReservaLibroDto.Response actualizar(@PathVariable Long id, @Valid @RequestBody ReservaLibroDto.Edicion datos) {
        usuarioActual.exigirPropietarioOAdmin(service.obtenerPorId(id).getUsuario().getId());
        return ReservaLibroDto.Response.de(service.actualizar(id, datos.aEntidad()));
    }

    @Operation(summary = "Eliminar una reserva que no esté activa (ADMIN)")
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Listar las reservas de libros de un usuario (el mismo usuario o ADMIN)")
    @GetMapping("/usuario/{usuarioId}")
    public List<ReservaLibroDto.Response> obtenerPorUsuarioId(@PathVariable Long usuarioId) {
        usuarioActual.exigirPropietarioOAdmin(usuarioId);
        return service.obtenerPorUsuarioId(usuarioId).stream().map(ReservaLibroDto.Response::de).toList();
    }

    @Operation(summary = "Registrar la devolución de un libro")
    @PatchMapping("/{id}/devolucion")
    public ReservaLibroDto.Response devolver(@PathVariable Long id) {
        usuarioActual.exigirPropietarioOAdmin(service.obtenerPorId(id).getUsuario().getId());
        return ReservaLibroDto.Response.de(service.devolver(id));
    }

    @Operation(summary = "Renovar una reserva activa (máximo 10 días extra)")
    @PatchMapping("/{id}/renovacion")
    public ReservaLibroDto.Response renovar(@PathVariable Long id, @RequestParam Integer diasExtra) {
        usuarioActual.exigirPropietarioOAdmin(service.obtenerPorId(id).getUsuario().getId());
        return ReservaLibroDto.Response.de(service.renovar(id, diasExtra));
    }
}
