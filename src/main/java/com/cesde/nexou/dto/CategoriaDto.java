package com.cesde.nexou.dto;

import com.cesde.nexou.model.entity.Categoria;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public final class CategoriaDto {

    private CategoriaDto() {
    }

    public record Request(
            @NotBlank(message = "el nombre es obligatorio")
            @Size(max = 80, message = "no puede superar los 80 caracteres") String nombre,
            @Size(max = 255, message = "no puede superar los 255 caracteres") String descripcion) {

        public Categoria aEntidad() {
            Categoria c = new Categoria();
            c.setNombre(nombre == null ? null : nombre.trim());
            c.setDescripcion(descripcion);
            return c;
        }
    }

    public record Response(Long id, String nombre, String descripcion) {

        public static Response de(Categoria c) {
            return new Response(c.getId(), c.getNombre(), c.getDescripcion());
        }
    }
}
