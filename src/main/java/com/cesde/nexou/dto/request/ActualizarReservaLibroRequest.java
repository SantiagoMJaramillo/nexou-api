package com.cesde.nexou.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * Datos editables de una reserva de libro. El estado, las fechas y el stock
 * solo cambian con las acciones de negocio (crear, renovar, devolver).
 */
@Getter
@Setter
public class ActualizarReservaLibroRequest {
    @Size(max = 30) private String tipoPrestamo;
    @Size(max = 150) private String proposito;
}
