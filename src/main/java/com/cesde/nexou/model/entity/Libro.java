package com.cesde.nexou.model.entity;

import com.cesde.nexou.model.base.BaseEntity;
import com.cesde.nexou.model.embeddable.Ubicacion;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "libros")
@Getter
@Setter
@NoArgsConstructor
public class Libro extends BaseEntity {

    @Size(max = 150, message = "no puede superar los 150 caracteres")
    @Column(name = "nom_libro", nullable = false, length = 150)
    private String nomLibro;

    @Size(max = 100, message = "no puede superar los 100 caracteres")
    @Column(length = 100)
    private String autor;

    @Size(max = 100, message = "no puede superar los 100 caracteres")
    @Column(length = 100)
    private String editorial;

    @Size(max = 20, message = "no puede superar los 20 caracteres")
    @Column(nullable = false, unique = true, length = 20)
    private String isbn;

    @Column(name = "cantidad_total")
    private Integer cantidadTotal;

    @Column(name = "cantidad_disponible")
    private Integer cantidadDisponible;

    @Column(name = "dias_prestamo_max")
    private Integer diasPrestamoMax;

    @Valid
    @Embedded
    private Ubicacion ubicacionFisica;

    @ManyToMany
    @JsonIgnoreProperties("libros")
    @JoinTable(
        name = "libro_categoria",
        joinColumns = @JoinColumn(name = "libro_id"),
        inverseJoinColumns = @JoinColumn(name = "categoria_id")
    )
    private Set<Categoria> categorias = new HashSet<>();
}