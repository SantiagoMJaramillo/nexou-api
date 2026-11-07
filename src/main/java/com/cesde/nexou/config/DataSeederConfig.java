package com.cesde.nexou.config;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.cesde.nexou.model.enums.Rol;

import com.cesde.nexou.model.embeddable.Ubicacion;
import com.cesde.nexou.model.entity.Categoria;
import com.cesde.nexou.model.entity.ConfiguracionUsuario;
import com.cesde.nexou.model.entity.EquipoTecnologico;
import com.cesde.nexou.model.entity.Libro;
import com.cesde.nexou.model.entity.ReservaEquipo;
import com.cesde.nexou.model.entity.ReservaLibro;
import com.cesde.nexou.model.entity.Usuario;
import com.cesde.nexou.model.enums.EstadoReserva;
import com.cesde.nexou.repository.CategoriaRepository;
import com.cesde.nexou.repository.EquipoTecnologicoRepository;
import com.cesde.nexou.repository.LibroRepository;
import com.cesde.nexou.repository.ReservaEquipoRepository;
import com.cesde.nexou.repository.ReservaLibroRepository;
import com.cesde.nexou.repository.UsuarioRepository;

/**
 * Siembra datos de prueba al arrancar la aplicación, solo si la base de datos está vacía.
 * Orden: Categorías -> Libros -> Usuarios (con su configuración) -> Equipos -> Reservas.
 */
@Configuration
public class DataSeederConfig {

