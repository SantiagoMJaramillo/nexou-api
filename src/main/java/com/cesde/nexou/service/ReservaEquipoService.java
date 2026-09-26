package com.cesde.nexou.service;
import com.cesde.nexou.dto.request.CrearReservaEquipoRequest;
import com.cesde.nexou.dto.response.ReservaEquipoResponse;
import com.cesde.nexou.model.entity.*;
import com.cesde.nexou.model.enums.EstadoReserva;
import com.cesde.nexou.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.time.Duration;

@Service
@RequiredArgsConstructor
public class ReservaEquipoService {
    private final UsuarioRepository usuarioRepository;
    private final EquipoTecnologicoRepository equipoRepository;
    private final ReservaEquipoRepository reservaRepository;

    @Transactional
    public ReservaEquipoResponse crear(CrearReservaEquipoRequest request) {
        Usuario usuario = usuarioRepository.findByIdAndEstadoActivoTrue(request.getUsuarioId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario inactivo o no existe"));
        EquipoTecnologico equipo = equipoRepository.findByIdAndCantidadDisponibleGreaterThan(request.getEquipoId(), 0)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT, "Equipo sin stock o no existe"));
        
        Duration duracion = Duration.between(request.getHoraInicio(), request.getHoraFin());
        if (duracion.toHours() > equipo.getDuracionMaximaHrs()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Supera la duración máxima del equipo");
        }

        ReservaEquipo reserva = new ReservaEquipo();
        reserva.setUsuario(usuario);
        reserva.setEquipo(equipo);
        reserva.setHoraInicio(request.getHoraInicio());
        reserva.setHoraFin(request.getHoraFin());
        reserva.setEstadoReserva(EstadoReserva.ACTIVA);

        equipo.setCantidadDisponible(equipo.getCantidadDisponible() - 1);
        equipoRepository.save(equipo);
        return ReservaEquipoResponse.desde(reservaRepository.save(reserva));
    }

    @Transactional
    public ReservaEquipoResponse devolver(Long reservaId) {
        ReservaEquipo reserva = reservaRepository.findById(reservaId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No existe la reserva"));
        if (reserva.getEstadoReserva() != EstadoReserva.ACTIVA) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Solo se puede devolver reserva activa");
        }
        reserva.setEstadoReserva(EstadoReserva.DEVUELTO);
        
        EquipoTecnologico equipo = reserva.getEquipo();
        equipo.setCantidadDisponible(equipo.getCantidadDisponible() + 1);
        equipoRepository.save(equipo);
        return ReservaEquipoResponse.desde(reservaRepository.save(reserva));
    }
}