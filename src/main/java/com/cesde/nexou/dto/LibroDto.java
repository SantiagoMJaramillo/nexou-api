package com.cesde.nexou.dto;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.cesde.nexou.model.embeddable.Ubicacion;
import com.cesde.nexou.model.entity.Categoria;
import com.cesde.nexou.model.entity.Libro;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public final class LibroDto {

    private LibroDto() {
    }

    public record UbicacionDto(
            @Size(max = 50, message = "no puede superar los 50 caracteres") String sede,
            @Size(max = 10, message = "no puede superar los 10 caracteres") String piso,
            @Size(max = 100, message = "no puede superar los 100 caracteres") String referencia) {
    }

    public record Request(
            @NotBlank(message = "el nombre es obligatorio")
            @Size(max = 150, message = "no puede superar los 150 caracteres") String nomLibro,
            @Size(max = 100, message = "no puede superar los 100 caracteres") String autor,
            @Size(max = 100, message = "no puede superar los 100 caracteres") String editorial,
            @NotBlank(message = "el ISBN es obligatorio")
            @Size(max = 20, message = "no puede superar los 20 caracteres") String isbn,
            @NotNull(message = "la cantidad total es obligatoria")
            @Positive(message = "debe ser mayor a 0") Integer cantidadTotal,
            @PositiveOrZero(message = "no puede ser negativa") Integer cantidadDisponible,
            @NotNull(message = "los días máximos de préstamo son obligatorios")
            @Positive(message = "debe ser mayor a 0") Integer diasPrestamoMax,
            @Valid UbicacionDto ubicacion,
            Set<Long> categoriaIds,
            Boolean estadoActivo) {

        public Libro aEntidad() {
            Libro l = new Libro();
            l.setNomLibro(nomLibro == null ? null : nomLibro.trim());
            l.setAutor(autor);
            l.setEditorial(editorial);
            l.setIsbn(isbn == null ? null : isbn.trim());
            l.setCantidadTotal(cantidadTotal);
            l.setCantidadDisponible(cantidadDisponible);
            l.setDiasPrestamoMax(diasPrestamoMax);
            l.setEstadoActivo(estadoActivo);
            if (ubicacion != null) {
                l.setUbicacionFisica(new Ubicacion(ubicacion.sede(), ubicacion.piso(), ubicacion.referencia()));
            }
            // El servicio reemplaza estas categorías (solo con ID) por las reales de la base de datos
            Set<Categoria> categorias = new HashSet<>();
            if (categoriaIds != null) {
                for (Long id : categoriaIds) {
                    Categoria c = new Categoria();
                    c.setId(id);
                    categorias.add(c);
                }
            }
            l.setCategorias(categorias);
            return l;
        }
    }

    public record Response(
            Long id,
            String nomLibro,
            String autor,
            String editorial,
            String isbn,
            Integer cantidadTotal,
            Integer cantidadDisponible,
            Integer diasPrestamoMax,
            UbicacionDto ubicacion,
            List<CategoriaDto.Response> categorias,
            Boolean estadoActivo,
            String imagenUrl) {

        public static Response de(Libro l) {
            Ubicacion u = l.getUbicacionFisica();
            UbicacionDto ubicacion = u == null ? null : new UbicacionDto(u.getSede(), u.getPiso(), u.getReferencia());
            List<CategoriaDto.Response> categorias = l.getCategorias().stream()
                    .map(CategoriaDto.Response::de)
                    .sorted(Comparator.comparing(CategoriaDto.Response::nombre))
                    .toList();
            return new Response(l.getId(), l.getNomLibro(), l.getAutor(), l.getEditorial(), l.getIsbn(),
                    l.getCantidadTotal(), l.getCantidadDisponible(), l.getDiasPrestamoMax(),
                    ubicacion, categorias, l.getEstadoActivo(),
                    l.getImagenVersion() == null ? null : "/api/libros/" + l.getId() + "/imagen?v=" + l.getImagenVersion());
        }
    }
}
