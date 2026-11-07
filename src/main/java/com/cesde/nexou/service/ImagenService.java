package com.cesde.nexou.service;

import java.io.IOException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.cesde.nexou.exception.RecursoNoEncontradoException;
import com.cesde.nexou.exception.ReglaDeNegocioException;
import com.cesde.nexou.model.entity.EquipoTecnologico;
import com.cesde.nexou.model.entity.ImagenArticulo;
import com.cesde.nexou.model.entity.Libro;
import com.cesde.nexou.repository.EquipoTecnologicoRepository;
import com.cesde.nexou.repository.ImagenArticuloRepository;
import com.cesde.nexou.repository.LibroRepository;

import lombok.RequiredArgsConstructor;

/** Guarda, entrega y borra las imágenes de libros y equipos. */
@Service
@RequiredArgsConstructor
public class ImagenService {

    public static final String LIBRO = "LIBRO";
    public static final String EQUIPO = "EQUIPO";
    private static final long TAMANO_MAXIMO = 1024L * 1024;

    private final ImagenArticuloRepository imagenRepository;
    private final LibroRepository libroRepository;
    private final EquipoTecnologicoRepository equipoRepository;

    @Transactional
    public void guardarDeLibro(Long libroId, MultipartFile archivo) {
        guardarDeLibro(libroId, leer(archivo));
    }

    @Transactional
    public void guardarDeEquipo(Long equipoId, MultipartFile archivo) {
        guardarDeEquipo(equipoId, leer(archivo));
    }

    @Transactional
    public void guardarDeLibro(Long libroId, byte[] datos) {
        Libro libro = libroRepository.findById(libroId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Libro no encontrado con id: " + libroId));
        guardar(LIBRO, libroId, datos);
        libro.setImagenVersion(System.currentTimeMillis());
        libroRepository.save(libro);
    }

    @Transactional
    public void guardarDeEquipo(Long equipoId, byte[] datos) {
        EquipoTecnologico equipo = equipoRepository.findById(equipoId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Equipo no encontrado con id: " + equipoId));
        guardar(EQUIPO, equipoId, datos);
        equipo.setImagenVersion(System.currentTimeMillis());
        equipoRepository.save(equipo);
    }

    @Transactional
    public void quitarDeLibro(Long libroId) {
        Libro libro = libroRepository.findById(libroId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Libro no encontrado con id: " + libroId));
        imagenRepository.deleteByTipoAndArticuloId(LIBRO, libroId);
        libro.setImagenVersion(null);
        libroRepository.save(libro);
    }

    @Transactional
    public void quitarDeEquipo(Long equipoId) {
        EquipoTecnologico equipo = equipoRepository.findById(equipoId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Equipo no encontrado con id: " + equipoId));
        imagenRepository.deleteByTipoAndArticuloId(EQUIPO, equipoId);
        equipo.setImagenVersion(null);
        equipoRepository.save(equipo);
    }

    public ImagenArticulo obtener(String tipo, Long articuloId) {
        return imagenRepository.findByTipoAndArticuloId(tipo, articuloId)
                .orElseThrow(() -> new RecursoNoEncontradoException("El artículo no tiene imagen"));
    }

    private byte[] leer(MultipartFile archivo) {
        if (archivo == null || archivo.isEmpty()) {
            throw new ReglaDeNegocioException("Selecciona una imagen");
        }
        try {
            return archivo.getBytes();
        } catch (IOException e) {
            throw new ReglaDeNegocioException("No se pudo leer la imagen");
        }
    }

    private void guardar(String tipo, Long articuloId, byte[] datos) {
        if (datos.length > TAMANO_MAXIMO) {
            throw new ReglaDeNegocioException("La imagen supera el máximo de 1 MB");
        }
        // El tipo se deduce de los primeros bytes, no del nombre ni del header: así no se
        // puede subir un HTML o un script disfrazado de imagen.
        String tipoContenido = detectarTipo(datos);
        if (tipoContenido == null) {
            throw new ReglaDeNegocioException("Formato no válido: usa JPG, PNG o WEBP");
        }

        ImagenArticulo imagen = imagenRepository.findByTipoAndArticuloId(tipo, articuloId).orElseGet(ImagenArticulo::new);
        imagen.setTipo(tipo);
        imagen.setArticuloId(articuloId);
        imagen.setTipoContenido(tipoContenido);
        imagen.setDatos(datos);
        imagenRepository.save(imagen);
    }

    private static String detectarTipo(byte[] d) {
        if (d.length >= 3 && (d[0] & 0xFF) == 0xFF && (d[1] & 0xFF) == 0xD8 && (d[2] & 0xFF) == 0xFF) {
            return "image/jpeg";
        }
        if (d.length >= 8 && (d[0] & 0xFF) == 0x89 && d[1] == 'P' && d[2] == 'N' && d[3] == 'G') {
            return "image/png";
        }
        if (d.length >= 12 && d[0] == 'R' && d[1] == 'I' && d[2] == 'F' && d[3] == 'F'
                && d[8] == 'W' && d[9] == 'E' && d[10] == 'B' && d[11] == 'P') {
            return "image/webp";
        }
        return null;
    }
}
