package com.cesde.nexou.service;
import com.cesde.nexou.dto.request.ActualizarReservaLibroRequest;
import com.cesde.nexou.dto.request.CrearReservaLibroRequest;
import com.cesde.nexou.dto.request.RenovarLibroRequest;
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
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReservaLibroService {
    private final UsuarioRepository usuarioRepository;
    private final ValidacionGlobalService validacionGlobalService;
    private final LibroRepository libroRepository;
    private final ReservaLibroRepository reservaLibroRepository;

    private static final int MAX_DIAS_RENOVACION = 10;

    public List<ReservaLibroResponse> obtenerTodos() {
        return reservaLibroRepository.findAll().stream().map(ReservaLibroResponse::desde).toList();
    }

    public ReservaLibroResponse obtenerPorId(Long id) {
        return ReservaLibroResponse.desde(buscarReserva(id));
    }

    @Transactional
    public ReservaLibroResponse crear(CrearReservaLibroRequest request) {
        Usuario usuario = usuarioRepository.findByIdAndEstadoActivoTrue(request.getUsuarioId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario inactivo o no encontrado con id: " + request.getUsuarioId()));
        // Regla de negocio: máximo 3 reservas activas por usuario (libros + equipos)
        validacionGlobalService.validarLimiteGlobal(usuario.getId());
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

    @Transactional
    public ReservaLibroResponse renovar(Long reservaId, RenovarLibroRequest request) {
        ReservaLibro reserva = reservaLibroRepository.findById(reservaId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Reserva de libro no encontrada con id: " + reservaId));
        if (reserva.getEstadoReserva() != EstadoReserva.ACTIVA) {
            throw new ReglaDeNegocioException("Solo las reservas activas se pueden renovar");
        }
        if (request.getDiasExtra() > MAX_DIAS_RENOVACION) {
            throw new ReglaDeNegocioException("No se puede renovar por más de " + MAX_DIAS_RENOVACION + " días extra");
        }
        reserva.setFechaEntregaEsperada(reserva.getFechaEntregaEsperada().plusDays(request.getDiasExtra()));
        return ReservaLibroResponse.desde(reservaLibroRepository.save(reserva));
    }

    @Transactional
    public ReservaLibroResponse actualizar(Long id, ActualizarReservaLibroRequest request) {
        ReservaLibro reserva = buscarReserva(id);
        reserva.setTipoPrestamo(request.getTipoPrestamo());
        reserva.setProposito(request.getProposito());
        return ReservaLibroResponse.desde(reservaLibroRepository.save(reserva));
    }

    @Transactional
    public void eliminar(Long id) {
        ReservaLibro reserva = buscarReserva(id);
        // Regla de negocio: una reserva activa tiene el ejemplar prestado; primero debe devolverse
        if (reserva.getEstadoReserva() == EstadoReserva.ACTIVA) {
            throw new ReglaDeNegocioException("No se puede eliminar una reserva activa. Registre primero la devolución");
        }
        reservaLibroRepository.delete(reserva);
    }

    // Uso del método personalizado del repositorio
    public List<ReservaLibroResponse> obtenerPorUsuarioId(Long usuarioId) {
        if (!usuarioRepository.existsById(usuarioId)) {
            throw new RecursoNoEncontradoException("Usuario no encontrado con id: " + usuarioId);
        }
        return reservaLibroRepository.findByUsuarioId(usuarioId).stream().map(ReservaLibroResponse::desde).toList();
    }

    private ReservaLibro buscarReserva(Long id) {
        return reservaLibroRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Reserva de libro no encontrada con id: " + id));
    }
}
