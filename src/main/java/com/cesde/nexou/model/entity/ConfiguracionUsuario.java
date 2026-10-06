package com.cesde.nexou.model.entity;

import com.cesde.nexou.model.base.BaseEntity;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "configuracion_usuario")
@Getter
@Setter
@NoArgsConstructor
public class ConfiguracionUsuario extends BaseEntity {

    @Size(max = 20, message = "no puede superar los 20 caracteres")
    @Column(length = 20)
    private String idioma;

    @Size(max = 20, message = "no puede superar los 20 caracteres")
    @Column(length = 20)
    private String tema;

    @Column(name = "notificaciones_activas")
    private Boolean notificacionesActivas = true;

    @OneToOne
    @JsonIgnoreProperties("configuracionUsuario")
    @JoinColumn(name = "usuario_id", nullable = false, unique = true)
    private Usuario usuario;
}