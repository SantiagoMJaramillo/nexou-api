package com.cesde.nexou.service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cesde.nexou.exception.RecursoNoEncontradoException;
import com.cesde.nexou.exception.ReglaDeNegocioException;
import com.cesde.nexou.model.entity.Categoria;
import com.cesde.nexou.model.entity.Libro;
import com.cesde.nexou.repository.CategoriaRepository;
import com.cesde.nexou.repository.LibroRepository;
import com.cesde.nexou.repository.ReservaLibroRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LibroService {

    private final LibroRepository libroRepository;
    private final CategoriaRepository categoriaRepository;
    private final ReservaLibroRepository reservaLibroRepository;

    public List<Libro> obtenerTodos() {
        return libroRepository.findAll();
    }

    public Libro obtenerPorId(Long id) {
        return libroRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Libro no encontrado con id: " + id));
    }

    @Transactional
    public Libro crear(Libro libro) {
        validarDatos(libro);
        // Regla de negocio: el ISBN identifica al libro y no puede repetirse
        if (libroRepository.findByIsbn(libro.getIsbn()).isPresent()) {
            throw new ReglaDeNegocioException("Ya existe un libro con el ISBN: " + libro.getIsbn());
        }
        // Si no se indica la cantidad disponible, todos los ejemplares inician disponibles
        if (libro.getCantidadDisponible() == null) {
            libro.setCantidadDisponible(libro.getCantidadTotal());
        }
        validarInventario(libro);
        libro.setCategorias(resolverCategorias(libro.getCategorias()));
        // Un POST siempre crea un registro nuevo: se ignora cualquier id recibido en el body
        libro.setId(null);
        return libroRepository.save(libro);
    }

    @Transactional
    public Libro actualizar(Long id, Libro datos) {
        Libro libro = obtenerPorId(id);
        validarDatos(datos);
        if (!datos.getIsbn().equalsIgnoreCase(libro.getIsbn())
                && libroRepository.findByIsbn(datos.getIsbn()).isPresent()) {
            throw new ReglaDeNegocioException("El ISBN " + datos.getIsbn() + " ya está en uso por otro libro");
        }

        libro.setNomLibro(datos.getNomLibro());
        libro.setAutor(datos.getAutor());
        libro.setEditorial(datos.getEditorial());
        libro.setIsbn(datos.getIsbn());
        libro.setCantidadTotal(datos.getCantidadTotal());
        if (datos.getCantidadDisponible() != null) {
            libro.setCantidadDisponible(datos.getCantidadDisponible());
        }
        libro.setDiasPrestamoMax(datos.getDiasPrestamoMax());
        libro.setUbicacionFisica(datos.getUbicacionFisica());
        validarInventario(libro);

        // Las categorías solo se reemplazan si se envían en el JSON
        if (datos.getCategorias() != null && !datos.getCategorias().isEmpty()) {
            libro.setCategorias(resolverCategorias(datos.getCategorias()));
        }
        return libroRepository.save(libro);
    }

    @Transactional
    public void eliminar(Long id) {
        Libro libro = obtenerPorId(id);
        // Regla de negocio: no se elimina un libro con historial de préstamos
        if (reservaLibroRepository.existsByLibroId(id)) {
            throw new ReglaDeNegocioException("No se puede eliminar el libro porque tiene reservas registradas. Desactívelo (estadoActivo = false)");
        }
        libroRepository.delete(libro);
    }

    // Uso del método personalizado del repositorio
    public Libro obtenerPorIsbn(String isbn) {
        return libroRepository.findByIsbn(isbn)
                .orElseThrow(() -> new RecursoNoEncontradoException("Libro no encontrado con ISBN: " + isbn));
    }

    private void validarDatos(Libro libro) {
        if (libro.getNomLibro() == null || libro.getNomLibro().isBlank()) {
            throw new ReglaDeNegocioException("El nombre del libro es obligatorio");
        }
        if (libro.getIsbn() == null || libro.getIsbn().isBlank()) {
            throw new ReglaDeNegocioException("El ISBN del libro es obligatorio");
        }
        if (libro.getCantidadTotal() == null || libro.getCantidadTotal() <= 0) {
            throw new ReglaDeNegocioException("La cantidad total de ejemplares debe ser mayor a 0");
        }
        if (libro.getDiasPrestamoMax() == null || libro.getDiasPrestamoMax() <= 0) {
            throw new ReglaDeNegocioException("Los días máximos de préstamo deben ser mayores a 0");
        }
    }

    private void validarInventario(Libro libro) {
        if (libro.getCantidadDisponible() < 0 || libro.getCantidadDisponible() > libro.getCantidadTotal()) {
            throw new ReglaDeNegocioException("La cantidad disponible debe estar entre 0 y la cantidad total (" + libro.getCantidadTotal() + ")");
        }
    }

    // Convierte [{ "id": 1 }, ...] en categorías reales de la base de datos
    private Set<Categoria> resolverCategorias(Set<Categoria> categorias) {
        Set<Categoria> resultado = new HashSet<>();
        if (categorias == null) {
            return resultado;
        }
        for (Categoria c : categorias) {
            if (c.getId() == null) {
                throw new ReglaDeNegocioException("Cada categoría debe enviarse con su ID");
            }
            resultado.add(categoriaRepository.findById(c.getId())
                    .orElseThrow(() -> new RecursoNoEncontradoException("Categoría no encontrada con id: " + c.getId())));
        }
        return resultado;
    }
}
