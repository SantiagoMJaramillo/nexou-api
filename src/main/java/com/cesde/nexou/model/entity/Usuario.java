package com.cesde.nexou.model.entity;

import com.cesde.nexou.model.base.BaseEntity;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "usuarios")
@Getter
@Setter
@NoArgsConstructor
public class Usuario extends BaseEntity {

    @Size(max = 100, message = "no puede superar los 100 caracteres")
    @Column(name = "nombre_usuario", nullable = false, length = 100)
    private String nombreUsuario;

    @Size(max = 150, message = "no puede superar los 150 caracteres")
    @Column(nullable = false, unique = true, length = 150)
    private String correo;

    // Solo se recibe en las peticiones; nunca se devuelve en las respuestas JSON
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Size(max = 255, message = "no puede superar los 255 caracteres")
    @Column(nullable = false, length = 255)
    private String contrasena;

    @Size(max = 20, message = "no puede superar los 20 caracteres")
    @Column(name = "codigo_estudiante", length = 20)
    private String codigoEstudiante;

    @Size(max = 100, message = "no puede superar los 100 caracteres")
    @Column(length = 100)
    private String programa;

    private Integer semestre;

    @Size(max = 20, message = "no puede superar los 20 caracteres")
    @Column(length = 20)
    private String telefono;

    @OneToOne(mappedBy = "usuario", cascade = CascadeType.ALL)
    @JsonIgnoreProperties("usuario")
    private ConfiguracionUsuario configuracionUsuario;
}