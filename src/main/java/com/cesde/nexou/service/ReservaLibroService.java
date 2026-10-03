package com.cesde.nexou.service;
import com.cesde.nexou.dto.request.CrearReservaLibroRequest;
import com.cesde.nexou.dto.response.ReservaLibroResponse;
import com.cesde.nexou.model.entity.*;
import com.cesde.nexou.model.enums.EstadoReserva;
import com.cesde.nexou.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
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
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario inactivo o no existe"));
        Libro libro = libroRepository.findByIdAndCantidadDisponibleGreaterThan(request.getLibroId(), 0)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT, "Libro sin stock o no existe"));
        
        if (request.getDiasPrestamo() > libro.getDiasPrestamoMax()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Los días exceden el máximo del libro");
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
}
