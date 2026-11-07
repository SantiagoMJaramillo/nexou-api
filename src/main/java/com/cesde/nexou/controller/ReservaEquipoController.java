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
import org.springframework.web.bind.annotation.RestController;

import com.cesde.nexou.dto.ReservaEquipoDto;
import com.cesde.nexou.security.UsuarioActual;
import com.cesde.nexou.security.UsuarioAutenticado;
import com.cesde.nexou.service.ReservaEquipoService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "Reservas de equipos", description = "Préstamo y devolución de equipos. Un USER solo ve y modifica las suyas.")
@RestController
@RequestMapping("/api/reservas-equipos")
@RequiredArgsConstructor
public class ReservaEquipoController {

    private final ReservaEquipoService service;
    private final UsuarioActual usuarioActual;

    @Operation(summary = "Listar todas las reservas de equipos (ADMIN)")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public List<ReservaEquipoDto.Response> obtenerTodos() {
        return service.obtenerTodos().stream().map(ReservaEquipoDto.Response::de).toList();
    }

    @Operation(summary = "Listar mis reservas de equipos")
    @GetMapping("/mis-reservas")
    public List<ReservaEquipoDto.Response> misReservas() {
        return service.obtenerPorUsuarioId(usuarioActual.obtener().id()).stream()
                .map(ReservaEquipoDto.Response::de).toList();
    }

    @Operation(summary = "Obtener una reserva de equipo por su ID (dueño o ADMIN)")
    @GetMapping("/{id}")
    public ReservaEquipoDto.Response obtenerPorId(@PathVariable Long id) {
        var reserva = service.obtenerPorId(id);
        usuarioActual.exigirPropietarioOAdmin(reserva.getUsuario().getId());
        return ReservaEquipoDto.Response.de(reserva);
    }

    @Operation(summary = "Reservar un equipo", description = "Valida stock, duración máxima y el límite de 3 reservas activas. Un USER siempre reserva a su nombre.")
    @PostMapping
    public ResponseEntity<ReservaEquipoDto.Response> crear(@Valid @RequestBody ReservaEquipoDto.Request datos) {
        UsuarioAutenticado actual = usuarioActual.obtener();
        Long usuarioId = actual.esAdmin() && datos.usuarioId() != null ? datos.usuarioId() : actual.id();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ReservaEquipoDto.Response.de(service.crear(datos.aEntidad(usuarioId))));
    }

    @Operation(summary = "Actualizar lugar de entrega y propósito de una reserva")
    @PutMapping("/{id}")
    public ReservaEquipoDto.Response actualizar(@PathVariable Long id, @Valid @RequestBody ReservaEquipoDto.Edicion datos) {
        usuarioActual.exigirPropietarioOAdmin(service.obtenerPorId(id).getUsuario().getId());
        return ReservaEquipoDto.Response.de(service.actualizar(id, datos.aEntidad()));
    }

    @Operation(summary = "Eliminar una reserva que no esté activa (ADMIN)")
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Listar las reservas de equipos de un usuario (el mismo usuario o ADMIN)")
    @GetMapping("/usuario/{usuarioId}")
    public List<ReservaEquipoDto.Response> obtenerPorUsuarioId(@PathVariable Long usuarioId) {
        usuarioActual.exigirPropietarioOAdmin(usuarioId);
        return service.obtenerPorUsuarioId(usuarioId).stream().map(ReservaEquipoDto.Response::de).toList();
    }

    @Operation(summary = "Registrar la devolución de un equipo")
    @PatchMapping("/{id}/devolucion")
    public ReservaEquipoDto.Response devolver(@PathVariable Long id) {
        usuarioActual.exigirPropietarioOAdmin(service.obtenerPorId(id).getUsuario().getId());
        return ReservaEquipoDto.Response.de(service.devolver(id));
    }
}
