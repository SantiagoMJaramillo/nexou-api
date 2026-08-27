package com.cesde.nexou.model.entity;

import com.cesde.nexou.model.base.BaseEntity;
import com.cesde.nexou.model.enums.EstadoReserva;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "ReservaLibro")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReservaLibro extends BaseEntity {

    @Column(name = "tipo_prestamo", length = 30)
    private String tipoPrestamo;

    @Column(length = 150)
    private String proposito;

    @Column(name = "fecha_entrega_esperada")
    private LocalDate fechaEntregaEsperada;

    @Column(name = "fecha_devolucion_real")
    private LocalDate fechaDevolucionReal;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_reserva", nullable = false, length = 20)
    private EstadoReserva estadoReserva;

    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @ManyToOne
    @JoinColumn(name = "libro_id", nullable = false)
    private Libro libro;
}