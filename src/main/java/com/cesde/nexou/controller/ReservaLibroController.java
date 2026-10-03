package com.cesde.nexou.controller;
import com.cesde.nexou.dto.request.CrearReservaLibroRequest;
import com.cesde.nexou.dto.response.ReservaLibroResponse;
import com.cesde.nexou.service.ReservaLibroService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reservas-libros")
@RequiredArgsConstructor
public class ReservaLibroController {
    private final ReservaLibroService service;

    @PostMapping
    public ResponseEntity<ReservaLibroResponse> crear(@Valid @RequestBody CrearReservaLibroRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crear(request));
    }

    @PatchMapping("/{id}/devolucion")
    public ResponseEntity<ReservaLibroResponse> devolver(@PathVariable Long id) {
        return ResponseEntity.ok(service.devolver(id));
    }
}
