package com.cesde.nexou.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cesde.nexou.model.entity.Categoria;
import com.cesde.nexou.service.CategoriaService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "Categorías", description = "Categorías temáticas de los libros y búsqueda por nombre")
@RestController
@RequestMapping("/api/categorias")
@RequiredArgsConstructor
public class CategoriaController {

    private final CategoriaService categoriaService;

    @Operation(summary = "Listar todas las categorías")
    @GetMapping
    public List<Categoria> obtenerTodos() {
        return categoriaService.obtenerTodos();
    }

    @Operation(summary = "Obtener una categoría por su ID")
    @GetMapping("/{id}")
    public ResponseEntity<Categoria> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(categoriaService.obtenerPorId(id));
    }

    @Operation(summary = "Crear una categoría")
    @PostMapping
    public ResponseEntity<Categoria> crear(@RequestBody Categoria categoria) {
        return ResponseEntity.status(HttpStatus.CREATED).body(categoriaService.crear(categoria));
    }

    @Operation(summary = "Actualizar una categoría por su ID")
    @PutMapping("/{id}")
    public ResponseEntity<Categoria> actualizar(@PathVariable Long id, @RequestBody Categoria categoria) {
        return ResponseEntity.ok(categoriaService.actualizar(id, categoria));
    }

    @Operation(summary = "Eliminar una categoría sin libros asociados")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        categoriaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    // Método personalizado: buscar categoría por nombre
    @Operation(summary = "Buscar una categoría por su nombre")
    @GetMapping("/nombre/{nombre}")
    public ResponseEntity<Categoria> obtenerPorNombre(@PathVariable String nombre) {
        return ResponseEntity.ok(categoriaService.obtenerPorNombre(nombre));
    }
}
