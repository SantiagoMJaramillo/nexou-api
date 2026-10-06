package com.cesde.nexou.service;
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

    public List<ReservaLibro> obtenerTodos() {
        return reservaLibroRepository.findAll();
    }

    public ReservaLibro obtenerPorId(Long id) {
        return buscarReserva(id);
    }

    @Transactional
    public ReservaLibro crear(ReservaLibro request) {
        validarCreacion(request);
        Long usuarioId = request.getUsuario().getId();
        Long libroId = request.getLibro().getId();
        Usuario usuario = usuarioRepository.findByIdAndEstadoActivoTrue(usuarioId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario inactivo o no encontrado con id: " + usuarioId));
        // Regla de negocio: máximo 3 reservas activas por usuario (libros + equipos)
        validacionGlobalService.validarLimiteGlobal(usuario.getId());
        Libro libro = libroRepository.findById(libroId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Libro no encontrado con id: " + libroId));

        if (libro.getCantidadDisponible() == null || libro.getCantidadDisponible() <= 0) {
            throw new ReglaDeNegocioException("El libro '" + libro.getNomLibro() + "' no tiene ejemplares disponibles");
        }
        if (libro.getDiasPrestamoMax() != null && request.getDiasPrestamo() > libro.getDiasPrestamoMax()) {
            throw new ReglaDeNegocioException("Los días de préstamo exceden el máximo del libro (" + libro.getDiasPrestamoMax() + ")");
        }

        request.setUsuario(usuario);
        request.setLibro(libro);
        request.setFechaEntregaEsperada(LocalDate.now().plusDays(request.getDiasPrestamo()));
        request.setEstadoReserva(EstadoReserva.ACTIVA);

        libro.setCantidadDisponible(libro.getCantidadDisponible() - 1);
        libroRepository.save(libro);
        // Un POST siempre crea un registro nuevo: se ignora cualquier id recibido en el body
        request.setId(null);
        return reservaLibroRepository.save(request);
    }

    @Transactional
    public ReservaLibro devolver(Long reservaId) {
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
        return reservaLibroRepository.save(reserva);
    }

    @Transactional
    public ReservaLibro renovar(Long reservaId, Integer diasExtra) {
        ReservaLibro reserva = reservaLibroRepository.findById(reservaId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Reserva de libro no encontrada con id: " + reservaId));
        if (reserva.getEstadoReserva() != EstadoReserva.ACTIVA) {
            throw new ReglaDeNegocioException("Solo las reservas activas se pueden renovar");
        }
        if (diasExtra == null || diasExtra <= 0) {
            throw new ReglaDeNegocioException("Los días extra deben ser positivos");
        }
        if (diasExtra > MAX_DIAS_RENOVACION) {
            throw new ReglaDeNegocioException("No se puede renovar por más de " + MAX_DIAS_RENOVACION + " días extra");
        }
        reserva.setFechaEntregaEsperada(reserva.getFechaEntregaEsperada().plusDays(diasExtra));
        return reservaLibroRepository.save(reserva);
    }

    @Transactional
    public ReservaLibro actualizar(Long id, ReservaLibro request) {
        ReservaLibro reserva = buscarReserva(id);
        reserva.setTipoPrestamo(request.getTipoPrestamo());
        reserva.setProposito(request.getProposito());
        return reservaLibroRepository.save(reserva);
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
    public List<ReservaLibro> obtenerPorUsuarioId(Long usuarioId) {
        if (!usuarioRepository.existsById(usuarioId)) {
            throw new RecursoNoEncontradoException("Usuario no encontrado con id: " + usuarioId);
        }
        return reservaLibroRepository.findByUsuarioId(usuarioId);
    }

    private void validarCreacion(ReservaLibro request) {
        if (request == null || request.getUsuario() == null || request.getUsuario().getId() == null
                || request.getLibro() == null || request.getLibro().getId() == null
                || request.getDiasPrestamo() == null || request.getDiasPrestamo() <= 0) {
            throw new ReglaDeNegocioException("La reserva debe incluir usuario.id, libro.id y diasPrestamo positivo");
        }
    }

    private ReservaLibro buscarReserva(Long id) {
        return reservaLibroRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Reserva de libro no encontrada con id: " + id));
    }
}
