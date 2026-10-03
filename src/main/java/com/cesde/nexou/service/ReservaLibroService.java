package com.cesde.nexou.service;
import com.cesde.nexou.dto.request.CrearReservaLibroRequest;
import com.cesde.nexou.dto.response.ReservaLibroResponse;
import com.cesde.nexou.exception.RecursoNoEncontradoException;
import com.cesde.nexou.exception.ReglaDeNegocioException;
import com.cesde.nexou.model.entity.*;
import com.cesde.nexou.model.enums.EstadoReserva;
import com.cesde.nexou.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class ReservaLibroService {
    private final UsuarioRepository usuarioRepository;
    private final LibroRepository libroRepository;
    private final ReservaLibroRepository reservaLibroRepository;

    @Transactional
    public ReservaLibroResponse crear(CrearReservaLibroRequest request) {
        Usuario usuario = usuarioRepository.findByIdAndEstadoActivoTrue(request.getUsuarioId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario inactivo o no encontrado con id: " + request.getUsuarioId()));
        Libro libro = libroRepository.findById(request.getLibroId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Libro no encontrado con id: " + request.getLibroId()));

        if (libro.getCantidadDisponible() == null || libro.getCantidadDisponible() <= 0) {
            throw new ReglaDeNegocioException("El libro '" + libro.getNomLibro() + "' no tiene ejemplares disponibles");
        }
        if (libro.getDiasPrestamoMax() != null && request.getDiasPrestamo() > libro.getDiasPrestamoMax()) {
            throw new ReglaDeNegocioException("Los días de préstamo exceden el máximo del libro (" + libro.getDiasPrestamoMax() + ")");
        }

        ReservaLibro reserva = new ReservaLibro();
        reserva.setUsuario(usuario);
        reserva.setLibro(libro);
        reserva.setFechaEntregaEsperada(LocalDate.now().plusDays(request.getDiasPrestamo()));
        reserva.setEstadoReserva(EstadoReserva.ACTIVA);

        libro.setCantidadDisponible(libro.getCantidadDisponible() - 1);
        libroRepository.save(libro);
        return ReservaLibroResponse.desde(reservaLibroRepository.save(reserva));
    }

    @Transactional
    public ReservaLibroResponse devolver(Long reservaId) {
        ReservaLibro reserva = reservaLibroRepository.findById(reservaId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Reserva de libro no encontrada con id: " + reservaId));
        if (reserva.getEstadoReserva() != EstadoReserva.ACTIVA) {
            throw new ReglaDeNegocioException("Solo se puede devolver una reserva activa");
        }
        reserva.setEstadoReserva(EstadoReserva.DEVUELTO);
        reserva.setFechaDevolucionReal(LocalDate.now());

        Libro libro = reserva.getLibro();
        libro.setCantidadDisponible(libro.getCantidadDisponible() + 1);
        libroRepository.save(libro);
        return ReservaLibroResponse.desde(reservaLibroRepository.save(reserva));
    }
}
