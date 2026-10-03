package com.cesde.nexou.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * Datos editables de una reserva de equipo. El estado, las horas y el stock
 * solo cambian con las acciones de negocio (crear, devolver).
 */
@Getter
@Setter
public class ActualizarReservaEquipoRequest {
    @Size(max = 100) private String lugarEntrega;
    @Size(max = 150) private String proposito;
}
