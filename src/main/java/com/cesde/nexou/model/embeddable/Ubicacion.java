package com.cesde.nexou.model.embeddable;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Ubicacion {

    @Size(max = 50, message = "no puede superar los 50 caracteres")
    @Column(length = 50)
    private String sede;

    @Size(max = 10, message = "no puede superar los 10 caracteres")
    @Column(length = 10)
    private String piso;

    @Size(max = 100, message = "no puede superar los 100 caracteres")
    @Column(length = 100)
    private String referencia;
}
