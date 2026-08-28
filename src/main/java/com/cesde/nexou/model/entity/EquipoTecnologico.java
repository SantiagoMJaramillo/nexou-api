package com.cesde.nexou.model.entity;

import com.cesde.nexou.model.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
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

    @Column(name = "nom_equipo", nullable = false, length = 100)
    private String nomEquipo;

    @Column(length = 50)
    private String marca;

    @Column(length = 50)
    private String modelo;

    @Column(name = "tipo_equipo", length = 50)
    private String tipoEquipo;

    @Column(name = "cantidad_total")
    private Integer cantidadTotal;

    @Column(name = "cantidad_disponible")
    private Integer cantidadDisponible;

    @Column(name = "duracion_maxima_hrs", precision = 5)
    private Double duracionMaximaHrs;

    @Column(name = "estado_equipo", length = 30)
    private String estadoEquipo;
}