package com.cesde.nexou.model.entity;

import com.cesde.nexou.model.base.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * Imagen de un libro o de un equipo. Vive en su propia tabla (y no como columna de Libro o
 * EquipoTecnologico) para que los listados no arrastren los bytes de las fotos: solo se leen
 * cuando alguien pide la imagen. Se guarda en la base de datos porque el disco de Render es efímero.
 */
@Entity
@Table(name = "imagenes_articulo",
        uniqueConstraints = @UniqueConstraint(columnNames = {"tipo", "articulo_id"}))
@Getter
@Setter
@NoArgsConstructor
public class ImagenArticulo extends BaseEntity {

    /** "LIBRO" o "EQUIPO". */
    @Column(nullable = false, length = 10)
    private String tipo;

    @Column(name = "articulo_id", nullable = false)
    private Long articuloId;

    @Column(name = "tipo_contenido", nullable = false, length = 50)
    private String tipoContenido;

    // VARBINARY explícito: en PostgreSQL es bytea (sin límite práctico) y evita que Hibernate elija BLOB/oid
    @JdbcTypeCode(SqlTypes.VARBINARY)
    @Column(nullable = false, length = 1_000_000)
    private byte[] datos;
}
