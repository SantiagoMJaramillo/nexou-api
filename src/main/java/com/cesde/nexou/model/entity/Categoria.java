package com.cesde.nexou.model.entity;

import com.cesde.nexou.model.base.BaseEntity;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "categorias")
@Getter
@Setter
@NoArgsConstructor
public class Categoria extends BaseEntity {

    @Size(max = 80, message = "no puede superar los 80 caracteres")
    @Column(nullable = false, unique = true, length = 80)
    private String nombre;

    @Size(max = 255, message = "no puede superar los 255 caracteres")
    @Column(length = 255)
    private String descripcion;

    @ManyToMany(mappedBy = "categorias")
    @JsonIgnoreProperties("categorias")
    private Set<Libro> libros = new HashSet<>();
}