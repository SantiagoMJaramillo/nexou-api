package com.cesde.nexou.dto.response;
import com.cesde.nexou.model.entity.ReservaLibro;
import com.cesde.nexou.model.enums.EstadoReserva;
import java.time.LocalDate;

public record ReservaLibroResponse(Long id, Long usuarioId, Long libroId, String nomLibro, EstadoReserva estadoReserva,
        String tipoPrestamo, String proposito, LocalDate fechaEntregaEsperada, LocalDate fechaDevolucionReal) {
    public static ReservaLibroResponse desde(ReservaLibro reserva) {
        return new ReservaLibroResponse(reserva.getId(), reserva.getUsuario().getId(), reserva.getLibro().getId(),
                reserva.getLibro().getNomLibro(), reserva.getEstadoReserva(), reserva.getTipoPrestamo(),
                reserva.getProposito(), reserva.getFechaEntregaEsperada(), reserva.getFechaDevolucionReal());
    }
}