    @Bean
    @Order(1)
    CommandLineRunner sembrarDatos(PasswordEncoder passwordEncoder,
                                          JdbcTemplate jdbc,
                                          CategoriaRepository categoriaRepository,
                                          LibroRepository libroRepository,
                                          UsuarioRepository usuarioRepository,
                                          EquipoTecnologicoRepository equipoRepository,
                                          ReservaLibroRepository reservaLibroRepository,
                                          ReservaEquipoRepository reservaEquipoRepository) {
        return args -> {
            // Solo sembramos si el catálogo está vacío, para evitar duplicados en cada arranque
            if (libroRepository.count() > 0 || usuarioRepository.count() > 0) {
                return;
            }

            // 1. Categorías
            Categoria software = categoria("Ingeniería de Software", "Diseño, buenas prácticas y patrones");
            Categoria datos = categoria("Bases de Datos", "Modelado relacional y SQL");
            Categoria redes = categoria("Redes y Seguridad", "Redes de computadores y seguridad informática");
            categoriaRepository.saveAll(List.of(software, datos, redes));

            // 2. Libros (con ubicación embebida y categorías)
            Libro cleanCode = libro("Código limpio", "Robert C. Martin", "Anaya", "978-8441532106", 3, 15,
                    new Ubicacion("Sede Medellín", "2", "Estante A-1"), Set.of(software));
            Libro patrones = libro("Patrones de diseño", "Erich Gamma y otros", "Addison-Wesley", "978-8478290598", 2, 10,
                    new Ubicacion("Sede Medellín", "2", "Estante A-2"), Set.of(software));
            Libro fundamentosBd = libro("Fundamentos de bases de datos", "Abraham Silberschatz", "McGraw-Hill", "978-8448146443", 4, 15,
                    new Ubicacion("Sede Bello", "1", "Estante B-1"), Set.of(datos));
            Libro redesLibro = libro("Redes de computadoras", "Andrew S. Tanenbaum", "Pearson", "978-6073208178", 2, 10,
                    new Ubicacion("Sede Bello", "1", "Estante C-1"), Set.of(redes));
            Libro sqlSeguro = libro("SQL y seguridad de datos", "Equipo NEXOU", "CESDE", "978-0000000001", 1, 7,
                    new Ubicacion("Sede Medellín", "3", "Estante D-4"), Set.of(datos, redes));
            libroRepository.saveAll(List.of(cleanCode, patrones, fundamentosBd, redesLibro, sqlSeguro));

            // 3. Usuarios con su configuración (se guarda por cascada)
            Usuario laura = usuario(passwordEncoder, "laura", "laura.restrepo@cesde.net", "CES-2024-001", "Desarrollo de Software", 3, "3001234567");
            laura.setConfiguracionUsuario(configuracion(laura, "es", "claro", true));
            Usuario carlos = usuario(passwordEncoder, "carlos", "carlos.gomez@cesde.net", "CES-2024-002", "Analítica de Datos", 2, "3007654321");
            carlos.setConfiguracionUsuario(configuracion(carlos, "es", "oscuro", false));
            usuarioRepository.saveAll(List.of(laura, carlos));

            // 4. Equipos tecnológicos
            EquipoTecnologico portatil = equipo("Portátil ThinkPad E14", "Lenovo", "E14 Gen 5", "Portátil", 5, 4.0);
            EquipoTecnologico proyector = equipo("Proyector PowerLite", "Epson", "X49", "Proyector", 2, 3.0);
            EquipoTecnologico tablet = equipo("Tablet Galaxy Tab", "Samsung", "S9 FE", "Tablet", 4, 2.0);
            equipoRepository.saveAll(List.of(portatil, proyector, tablet));

            // 5. Reservas. Cada reserva ACTIVA descuenta 1 unidad disponible para que el stock quede coherente
            LocalDateTime hoy8am = LocalDate.now().atTime(8, 0);
            reservaLibroRepository.saveAll(List.of(
                    reservaLibro(laura, prestar(cleanCode), "Domicilio", "Proyecto integrador", LocalDate.now().plusDays(7), null, EstadoReserva.ACTIVA),
                    reservaLibro(carlos, prestar(fundamentosBd), "Domicilio", "Parcial de bases de datos", LocalDate.now().plusDays(10), null, EstadoReserva.ACTIVA),
                    reservaLibro(carlos, redesLibro, "Sala", "Consulta", LocalDate.now().minusDays(5), LocalDate.now().minusDays(6), EstadoReserva.DEVUELTO)));
            reservaEquipoRepository.saveAll(List.of(
                    reservaEquipo(laura, prestar(portatil), hoy8am, hoy8am.plusHours(3), "Biblioteca Sede Medellín", "Práctica de programación"),
                    reservaEquipo(carlos, prestar(proyector), hoy8am.plusHours(2), hoy8am.plusHours(4), "Aula 204 Sede Bello", "Exposición")));
            libroRepository.saveAll(List.of(cleanCode, fundamentosBd));
            equipoRepository.saveAll(List.of(portatil, proyector));

            // 6. Historial de demostración (varios meses, semestres y devoluciones tardías) para las estadísticas
            sembrarHistorial(passwordEncoder, jdbc, usuarioRepository, libroRepository, equipoRepository,
                    reservaLibroRepository, reservaEquipoRepository, laura, carlos,
                    cleanCode, patrones, fundamentosBd, redesLibro, sqlSeguro, portatil, proyector, tablet);

            System.out.println(">>> SEED EXITOSA: 3 categorías, 5 libros, 2 usuarios con configuración, 3 equipos, 3 reservas de libros y 2 de equipos.");
        };
    }

