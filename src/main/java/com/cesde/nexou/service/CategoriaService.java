package com.cesde.nexou.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cesde.nexou.exception.RecursoNoEncontradoException;
import com.cesde.nexou.exception.ReglaDeNegocioException;
import com.cesde.nexou.model.entity.Categoria;
import com.cesde.nexou.repository.CategoriaRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;

    public List<Categoria> obtenerTodos() {
        return categoriaRepository.findAll();
    }

    public Categoria obtenerPorId(Long id) {
        return categoriaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Categoría no encontrada con id: " + id));
    }

    @Transactional
    public Categoria crear(Categoria categoria) {
        validarNombre(categoria);
        // Regla de negocio: el nombre de la categoría es único
        if (categoriaRepository.findByNombreIgnoreCase(categoria.getNombre()).isPresent()) {
            throw new ReglaDeNegocioException("Ya existe una categoría con el nombre: " + categoria.getNombre());
        }
        return categoriaRepository.save(categoria);
    }

    @Transactional
    public Categoria actualizar(Long id, Categoria datos) {
        Categoria categoria = obtenerPorId(id);
        validarNombre(datos);
        if (!datos.getNombre().equalsIgnoreCase(categoria.getNombre())
                && categoriaRepository.findByNombreIgnoreCase(datos.getNombre()).isPresent()) {
            throw new ReglaDeNegocioException("El nombre " + datos.getNombre() + " ya está en uso por otra categoría");
        }
        categoria.setNombre(datos.getNombre());
        categoria.setDescripcion(datos.getDescripcion());
        return categoriaRepository.save(categoria);
    }

    @Transactional
    public void eliminar(Long id) {
        Categoria categoria = obtenerPorId(id);
        // Regla de negocio: no se elimina una categoría que tiene libros asociados
        if (!categoria.getLibros().isEmpty()) {
            throw new ReglaDeNegocioException("No se puede eliminar la categoría porque tiene " + categoria.getLibros().size() + " libro(s) asociados");
        }
        categoriaRepository.delete(categoria);
    }

    // Uso del método personalizado del repositorio
    public Categoria obtenerPorNombre(String nombre) {
        return categoriaRepository.findByNombreIgnoreCase(nombre)
                .orElseThrow(() -> new RecursoNoEncontradoException("Categoría no encontrada con nombre: " + nombre));
    }

    private void validarNombre(Categoria categoria) {
        if (categoria.getNombre() == null || categoria.getNombre().isBlank()) {
            throw new ReglaDeNegocioException("El nombre de la categoría es obligatorio");
        }
    }
}
