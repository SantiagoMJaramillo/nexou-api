package com.cesde.nexou.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cesde.nexou.exception.RecursoNoEncontradoException;
import com.cesde.nexou.exception.ReglaDeNegocioException;
import com.cesde.nexou.model.entity.EquipoTecnologico;
import com.cesde.nexou.repository.EquipoTecnologicoRepository;
import com.cesde.nexou.repository.ReservaEquipoRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EquipoTecnologicoService {

    private final EquipoTecnologicoRepository equipoRepository;
    private final ReservaEquipoRepository reservaEquipoRepository;

    public List<EquipoTecnologico> obtenerTodos() {
        return equipoRepository.findAll();
    }

    public EquipoTecnologico obtenerPorId(Long id) {
        return equipoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Equipo no encontrado con id: " + id));
    }

    @Transactional
    public EquipoTecnologico crear(EquipoTecnologico equipo) {
        validarDatos(equipo);
        // Si no se indica la cantidad disponible, todas las unidades inician disponibles
        if (equipo.getCantidadDisponible() == null) {
            equipo.setCantidadDisponible(equipo.getCantidadTotal());
        }
        validarInventario(equipo);
        // Un POST siempre crea un registro nuevo: se ignora cualquier id recibido en el body
        equipo.setId(null);
        return equipoRepository.save(equipo);
    }

    @Transactional
    public EquipoTecnologico actualizar(Long id, EquipoTecnologico datos) {
        EquipoTecnologico equipo = obtenerPorId(id);
        validarDatos(datos);

        equipo.setNomEquipo(datos.getNomEquipo());
        equipo.setMarca(datos.getMarca());
        equipo.setModelo(datos.getModelo());
        equipo.setTipoEquipo(datos.getTipoEquipo());
        equipo.setCantidadTotal(datos.getCantidadTotal());
        if (datos.getCantidadDisponible() != null) {
            equipo.setCantidadDisponible(datos.getCantidadDisponible());
        }
        equipo.setDuracionMaximaHrs(datos.getDuracionMaximaHrs());
        equipo.setEstadoEquipo(datos.getEstadoEquipo());
        validarInventario(equipo);
        return equipoRepository.save(equipo);
    }

    @Transactional
    public void eliminar(Long id) {
        EquipoTecnologico equipo = obtenerPorId(id);
        // Regla de negocio: no se elimina un equipo con historial de préstamos
        if (reservaEquipoRepository.existsByEquipoId(id)) {
            throw new ReglaDeNegocioException("No se puede eliminar el equipo porque tiene reservas registradas. Desactívelo (estadoActivo = false)");
        }
        equipoRepository.delete(equipo);
    }

    // Uso del método personalizado del repositorio
    public List<EquipoTecnologico> obtenerDisponibles() {
        return equipoRepository.findByCantidadDisponibleGreaterThan(0);
    }

    private void validarDatos(EquipoTecnologico equipo) {
        if (equipo.getNomEquipo() == null || equipo.getNomEquipo().isBlank()) {
            throw new ReglaDeNegocioException("El nombre del equipo es obligatorio");
        }
        if (equipo.getCantidadTotal() == null || equipo.getCantidadTotal() <= 0) {
            throw new ReglaDeNegocioException("La cantidad total de unidades debe ser mayor a 0");
        }
        if (equipo.getDuracionMaximaHrs() == null || equipo.getDuracionMaximaHrs() <= 0) {
            throw new ReglaDeNegocioException("La duración máxima de préstamo (horas) debe ser mayor a 0");
        }
    }

    private void validarInventario(EquipoTecnologico equipo) {
        if (equipo.getCantidadDisponible() < 0 || equipo.getCantidadDisponible() > equipo.getCantidadTotal()) {
            throw new ReglaDeNegocioException("La cantidad disponible debe estar entre 0 y la cantidad total (" + equipo.getCantidadTotal() + ")");
        }
    }
}
