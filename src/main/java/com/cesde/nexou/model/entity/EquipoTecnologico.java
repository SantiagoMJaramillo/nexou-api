package com.cesde.nexou.model.entity;

import com.cesde.nexou.model.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "EquipoTecnologico")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EquipoTecnologico extends BaseEntity {

    @Size(max = 100, message = "no puede superar los 100 caracteres")
    @Column(name = "nom_equipo", nullable = false, length = 100)
    private String nomEquipo;

    @Size(max = 50, message = "no puede superar los 50 caracteres")
    @Column(length = 50)
    private String marca;

    @Size(max = 50, message = "no puede superar los 50 caracteres")
    @Column(length = 50)
    private String modelo;

    @Size(max = 50, message = "no puede superar los 50 caracteres")
    @Column(name = "tipo_equipo", length = 50)
    private String tipoEquipo;

    @Column(name = "cantidad_total")
    private Integer cantidadTotal;

    @Column(name = "cantidad_disponible")
    private Integer cantidadDisponible;

    @Column(name = "duracion_maxima_hrs", precision = 5)
    private Double duracionMaximaHrs;

    @Size(max = 30, message = "no puede superar los 30 caracteres")
    @Column(name = "estado_equipo", length = 30)
    private String estadoEquipo;
}