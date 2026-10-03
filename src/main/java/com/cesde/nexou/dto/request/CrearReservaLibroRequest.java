package com.cesde.nexou.dto.request;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CrearReservaLibroRequest {
    @NotNull @Positive private Long usuarioId;
    @NotNull @Positive private Long libroId;
    @NotNull @Positive private Integer diasPrestamo;
}
