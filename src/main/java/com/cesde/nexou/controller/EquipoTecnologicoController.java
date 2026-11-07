package com.cesde.nexou.controller;

import java.util.List;
import java.time.Duration;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import com.cesde.nexou.model.entity.ImagenArticulo;
import com.cesde.nexou.service.ImagenService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cesde.nexou.dto.EquipoDto;
import com.cesde.nexou.service.EquipoTecnologicoService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "Equipos tecnológicos", description = "Equipos en préstamo (lectura: todos; escritura: ADMIN)")
@RestController
@RequestMapping("/api/equipos")
@RequiredArgsConstructor
public class EquipoTecnologicoController {

    private final ImagenService imagenService;
    private final EquipoTecnologicoService equipoService;

    @Operation(summary = "Listar todos los equipos")
    @GetMapping
    public List<EquipoDto.Response> obtenerTodos() {
        return equipoService.obtenerTodos().stream().map(EquipoDto.Response::de).toList();
    }

    @Operation(summary = "Obtener un equipo por su ID")
    @GetMapping("/{id}")
    public EquipoDto.Response obtenerPorId(@PathVariable Long id) {
        return EquipoDto.Response.de(equipoService.obtenerPorId(id));
    }

    @Operation(summary = "Publicar un equipo para préstamo")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<EquipoDto.Response> crear(@Valid @RequestBody EquipoDto.Request datos) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(EquipoDto.Response.de(equipoService.crear(datos.aEntidad())));
    }

    @Operation(summary = "Actualizar un equipo por su ID")
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public EquipoDto.Response actualizar(@PathVariable Long id, @Valid @RequestBody EquipoDto.Request datos) {
        return EquipoDto.Response.de(equipoService.actualizar(id, datos.aEntidad()));
    }

    @Operation(summary = "Eliminar un equipo sin reservas registradas")
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        equipoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Listar los equipos con unidades disponibles")
    @GetMapping("/disponibles")
    public List<EquipoDto.Response> obtenerDisponibles() {
        return equipoService.obtenerDisponibles().stream().map(EquipoDto.Response::de).toList();
    }

    @Operation(summary = "Subir o reemplazar la imagen (equipo)", description = "Multipart con el campo 'archivo' (JPG, PNG o WEBP, máx. 1 MB)")
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping(value = "/{id}/imagen", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public EquipoDto.Response subirImagen(@PathVariable Long id, @RequestParam("archivo") MultipartFile archivo) {
        imagenService.guardarDeEquipo(id, archivo);
        return EquipoDto.Response.de(equipoService.obtenerPorId(id));
    }

    @Operation(summary = "Quitar la imagen (equipo)")
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}/imagen")
    public ResponseEntity<Void> quitarImagen(@PathVariable Long id) {
        imagenService.quitarDeEquipo(id);
        return ResponseEntity.noContent().build();
    }

    // Pública: un <img> no puede enviar el token. La URL lleva ?v=<versión>, por eso el caché puede ser largo.
    @Operation(summary = "Descargar la imagen (equipo)")
    @GetMapping("/{id}/imagen")
    public ResponseEntity<byte[]> obtenerImagen(@PathVariable Long id) {
        ImagenArticulo imagen = imagenService.obtener(ImagenService.EQUIPO, id);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(imagen.getTipoContenido()))
                .cacheControl(CacheControl.maxAge(Duration.ofDays(365)).cachePublic().immutable())
                .header("X-Content-Type-Options", "nosniff")
                .body(imagen.getDatos());
    }
}
