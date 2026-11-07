package com.cesde.nexou.dto;

import com.cesde.nexou.model.entity.EquipoTecnologico;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public final class EquipoDto {

    private EquipoDto() {
    }

    public record Request(
            @NotBlank(message = "el nombre es obligatorio")
            @Size(max = 100, message = "no puede superar los 100 caracteres") String nomEquipo,
            @Size(max = 50, message = "no puede superar los 50 caracteres") String marca,
            @Size(max = 50, message = "no puede superar los 50 caracteres") String modelo,
            @Size(max = 50, message = "no puede superar los 50 caracteres") String tipoEquipo,
            @NotNull(message = "la cantidad total es obligatoria")
            @Positive(message = "debe ser mayor a 0") Integer cantidadTotal,
            @PositiveOrZero(message = "no puede ser negativa") Integer cantidadDisponible,
            @NotNull(message = "la duración máxima es obligatoria")
            @Positive(message = "debe ser mayor a 0") Double duracionMaximaHrs,
            @Size(max = 30, message = "no puede superar los 30 caracteres") String estadoEquipo,
            Boolean estadoActivo) {

        public EquipoTecnologico aEntidad() {
            EquipoTecnologico e = new EquipoTecnologico();
            e.setNomEquipo(nomEquipo == null ? null : nomEquipo.trim());
            e.setMarca(marca);
            e.setModelo(modelo);
            e.setTipoEquipo(tipoEquipo);
            e.setCantidadTotal(cantidadTotal);
            e.setCantidadDisponible(cantidadDisponible);
            e.setDuracionMaximaHrs(duracionMaximaHrs);
            e.setEstadoEquipo(estadoEquipo);
            e.setEstadoActivo(estadoActivo);
            return e;
        }
    }

    public record Response(
            Long id,
            String nomEquipo,
            String marca,
            String modelo,
            String tipoEquipo,
            Integer cantidadTotal,
            Integer cantidadDisponible,
            Double duracionMaximaHrs,
            String estadoEquipo,
            Boolean estadoActivo,
            String imagenUrl) {

        public static Response de(EquipoTecnologico e) {
            return new Response(e.getId(), e.getNomEquipo(), e.getMarca(), e.getModelo(), e.getTipoEquipo(),
                    e.getCantidadTotal(), e.getCantidadDisponible(), e.getDuracionMaximaHrs(),
                    e.getEstadoEquipo(), e.getEstadoActivo(),
                    e.getImagenVersion() == null ? null : "/api/equipos/" + e.getId() + "/imagen?v=" + e.getImagenVersion());
        }
    }
}
