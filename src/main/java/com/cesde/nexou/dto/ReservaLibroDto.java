package com.cesde.nexou.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.cesde.nexou.model.entity.Libro;
import com.cesde.nexou.model.entity.ReservaLibro;
import com.cesde.nexou.model.entity.Usuario;
import com.cesde.nexou.model.enums.EstadoReserva;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public final class ReservaLibroDto {

    private ReservaLibroDto() {
    }

    /** usuarioId solo lo respeta el ADMIN; un usuario normal siempre reserva a su nombre. */
    public record Request(
            @NotNull(message = "el libro es obligatorio") Long libroId,
            @NotNull(message = "los días de préstamo son obligatorios")
            @Positive(message = "debe ser mayor a 0") Integer diasPrestamo,
            @Size(max = 30, message = "no puede superar los 30 caracteres") String tipoPrestamo,
            @Size(max = 150, message = "no puede superar los 150 caracteres") String proposito,
            Long usuarioId) {

        public ReservaLibro aEntidad(Long usuarioIdFinal) {
            ReservaLibro r = new ReservaLibro();
            Usuario u = new Usuario();
            u.setId(usuarioIdFinal);
            Libro l = new Libro();
            l.setId(libroId);
            r.setUsuario(u);
            r.setLibro(l);
            r.setDiasPrestamo(diasPrestamo);
            r.setTipoPrestamo(tipoPrestamo);
            r.setProposito(proposito);
            return r;
        }
    }

    public record Edicion(
            @Size(max = 30, message = "no puede superar los 30 caracteres") String tipoPrestamo,
            @Size(max = 150, message = "no puede superar los 150 caracteres") String proposito) {

        public ReservaLibro aEntidad() {
            ReservaLibro r = new ReservaLibro();
            r.setTipoPrestamo(tipoPrestamo);
            r.setProposito(proposito);
            return r;
        }
    }

    public record Response(
            Long id,
            Long usuarioId,
            String usuario,
            Long libroId,
            String libro,
            String tipoPrestamo,
            String proposito,
            LocalDate fechaEntregaEsperada,
            LocalDate fechaDevolucionReal,
            EstadoReserva estadoReserva,
            LocalDateTime fechaCreacion,
            Integer usuarioSemestre,
            String usuarioPrograma) {

        public static Response de(ReservaLibro r) {
            return new Response(r.getId(), r.getUsuario().getId(), r.getUsuario().getNombreUsuario(),
                    r.getLibro().getId(), r.getLibro().getNomLibro(), r.getTipoPrestamo(), r.getProposito(),
                    r.getFechaEntregaEsperada(), r.getFechaDevolucionReal(), r.getEstadoReserva(),
                    r.getFechaCreacion(), r.getUsuario().getSemestre(), r.getUsuario().getPrograma());
        }
    }
}
