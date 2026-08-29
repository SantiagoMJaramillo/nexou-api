package com.cesde.nexou.model.embeddable;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
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

    @Column(length = 50)
    private String sede;

    @Column(length = 10)
    private String piso;

    @Column(length = 100)
    private String referencia;
}
