package com.cesde.nexou.dto.response;
import com.cesde.nexou.model.entity.ReservaEquipo;
import com.cesde.nexou.model.enums.EstadoReserva;
import java.time.LocalDateTime;

public record ReservaEquipoResponse(Long id, Long usuarioId, Long equipoId, EstadoReserva estadoReserva, LocalDateTime horaInicio, LocalDateTime horaFin) {
    public static ReservaEquipoResponse desde(ReservaEquipo reserva) {
        return new ReservaEquipoResponse(reserva.getId(), reserva.getUsuario().getId(), reserva.getEquipo().getId(), reserva.getEstadoReserva(), reserva.getHoraInicio(), reserva.getHoraFin());
    }
}