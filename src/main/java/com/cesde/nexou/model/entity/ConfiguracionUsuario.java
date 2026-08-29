package com.cesde.nexou.model.entity;

import com.cesde.nexou.model.base.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "configuracion_usuario")
@Getter
@Setter
@NoArgsConstructor
public class ConfiguracionUsuario extends BaseEntity {

    @Column(length = 20)
    private String idioma;

    @Column(length = 20)
    private String tema;

    @Column(name = "notificaciones_activas")
    private Boolean notificacionesActivas = true;

    @OneToOne
    @JoinColumn(name = "usuario_id", nullable = false, unique = true)
    private Usuario usuario;
}