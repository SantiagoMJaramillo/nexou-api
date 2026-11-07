package com.cesde.nexou.controller;

import java.util.List;

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

import com.cesde.nexou.dto.CategoriaDto;
import com.cesde.nexou.service.CategoriaService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "Categorías", description = "Categorías temáticas de los libros (lectura: todos; escritura: ADMIN)")
@RestController
@RequestMapping("/api/categorias")
@RequiredArgsConstructor
public class CategoriaController {

    private final CategoriaService categoriaService;

    @Operation(summary = "Listar todas las categorías")
    @GetMapping
    public List<CategoriaDto.Response> obtenerTodos() {
        return categoriaService.obtenerTodos().stream().map(CategoriaDto.Response::de).toList();
    }

    @Operation(summary = "Obtener una categoría por su ID")
    @GetMapping("/{id}")
    public CategoriaDto.Response obtenerPorId(@PathVariable Long id) {
        return CategoriaDto.Response.de(categoriaService.obtenerPorId(id));
    }

    @Operation(summary = "Crear una categoría")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<CategoriaDto.Response> crear(@Valid @RequestBody CategoriaDto.Request datos) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(CategoriaDto.Response.de(categoriaService.crear(datos.aEntidad())));
    }

    @Operation(summary = "Actualizar una categoría")
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public CategoriaDto.Response actualizar(@PathVariable Long id, @Valid @RequestBody CategoriaDto.Request datos) {
        return CategoriaDto.Response.de(categoriaService.actualizar(id, datos.aEntidad()));
    }

    @Operation(summary = "Eliminar una categoría sin libros asociados")
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        categoriaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Buscar una categoría por su nombre")
    @GetMapping("/nombre/{nombre}")
    public CategoriaDto.Response obtenerPorNombre(@PathVariable String nombre) {
        return CategoriaDto.Response.de(categoriaService.obtenerPorNombre(nombre));
    }
}
