package com.cesde.nexou.model.entity;

import com.cesde.nexou.model.base.BaseEntity;
import com.cesde.nexou.model.embeddable.Ubicacion;
import jakarta.persistence.*;
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

    @Column(name = "nom_libro", nullable = false, length = 150)
    private String nomLibro;

    @Column(length = 100)
    private String autor;

    @Column(length = 100)
    private String editorial;

    @Column(nullable = false, unique = true, length = 20)
    private String isbn;

    @Column(name = "cantidad_total")
    private Integer cantidadTotal;

    @Column(name = "cantidad_disponible")
    private Integer cantidadDisponible;

    @Column(name = "dias_prestamo_max")
    private Integer diasPrestamoMax;

    @Embedded
    private Ubicacion ubicacionFisica;

    @ManyToMany
    @JoinTable(
        name = "libro_categoria",
        joinColumns = @JoinColumn(name = "libro_id"),
        inverseJoinColumns = @JoinColumn(name = "categoria_id")
    )
    private Set<Categoria> categorias = new HashSet<>();
}