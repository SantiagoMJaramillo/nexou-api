package com.cesde.nexou.controller;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Hidden;

/** Ruta pública para el health check de Render y para despertar el servicio gratuito. */
@Hidden
@RestController
public class HealthController {

    @GetMapping({"/", "/health"})
    public Map<String, String> health() {
        return Map.of("estado", "ok", "servicio", "nexou-api");
    }
}
