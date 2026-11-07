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

import com.cesde.nexou.dto.LibroDto;
import com.cesde.nexou.service.LibroService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "Libros", description = "Catálogo de libros (lectura: todos; escritura: ADMIN)")
@RestController
@RequestMapping("/api/libros")
@RequiredArgsConstructor
public class LibroController {

    private final ImagenService imagenService;
    private final LibroService libroService;

    @Operation(summary = "Listar todos los libros")
    @GetMapping
    public List<LibroDto.Response> obtenerTodos() {
        return libroService.obtenerTodos().stream().map(LibroDto.Response::de).toList();
    }

    @Operation(summary = "Obtener un libro por su ID")
    @GetMapping("/{id}")
    public LibroDto.Response obtenerPorId(@PathVariable Long id) {
        return LibroDto.Response.de(libroService.obtenerPorId(id));
    }

    @Operation(summary = "Publicar un libro para préstamo", description = "Las categorías se envían por ID: \"categoriaIds\": [1, 2]")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<LibroDto.Response> crear(@Valid @RequestBody LibroDto.Request datos) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(LibroDto.Response.de(libroService.crear(datos.aEntidad())));
    }

    @Operation(summary = "Actualizar un libro por su ID")
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public LibroDto.Response actualizar(@PathVariable Long id, @Valid @RequestBody LibroDto.Request datos) {
        return LibroDto.Response.de(libroService.actualizar(id, datos.aEntidad()));
    }

    @Operation(summary = "Eliminar un libro sin reservas registradas")
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        libroService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Buscar un libro por su ISBN")
    @GetMapping("/isbn/{isbn}")
    public LibroDto.Response obtenerPorIsbn(@PathVariable String isbn) {
        return LibroDto.Response.de(libroService.obtenerPorIsbn(isbn));
    }

    @Operation(summary = "Subir o reemplazar la imagen (libro)", description = "Multipart con el campo 'archivo' (JPG, PNG o WEBP, máx. 1 MB)")
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping(value = "/{id}/imagen", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public LibroDto.Response subirImagen(@PathVariable Long id, @RequestParam("archivo") MultipartFile archivo) {
        imagenService.guardarDeLibro(id, archivo);
        return LibroDto.Response.de(libroService.obtenerPorId(id));
    }

    @Operation(summary = "Quitar la imagen (libro)")
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}/imagen")
    public ResponseEntity<Void> quitarImagen(@PathVariable Long id) {
        imagenService.quitarDeLibro(id);
        return ResponseEntity.noContent().build();
    }

    // Pública: un <img> no puede enviar el token. La URL lleva ?v=<versión>, por eso el caché puede ser largo.
    @Operation(summary = "Descargar la imagen (libro)")
    @GetMapping("/{id}/imagen")
    public ResponseEntity<byte[]> obtenerImagen(@PathVariable Long id) {
        ImagenArticulo imagen = imagenService.obtener(ImagenService.LIBRO, id);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(imagen.getTipoContenido()))
                .cacheControl(CacheControl.maxAge(Duration.ofDays(365)).cachePublic().immutable())
                .header("X-Content-Type-Options", "nosniff")
                .body(imagen.getDatos());
    }
}
