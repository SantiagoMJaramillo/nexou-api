package com.cesde.nexou.dto.request;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
public class CrearReservaEquipoRequest {
    @NotNull @Positive private Long usuarioId;
    @NotNull @Positive private Long equipoId;
    @NotNull private LocalDateTime horaInicio;
    @NotNull private LocalDateTime horaFin;
}