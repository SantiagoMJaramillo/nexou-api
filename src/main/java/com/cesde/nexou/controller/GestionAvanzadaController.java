package com.cesde.nexou.controller;

import com.cesde.nexou.service.ValidacionGlobalService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/avanzado")
@RequiredArgsConstructor
public class GestionAvanzadaController {
    private final ValidacionGlobalService validacionGlobalService;

    // Endpoint de prueba para el validador
    @GetMapping("/usuarios/{id}/validar-limite")
    public ResponseEntity<String> validarLimite(@PathVariable Long id) {
        validacionGlobalService.validarLimiteGlobal(id);
        return ResponseEntity.ok("El usuario es apto para más préstamos.");
    }
}
