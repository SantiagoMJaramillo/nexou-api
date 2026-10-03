package com.cesde.nexou.service;

import com.cesde.nexou.exception.ReglaDeNegocioException;
import com.cesde.nexou.model.enums.EstadoReserva;
import com.cesde.nexou.repository.ReservaEquipoRepository;
import com.cesde.nexou.repository.ReservaLibroRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ValidacionGlobalService {
    private final ReservaLibroRepository reservaLibroRepo;
    private final ReservaEquipoRepository reservaEquipoRepo;

    private static final long LIMITE_MAXIMO_GLOBAL = 3;

    // Regla de negocio: un usuario no puede tener más de 3 reservas activas (libros + equipos)
    public void validarLimiteGlobal(Long usuarioId) {
        long librosActivos = reservaLibroRepo.countByUsuarioIdAndEstadoReserva(usuarioId, EstadoReserva.ACTIVA);
        long equiposActivos = reservaEquipoRepo.countByUsuarioIdAndEstadoReserva(usuarioId, EstadoReserva.ACTIVA);

        if ((librosActivos + equiposActivos) >= LIMITE_MAXIMO_GLOBAL) {
            throw new ReglaDeNegocioException("El usuario alcanzó el límite de " + LIMITE_MAXIMO_GLOBAL + " reservas activas.");
        }
    }
}
