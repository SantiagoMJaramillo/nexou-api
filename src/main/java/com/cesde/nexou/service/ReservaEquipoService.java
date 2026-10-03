package com.cesde.nexou.service;
import com.cesde.nexou.dto.request.CrearReservaEquipoRequest;
import com.cesde.nexou.dto.response.ReservaEquipoResponse;
import com.cesde.nexou.exception.RecursoNoEncontradoException;
import com.cesde.nexou.exception.ReglaDeNegocioException;
import com.cesde.nexou.model.entity.*;
import com.cesde.nexou.model.enums.EstadoReserva;
import com.cesde.nexou.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Duration;

@Service
@RequiredArgsConstructor
public class ReservaEquipoService {
    private final UsuarioRepository usuarioRepository;
    private final ValidacionGlobalService validacionGlobalService;
    private final EquipoTecnologicoRepository equipoRepository;
    private final ReservaEquipoRepository reservaRepository;

    @Transactional
    public ReservaEquipoResponse crear(CrearReservaEquipoRequest request) {
        Usuario usuario = usuarioRepository.findByIdAndEstadoActivoTrue(request.getUsuarioId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario inactivo o no encontrado con id: " + request.getUsuarioId()));
        // Regla de negocio: máximo 3 reservas activas por usuario (libros + equipos)
        validacionGlobalService.validarLimiteGlobal(usuario.getId());
        EquipoTecnologico equipo = equipoRepository.findById(request.getEquipoId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Equipo no encontrado con id: " + request.getEquipoId()));

        if (equipo.getCantidadDisponible() == null || equipo.getCantidadDisponible() <= 0) {
            throw new ReglaDeNegocioException("El equipo '" + equipo.getNomEquipo() + "' no tiene unidades disponibles");
        }
        if (!request.getHoraFin().isAfter(request.getHoraInicio())) {
            throw new ReglaDeNegocioException("La hora de fin debe ser posterior a la hora de inicio");
        }
        // Se compara en minutos: toHours() trunca (2h59m contaría como 2h)
        long minutos = Duration.between(request.getHoraInicio(), request.getHoraFin()).toMinutes();
        if (equipo.getDuracionMaximaHrs() != null && minutos > equipo.getDuracionMaximaHrs() * 60) {
            throw new ReglaDeNegocioException("La reserva supera la duración máxima del equipo (" + equipo.getDuracionMaximaHrs() + " h)");
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
                .orElseThrow(() -> new RecursoNoEncontradoException("Reserva de equipo no encontrada con id: " + reservaId));
        if (reserva.getEstadoReserva() != EstadoReserva.ACTIVA) {
            throw new ReglaDeNegocioException("Solo se puede devolver una reserva activa");
        }
        reserva.setEstadoReserva(EstadoReserva.DEVUELTO);

        EquipoTecnologico equipo = reserva.getEquipo();
        equipo.setCantidadDisponible(equipo.getCantidadDisponible() + 1);
        equipoRepository.save(equipo);
        return ReservaEquipoResponse.desde(reservaRepository.save(reserva));
    }
}