    /**
     * Crea 4 usuarios más (semestres 1 a 5) y un historial de préstamos repartido en los últimos ~80 días.
     * "retraso" > 0 significa que se devolvió tarde (días en libros, horas en equipos).
     */
    private void sembrarHistorial(PasswordEncoder passwordEncoder, JdbcTemplate jdbc, UsuarioRepository usuarioRepository,
                                  LibroRepository libroRepository, EquipoTecnologicoRepository equipoRepository,
                                  ReservaLibroRepository reservaLibroRepository, ReservaEquipoRepository reservaEquipoRepository,
                                  Usuario laura, Usuario carlos, Libro cleanCode, Libro patrones, Libro fundamentosBd,
                                  Libro redesLibro, Libro sqlSeguro, EquipoTecnologico portatil, EquipoTecnologico proyector,
                                  EquipoTecnologico tablet) {
        Usuario ana = usuario(passwordEncoder, "ana", null, null, "Desarrollo de Software", 1, null);
        Usuario luis = usuario(passwordEncoder, "luis", null, null, "Analítica de Datos", 4, null);
        Usuario marta = usuario(passwordEncoder, "marta", null, null, "Redes y Seguridad", 5, null);
        Usuario pedro = usuario(passwordEncoder, "pedro", null, null, "Desarrollo de Software", 3, null);
        usuarioRepository.saveAll(List.of(ana, luis, marta, pedro));

        // {usuario, libro, díasAtrás, díasPréstamo, retraso}
        Object[][] libros = {
            {ana, cleanCode, 80, 14, 0}, {luis, fundamentosBd, 75, 15, 3}, {marta, redesLibro, 70, 10, 5},
            {carlos, cleanCode, 62, 14, 0}, {pedro, patrones, 55, 10, 2}, {laura, fundamentosBd, 48, 15, -1},
            {luis, sqlSeguro, 40, 7, 4}, {ana, patrones, 33, 10, 0}, {marta, cleanCode, 28, 14, 6},
            {pedro, fundamentosBd, 21, 15, 0}, {luis, cleanCode, 14, 14, 1}, {laura, redesLibro, 9, 10, 0},
            {marta, sqlSeguro, 6, 7, 3}};
        for (Object[] f : libros) {
            LocalDate prestamo = LocalDate.now().minusDays((int) f[2]);
            LocalDate esperada = prestamo.plusDays((int) f[3]);
            ReservaLibro r = reservaLibro((Usuario) f[0], (Libro) f[1], "Domicilio", "Historial de demostración",
                    esperada, esperada.plusDays((int) f[4]), EstadoReserva.DEVUELTO);
            reservaLibroRepository.save(r);
            jdbc.update("UPDATE reserva_libro SET fecha_creacion = ? WHERE id = ?",
                    java.sql.Timestamp.valueOf(prestamo.atTime(10, 0)), r.getId());
        }
        // Un préstamo que sigue activo y ya pasó su fecha límite (aparece como "en mora")
        LocalDate prestamoVencido = LocalDate.now().minusDays(20);
        ReservaLibro enMora = reservaLibro(luis, prestar(patrones), "Domicilio", "Historial de demostración",
                prestamoVencido.plusDays(10), null, EstadoReserva.ACTIVA);
        reservaLibroRepository.save(enMora);
        jdbc.update("UPDATE reserva_libro SET fecha_creacion = ? WHERE id = ?",
                java.sql.Timestamp.valueOf(prestamoVencido.atTime(10, 0)), enMora.getId());
        libroRepository.save(patrones);

        // {usuario, equipo, díasAtrás, horasPréstamo, horasRetraso}
        Object[][] equipos = {
            {luis, portatil, 70, 3, 2}, {ana, tablet, 52, 2, 0}, {pedro, proyector, 44, 3, 1}, {marta, portatil, 30, 4, 0},
            {luis, portatil, 19, 3, 5}, {carlos, tablet, 11, 2, 0}, {ana, portatil, 4, 3, 0}};
        for (Object[] f : equipos) {
            LocalDateTime inicio = LocalDate.now().minusDays((int) f[2]).atTime(9, 0);
            LocalDateTime fin = inicio.plusHours((int) f[3]);
            ReservaEquipo r = reservaEquipo((Usuario) f[0], (EquipoTecnologico) f[1], inicio, fin, "Biblioteca", "Historial de demostración");
            r.setEstadoReserva(EstadoReserva.DEVUELTO);
            r.setFechaDevolucionReal(fin.plusHours((int) f[4]));
            reservaEquipoRepository.save(r);
            jdbc.update("UPDATE reserva_equipo SET fecha_creacion = ? WHERE id = ?", java.sql.Timestamp.valueOf(inicio.minusHours(1)), r.getId());
        }
    }

