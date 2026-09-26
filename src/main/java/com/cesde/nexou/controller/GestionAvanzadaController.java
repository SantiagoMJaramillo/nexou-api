package com.cesde.nexou.controller;

import com.cesde.nexou.service.RenovacionService;
import com.cesde.nexou.service.ValidacionGlobalService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/avanzado")
@RequiredArgsConstructor
public class GestionAvanzadaController {
    private final RenovacionService renovacionService;
    private final ValidacionGlobalService validacionGlobalService;

    // Endpoint para renovar libros
    @PatchMapping("/reservas-libros/{id}/renovar")
    public ResponseEntity<String> renovar(@PathVariable Long id, @RequestParam Integer diasExtra) {
        return ResponseEntity.ok(renovacionService.renovarLibro(id, diasExtra));
    }

    // Endpoint de prueba para el validador
    @GetMapping("/usuarios/{id}/validar-limite")
    public ResponseEntity<String> validarLimite(@PathVariable Long id) {
        validacionGlobalService.validarLimiteGlobal(id);
        return ResponseEntity.ok("El usuario es apto para más préstamos.");
    }
}
