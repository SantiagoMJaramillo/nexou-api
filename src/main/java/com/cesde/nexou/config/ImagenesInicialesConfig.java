package com.cesde.nexou.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ClassPathResource;

import com.cesde.nexou.repository.EquipoTecnologicoRepository;
import com.cesde.nexou.repository.ImagenArticuloRepository;
import com.cesde.nexou.repository.LibroRepository;
import com.cesde.nexou.service.ImagenService;

/**
 * Pone una ilustración a los artículos de demostración del seed (ISBN / nombre conocidos).
 * Solo corre cuando todavía no hay ninguna imagen guardada: así no pisa las fotos que suba el
 * administrador ni las vuelve a poner si las quita después. Las imágenes viven en
 * src/main/resources/seed-images y son ilustraciones propias, no portadas reales.
 */
@Configuration
public class ImagenesInicialesConfig {

    private static final Logger log = LoggerFactory.getLogger(ImagenesInicialesConfig.class);

    private static final Map<String, String> LIBROS_POR_ISBN = Map.of(
            "978-8441532106", "codigo-limpio.jpg",
            "978-8478290598", "patrones-de-diseno.jpg",
            "978-8448146443", "fundamentos-bases-de-datos.jpg",
            "978-6073208178", "redes-de-computadoras.jpg",
            "978-0000000001", "sql-y-seguridad.jpg");

    private static final Map<String, String> EQUIPOS_POR_NOMBRE = Map.of(
            "Portátil ThinkPad E14", "portatil-thinkpad-e14.jpg",
            "Proyector PowerLite", "proyector-powerlite.jpg",
            "Tablet Galaxy Tab", "tablet-galaxy-tab.jpg");

    @Bean
    @Order(3)
    CommandLineRunner imagenesIniciales(ImagenArticuloRepository imagenRepository, LibroRepository libroRepository,
                                        EquipoTecnologicoRepository equipoRepository, ImagenService imagenService) {
        return args -> {
            if (imagenRepository.count() > 0) {
                return;
            }
            int puestas = 0;
            for (var libro : libroRepository.findAll()) {
                String archivo = LIBROS_POR_ISBN.get(libro.getIsbn());
                if (archivo != null && libro.getImagenVersion() == null && poner(archivo, bytes -> imagenService.guardarDeLibro(libro.getId(), bytes))) {
                    puestas++;
                }
            }
            for (var equipo : equipoRepository.findAll()) {
                String archivo = EQUIPOS_POR_NOMBRE.get(equipo.getNomEquipo());
                if (archivo != null && equipo.getImagenVersion() == null && poner(archivo, bytes -> imagenService.guardarDeEquipo(equipo.getId(), bytes))) {
                    puestas++;
                }
            }
            if (puestas > 0) {
                log.info(">>> {} imágenes de demostración cargadas.", puestas);
            }
        };
    }

    private boolean poner(String archivo, java.util.function.Consumer<byte[]> guardar) {
        try (InputStream entrada = new ClassPathResource("seed-images/" + archivo).getInputStream()) {
            guardar.accept(entrada.readAllBytes());
            return true;
        } catch (IOException | RuntimeException e) {
            log.warn("No se pudo cargar la imagen {}: {}", archivo, e.getMessage());
            return false;
        }
    }
}