    // ---------- Métodos de apoyo para construir los objetos ----------

    private Categoria categoria(String nombre, String descripcion) {
        Categoria c = new Categoria();
        c.setNombre(nombre);
        c.setDescripcion(descripcion);
        return c;
    }

    private Libro libro(String nombre, String autor, String editorial, String isbn, int total, int diasMax,
                        Ubicacion ubicacion, Set<Categoria> categorias) {
        Libro l = new Libro();
        l.setNomLibro(nombre);
        l.setAutor(autor);
        l.setEditorial(editorial);
        l.setIsbn(isbn);
        l.setCantidadTotal(total);
        l.setCantidadDisponible(total);
        l.setDiasPrestamoMax(diasMax);
        l.setUbicacionFisica(ubicacion);
        l.getCategorias().addAll(categorias);
        return l;
    }

    private Usuario usuario(PasswordEncoder passwordEncoder, String nombre, String correo, String codigo, String programa, int semestre, String telefono) {
        Usuario u = new Usuario();
        u.setNombreUsuario(nombre);
        u.setCorreo(correo);
        u.setContrasena(passwordEncoder.encode(System.getenv().getOrDefault("SEED_PASSWORD", java.util.UUID.randomUUID().toString()))); // aleatoria si no se define SEED_PASSWORD
        u.setRol(Rol.USER);
        u.setCodigoEstudiante(codigo);
        u.setPrograma(programa);
        u.setSemestre(semestre);
        u.setTelefono(telefono);
        return u;
    }

    private ConfiguracionUsuario configuracion(Usuario usuario, String idioma, String tema, boolean notificaciones) {
        ConfiguracionUsuario c = new ConfiguracionUsuario();
        c.setUsuario(usuario);
        c.setIdioma(idioma);
        c.setTema(tema);
        c.setNotificacionesActivas(notificaciones);
        return c;
    }

    private EquipoTecnologico equipo(String nombre, String marca, String modelo, String tipo, int total, double horasMax) {
        EquipoTecnologico e = new EquipoTecnologico();
        e.setNomEquipo(nombre);
        e.setMarca(marca);
        e.setModelo(modelo);
        e.setTipoEquipo(tipo);
        e.setCantidadTotal(total);
        e.setCantidadDisponible(total);
        e.setDuracionMaximaHrs(horasMax);
        e.setEstadoEquipo("Operativo");
        return e;
    }

    private Libro prestar(Libro libro) {
        libro.setCantidadDisponible(libro.getCantidadDisponible() - 1);
        return libro;
    }

    private EquipoTecnologico prestar(EquipoTecnologico equipo) {
        equipo.setCantidadDisponible(equipo.getCantidadDisponible() - 1);
        return equipo;
    }

    private ReservaLibro reservaLibro(Usuario usuario, Libro libro, String tipo, String proposito,
                                      LocalDate entregaEsperada, LocalDate devolucionReal, EstadoReserva estado) {
        ReservaLibro r = new ReservaLibro();
        r.setUsuario(usuario);
        r.setLibro(libro);
        r.setTipoPrestamo(tipo);
        r.setProposito(proposito);
        r.setFechaEntregaEsperada(entregaEsperada);
        r.setFechaDevolucionReal(devolucionReal);
        r.setEstadoReserva(estado);
        return r;
    }

    private ReservaEquipo reservaEquipo(Usuario usuario, EquipoTecnologico equipo, LocalDateTime inicio,
                                        LocalDateTime fin, String lugar, String proposito) {
        ReservaEquipo r = new ReservaEquipo();
        r.setUsuario(usuario);
        r.setEquipo(equipo);
        r.setHoraInicio(inicio);
        r.setHoraFin(fin);
        r.setLugarEntrega(lugar);
        r.setProposito(proposito);
        r.setFechaEntregaEsperada(fin.toLocalDate());
        r.setEstadoReserva(EstadoReserva.ACTIVA);
        return r;
    }
}
