package com.cesde.nexou.service;

import com.cesde.nexou.model.entity.ReservaLibro;
import com.cesde.nexou.model.enums.EstadoReserva;
import com.cesde.nexou.repository.ReservaLibroRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class RenovacionService {
    private final ReservaLibroRepository reservaLibroRepo;

    @Transactional
    public String renovarLibro(Long reservaId, Integer diasExtra) {
        ReservaLibro reserva = reservaLibroRepo.findById(reservaId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Reserva no existe"));
        
        if (reserva.getEstadoReserva() != EstadoReserva.ACTIVA) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Solo reservas activas se pueden renovar");
        }
        if (diasExtra > 10) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No puede renovar por más de 10 días extras");
        }
        
        reserva.setFechaEntregaEsperada(reserva.getFechaEntregaEsperada().plusDays(diasExtra));
        reservaLibroRepo.save(reserva);
        return "Renovación exitosa. Nueva fecha: " + reserva.getFechaEntregaEsperada();
    }
}
