package com.cesde.nexou.controller;
import com.cesde.nexou.dto.request.CrearReservaEquipoRequest;
import com.cesde.nexou.dto.response.ReservaEquipoResponse;
import com.cesde.nexou.service.ReservaEquipoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reservas-equipos")
@RequiredArgsConstructor
public class ReservaEquipoController {
    private final ReservaEquipoService service;

    @PostMapping
    public ResponseEntity<ReservaEquipoResponse> crear(@Valid @RequestBody CrearReservaEquipoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crear(request));
    }

    @PatchMapping("/{id}/devolucion")
    public ResponseEntity<ReservaEquipoResponse> devolver(@PathVariable Long id) {
        return ResponseEntity.ok(service.devolver(id));
    }
}