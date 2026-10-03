package com.cesde.nexou.dto.response;
import com.cesde.nexou.model.entity.ReservaEquipo;
import com.cesde.nexou.model.enums.EstadoReserva;
import java.time.LocalDateTime;

public record ReservaEquipoResponse(Long id, Long usuarioId, Long equipoId, String nomEquipo, EstadoReserva estadoReserva,
        String lugarEntrega, String proposito, LocalDateTime horaInicio, LocalDateTime horaFin) {
    public static ReservaEquipoResponse desde(ReservaEquipo reserva) {
        return new ReservaEquipoResponse(reserva.getId(), reserva.getUsuario().getId(), reserva.getEquipo().getId(),
                reserva.getEquipo().getNomEquipo(), reserva.getEstadoReserva(), reserva.getLugarEntrega(),
                reserva.getProposito(), reserva.getHoraInicio(), reserva.getHoraFin());
    }
}
