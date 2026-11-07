package com.cesde.nexou.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.cesde.nexou.model.entity.EquipoTecnologico;
import com.cesde.nexou.model.entity.ReservaEquipo;
import com.cesde.nexou.model.entity.Usuario;
import com.cesde.nexou.model.enums.EstadoReserva;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public final class ReservaEquipoDto {

    private ReservaEquipoDto() {
    }

    /** usuarioId solo lo respeta el ADMIN; un usuario normal siempre reserva a su nombre. */
    public record Request(
            @NotNull(message = "el equipo es obligatorio") Long equipoId,
            @NotNull(message = "la hora de inicio es obligatoria") LocalDateTime horaInicio,
            @NotNull(message = "la hora de fin es obligatoria") LocalDateTime horaFin,
            @Size(max = 100, message = "no puede superar los 100 caracteres") String lugarEntrega,
            @Size(max = 150, message = "no puede superar los 150 caracteres") String proposito,
            Long usuarioId) {

        public ReservaEquipo aEntidad(Long usuarioIdFinal) {
            ReservaEquipo r = new ReservaEquipo();
            Usuario u = new Usuario();
            u.setId(usuarioIdFinal);
            EquipoTecnologico e = new EquipoTecnologico();
            e.setId(equipoId);
            r.setUsuario(u);
            r.setEquipo(e);
            r.setHoraInicio(horaInicio);
            r.setHoraFin(horaFin);
            r.setLugarEntrega(lugarEntrega);
            r.setProposito(proposito);
            return r;
        }
    }

    public record Edicion(
            @Size(max = 100, message = "no puede superar los 100 caracteres") String lugarEntrega,
            @Size(max = 150, message = "no puede superar los 150 caracteres") String proposito) {

        public ReservaEquipo aEntidad() {
            ReservaEquipo r = new ReservaEquipo();
            r.setLugarEntrega(lugarEntrega);
            r.setProposito(proposito);
            return r;
        }
    }

    public record Response(
            Long id,
            Long usuarioId,
            String usuario,
            Long equipoId,
            String equipo,
            LocalDateTime horaInicio,
            LocalDateTime horaFin,
            String lugarEntrega,
            String proposito,
            LocalDate fechaEntregaEsperada,
            EstadoReserva estadoReserva,
            LocalDateTime fechaCreacion,
            LocalDateTime fechaDevolucionReal,
            Integer usuarioSemestre,
            String usuarioPrograma) {

        public static Response de(ReservaEquipo r) {
            return new Response(r.getId(), r.getUsuario().getId(), r.getUsuario().getNombreUsuario(),
                    r.getEquipo().getId(), r.getEquipo().getNomEquipo(), r.getHoraInicio(), r.getHoraFin(),
                    r.getLugarEntrega(), r.getProposito(), r.getFechaEntregaEsperada(), r.getEstadoReserva(),
                    r.getFechaCreacion(), r.getFechaDevolucionReal(),
                    r.getUsuario().getSemestre(), r.getUsuario().getPrograma());
        }
    }
}
