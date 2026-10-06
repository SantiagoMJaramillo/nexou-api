package com.cesde.nexou.service;
import com.cesde.nexou.exception.RecursoNoEncontradoException;
import com.cesde.nexou.exception.ReglaDeNegocioException;
import com.cesde.nexou.model.entity.*;
import com.cesde.nexou.model.enums.EstadoReserva;
import com.cesde.nexou.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReservaEquipoService {
    private final UsuarioRepository usuarioRepository;
    private final ValidacionGlobalService validacionGlobalService;
    private final EquipoTecnologicoRepository equipoRepository;
    private final ReservaEquipoRepository reservaRepository;

    public List<ReservaEquipo> obtenerTodos() {
        return reservaRepository.findAll();
    }

    public ReservaEquipo obtenerPorId(Long id) {
        return buscarReserva(id);
    }

    @Transactional
    public ReservaEquipo crear(ReservaEquipo request) {
        validarCreacion(request);
        Long usuarioId = request.getUsuario().getId();
        Long equipoId = request.getEquipo().getId();
        Usuario usuario = usuarioRepository.findByIdAndEstadoActivoTrue(usuarioId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario inactivo o no encontrado con id: " + usuarioId));
        // Regla de negocio: máximo 3 reservas activas por usuario (libros + equipos)
        validacionGlobalService.validarLimiteGlobal(usuario.getId());
        EquipoTecnologico equipo = equipoRepository.findById(equipoId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Equipo no encontrado con id: " + equipoId));

        if (equipo.getCantidadDisponible() == null || equipo.getCantidadDisponible() <= 0) {
            throw new ReglaDeNegocioException("El equipo '" + equipo.getNomEquipo() + "' no tiene unidades disponibles");
        }
        if (!request.getHoraFin().isAfter(request.getHoraInicio())) {
            throw new ReglaDeNegocioException("La hora de fin debe ser posterior a la hora de inicio");
        }
        // Regla de negocio: no se reservan equipos para un horario que ya pasó
        if (request.getHoraInicio().isBefore(LocalDateTime.now())) {
            throw new ReglaDeNegocioException("La hora de inicio no puede estar en el pasado");
        }
        // Se compara en minutos: toHours() trunca (2h59m contaría como 2h)
        long minutos = Duration.between(request.getHoraInicio(), request.getHoraFin()).toMinutes();
        if (equipo.getDuracionMaximaHrs() != null && minutos > equipo.getDuracionMaximaHrs() * 60) {
            throw new ReglaDeNegocioException("La reserva supera la duración máxima del equipo (" + equipo.getDuracionMaximaHrs() + " h)");
        }

        request.setUsuario(usuario);
        request.setEquipo(equipo);
        // La entrega se espera el mismo día en que termina la reserva
        request.setFechaEntregaEsperada(request.getHoraFin().toLocalDate());
        request.setEstadoReserva(EstadoReserva.ACTIVA);

        equipo.setCantidadDisponible(equipo.getCantidadDisponible() - 1);
        equipoRepository.save(equipo);
        return reservaRepository.save(request);
    }

    @Transactional
    public ReservaEquipo devolver(Long reservaId) {
        ReservaEquipo reserva = reservaRepository.findById(reservaId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Reserva de equipo no encontrada con id: " + reservaId));
        if (reserva.getEstadoReserva() != EstadoReserva.ACTIVA) {
            throw new ReglaDeNegocioException("Solo se puede devolver una reserva activa");
        }
        reserva.setEstadoReserva(EstadoReserva.DEVUELTO);

        EquipoTecnologico equipo = reserva.getEquipo();
        equipo.setCantidadDisponible(equipo.getCantidadDisponible() + 1);
        equipoRepository.save(equipo);
        return reservaRepository.save(reserva);
    }

    @Transactional
    public ReservaEquipo actualizar(Long id, ReservaEquipo request) {
        ReservaEquipo reserva = buscarReserva(id);
        reserva.setLugarEntrega(request.getLugarEntrega());
        reserva.setProposito(request.getProposito());
        return reservaRepository.save(reserva);
    }

    @Transactional
    public void eliminar(Long id) {
        ReservaEquipo reserva = buscarReserva(id);
        // Regla de negocio: una reserva activa tiene el equipo prestado; primero debe devolverse
        if (reserva.getEstadoReserva() == EstadoReserva.ACTIVA) {
            throw new ReglaDeNegocioException("No se puede eliminar una reserva activa. Registre primero la devolución");
        }
        reservaRepository.delete(reserva);
    }

    // Uso del método personalizado del repositorio
    public List<ReservaEquipo> obtenerPorUsuarioId(Long usuarioId) {
        if (!usuarioRepository.existsById(usuarioId)) {
            throw new RecursoNoEncontradoException("Usuario no encontrado con id: " + usuarioId);
        }
        return reservaRepository.findByUsuarioId(usuarioId);
    }

    private void validarCreacion(ReservaEquipo request) {
        if (request == null || request.getUsuario() == null || request.getUsuario().getId() == null
                || request.getEquipo() == null || request.getEquipo().getId() == null
                || request.getHoraInicio() == null || request.getHoraFin() == null) {
            throw new ReglaDeNegocioException("La reserva debe incluir usuario.id, equipo.id, horaInicio y horaFin");
        }
    }

    private ReservaEquipo buscarReserva(Long id) {
        return reservaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Reserva de equipo no encontrada con id: " + id));
    }
}
