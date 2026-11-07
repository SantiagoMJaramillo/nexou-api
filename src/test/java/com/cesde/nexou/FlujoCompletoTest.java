package com.cesde.nexou;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

/** Recorre el flujo real: registro, login, permisos por rol y reservas, contra una BD H2 en memoria. */
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "spring.datasource.url=jdbc:h2:mem:nexou;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
                "spring.datasource.username=sa",
                "spring.datasource.password=",
                "spring.datasource.driver-class-name=org.h2.Driver",
                "spring.jpa.hibernate.ddl-auto=create-drop",
                "app.jwt.secret=secreto-de-pruebas-con-mas-de-32-caracteres-ok",
                "app.admin.usuario=admin",
                "app.admin.contrasena=admin1234"
        })
class FlujoCompletoTest {

    @LocalServerPort
    int puerto;

    private final HttpClient http = HttpClient.newHttpClient();

    private HttpResponse<String> llamar(String metodo, String ruta, String token, String cuerpo) throws Exception {
        HttpRequest.Builder b = HttpRequest.newBuilder(URI.create("http://localhost:" + puerto + ruta))
                .header("Content-Type", "application/json");
        if (token != null) {
            b.header("Authorization", "Bearer " + token);
        }
        b.method(metodo, cuerpo == null ? HttpRequest.BodyPublishers.noBody() : HttpRequest.BodyPublishers.ofString(cuerpo));
        return http.send(b.build(), HttpResponse.BodyHandlers.ofString());
    }

    private String token(HttpResponse<String> r) {
        Matcher m = Pattern.compile("\"token\":\"([^\"]+)\"").matcher(r.body());
        assertTrue(m.find(), "No vino token: " + r.body());
        return m.group(1);
    }

    @Test
    void flujoDeRegistroAdminYReservas() throws Exception {
        // Registro con solo usuario y contraseña
        var registro = llamar("POST", "/api/auth/registro", null, "{\"usuario\":\"ines\",\"contrasena\":\"1234\"}");
        assertEquals(201, registro.statusCode(), registro.body());
        assertTrue(registro.body().contains("\"rol\":\"USER\""));
        assertTrue(!registro.body().contains("contrasena"), "No debe filtrar la contraseña");
        String tokenAna = token(registro);

        // Usuario repetido (sin importar mayúsculas)
        assertEquals(400, llamar("POST", "/api/auth/registro", null, "{\"usuario\":\"INES\",\"contrasena\":\"1234\"}").statusCode());

        // Login correcto e incorrecto
        assertEquals(200, llamar("POST", "/api/auth/login", null, "{\"usuario\":\"ines\",\"contrasena\":\"1234\"}").statusCode());
        assertEquals(401, llamar("POST", "/api/auth/login", null, "{\"usuario\":\"ines\",\"contrasena\":\"mala\"}").statusCode());

        // Sin token no se entra; con token de USER no se publica
        assertEquals(401, llamar("GET", "/api/libros", null, null).statusCode());
        assertEquals(200, llamar("GET", "/api/libros", tokenAna, null).statusCode());
        String libroJson = "{\"nomLibro\":\"Java Basico\",\"isbn\":\"111-222\",\"cantidadTotal\":2,\"diasPrestamoMax\":7,\"categoriaIds\":[]}";
        assertEquals(403, llamar("POST", "/api/libros", tokenAna, libroJson).statusCode());
        assertEquals(403, llamar("GET", "/api/usuarios", tokenAna, null).statusCode());
        assertEquals(403, llamar("GET", "/api/reservas-libros", tokenAna, null).statusCode());

        // El admin sembrado publica un libro
        var loginAdmin = llamar("POST", "/api/auth/login", null, "{\"usuario\":\"admin\",\"contrasena\":\"admin1234\"}");
        assertEquals(200, loginAdmin.statusCode(), loginAdmin.body());
        assertTrue(loginAdmin.body().contains("\"rol\":\"ADMIN\""));
        String tokenAdmin = token(loginAdmin);
        var libro = llamar("POST", "/api/libros", tokenAdmin, libroJson);
        assertEquals(201, libro.statusCode(), libro.body());
        Matcher id = Pattern.compile("\"id\":(\\d+)").matcher(libro.body());
        assertTrue(id.find());
        String libroId = id.group(1);

        // La usuaria reserva a su nombre (aunque intente suplantar a otro)
        var reserva = llamar("POST", "/api/reservas-libros", tokenAna,
                "{\"libroId\":" + libroId + ",\"diasPrestamo\":5,\"usuarioId\":1}");
        assertEquals(201, reserva.statusCode(), reserva.body());
        assertTrue(reserva.body().contains("\"usuario\":\"ines\""), reserva.body());
        assertTrue(reserva.body().contains("\"estadoReserva\":\"ACTIVA\""));
        Matcher rid = Pattern.compile("\"id\":(\\d+)").matcher(reserva.body());
        assertTrue(rid.find());

        var mis = llamar("GET", "/api/reservas-libros/mis-reservas", tokenAna, null);
        assertEquals(200, mis.statusCode());
        assertTrue(mis.body().contains("Java Basico"));

        // Pasar de los días máximos es regla de negocio
        assertEquals(400, llamar("POST", "/api/reservas-libros", tokenAna,
                "{\"libroId\":" + libroId + ",\"diasPrestamo\":30}").statusCode());

        // El admin ve todo y la usuaria devuelve el libro
        assertEquals(200, llamar("GET", "/api/reservas-libros", tokenAdmin, null).statusCode());
        var devolucion = llamar("PATCH", "/api/reservas-libros/" + rid.group(1) + "/devolucion", tokenAna, null);
        assertEquals(200, devolucion.statusCode(), devolucion.body());
        assertTrue(devolucion.body().contains("\"estadoReserva\":\"DEVUELTO\""));

        // Otro usuario no puede tocar la reserva ajena
        String tokenLuis = token(llamar("POST", "/api/auth/registro", null, "{\"usuario\":\"hugo\",\"contrasena\":\"1234\"}"));
        assertEquals(403, llamar("GET", "/api/reservas-libros/" + rid.group(1), tokenLuis, null).statusCode());

        // Token falso
        assertEquals(401, llamar("GET", "/api/libros", "token.falso.aqui", null).statusCode());
    }

    private HttpResponse<byte[]> subir(String ruta, String token, byte[] contenido) throws Exception {
        String limite = "----nexou" + System.nanoTime();
        String salto = "\r\n";
        byte[] inicio = ("--" + limite + salto
                + "Content-Disposition: form-data; name=\"archivo\"; filename=\"foto.png\"" + salto
                + "Content-Type: image/png" + salto + salto).getBytes();
        byte[] fin = (salto + "--" + limite + "--" + salto).getBytes();
        byte[] cuerpo = new byte[inicio.length + contenido.length + fin.length];
        System.arraycopy(inicio, 0, cuerpo, 0, inicio.length);
        System.arraycopy(contenido, 0, cuerpo, inicio.length, contenido.length);
        System.arraycopy(fin, 0, cuerpo, inicio.length + contenido.length, fin.length);
        HttpRequest.Builder b = HttpRequest.newBuilder(URI.create("http://localhost:" + puerto + ruta))
                .header("Content-Type", "multipart/form-data; boundary=" + limite)
                .PUT(HttpRequest.BodyPublishers.ofByteArray(cuerpo));
        if (token != null) {
            b.header("Authorization", "Bearer " + token);
        }
        return http.send(b.build(), HttpResponse.BodyHandlers.ofByteArray());
    }

    @Test
    void imagenesDeArticulos() throws Exception {
        String tokenAdmin = token(llamar("POST", "/api/auth/login", null, "{\"usuario\":\"admin\",\"contrasena\":\"admin1234\"}"));
        String tokenUsuario = token(llamar("POST", "/api/auth/registro", null, "{\"usuario\":\"bea\",\"contrasena\":\"1234\"}"));
        var libro = llamar("POST", "/api/libros", tokenAdmin,
                "{\"nomLibro\":\"Con foto\",\"isbn\":\"999-001\",\"cantidadTotal\":1,\"diasPrestamoMax\":7}");
        assertEquals(201, libro.statusCode(), libro.body());
        assertTrue(libro.body().contains("\"imagenUrl\":null"), "Sin imagen todavía");
        Matcher id = Pattern.compile("\"id\":(\\d+)").matcher(libro.body());
        assertTrue(id.find());
        String ruta = "/api/libros/" + id.group(1) + "/imagen";

        // PNG mínimo (solo importa la firma de los primeros bytes)
        byte[] png = {(byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A, 0, 0, 0, 0};

        // Un usuario normal no puede subir; sin token tampoco
        assertEquals(403, subir(ruta, tokenUsuario, png).statusCode());
        assertEquals(401, subir(ruta, null, png).statusCode());
        // Un archivo que no es imagen se rechaza aunque diga ser PNG
        assertEquals(400, subir(ruta, tokenAdmin, "<html>hola</html>".getBytes()).statusCode());

        // El admin sube la imagen y el libro pasa a tener imagenUrl
        var subida = subir(ruta, tokenAdmin, png);
        assertEquals(200, subida.statusCode(), new String(subida.body()));
        assertTrue(new String(subida.body()).contains("\"imagenUrl\":\"" + ruta + "?v="));

        // La descarga es pública y devuelve el tipo correcto
        var descarga = http.send(HttpRequest.newBuilder(URI.create("http://localhost:" + puerto + ruta)).GET().build(),
                HttpResponse.BodyHandlers.ofByteArray());
        assertEquals(200, descarga.statusCode());
        assertEquals("image/png", descarga.headers().firstValue("Content-Type").orElse(""));
        assertEquals(png.length, descarga.body().length);

        // Al quitarla, vuelve a no haber imagen
        assertEquals(204, llamar("DELETE", ruta, tokenAdmin, null).statusCode());
        assertEquals(404, http.send(HttpRequest.newBuilder(URI.create("http://localhost:" + puerto + ruta)).GET().build(),
                HttpResponse.BodyHandlers.ofString()).statusCode());
    }

    @Test
    void perfilYDevolucionDeEquipos() throws Exception {
        String tokenAdmin = token(llamar("POST", "/api/auth/login", null, "{\"usuario\":\"admin\",\"contrasena\":\"admin1234\"}"));
        String tokenUsuario = token(llamar("POST", "/api/auth/registro", null, "{\"usuario\":\"carla\",\"contrasena\":\"1234\"}"));

        // Perfil opcional: semestre y programa (el registro sigue pidiendo solo usuario y contrasena)
        var perfil = llamar("PUT", "/api/auth/perfil", tokenUsuario, "{\"semestre\":4,\"programa\":\"Analitica\"}");
        assertEquals(200, perfil.statusCode(), perfil.body());
        assertTrue(perfil.body().contains("\"semestre\":4"));
        assertEquals(400, llamar("PUT", "/api/auth/perfil", tokenUsuario, "{\"semestre\":15}").statusCode());
        assertEquals(401, llamar("PUT", "/api/auth/perfil", null, "{\"semestre\":2}").statusCode());

        // Al devolver un equipo se guarda cuando se devolvió (sirve para medir retrasos)
        var equipo = llamar("POST", "/api/equipos", tokenAdmin,
                "{\"nomEquipo\":\"Camara\",\"cantidadTotal\":1,\"duracionMaximaHrs\":3}");
        assertEquals(201, equipo.statusCode(), equipo.body());
        Matcher idEquipo = Pattern.compile("\"id\":(\\d+)").matcher(equipo.body());
        assertTrue(idEquipo.find());
        String inicio = java.time.LocalDateTime.now().plusHours(1).withNano(0).toString();
        String fin = java.time.LocalDateTime.now().plusHours(2).withNano(0).toString();
        var reserva = llamar("POST", "/api/reservas-equipos", tokenUsuario,
                "{\"equipoId\":" + idEquipo.group(1) + ",\"horaInicio\":\"" + inicio + "\",\"horaFin\":\"" + fin + "\"}");
        assertEquals(201, reserva.statusCode(), reserva.body());
        assertTrue(reserva.body().contains("\"usuarioSemestre\":4"), reserva.body());
        Matcher idReserva = Pattern.compile("\"id\":(\\d+)").matcher(reserva.body());
        assertTrue(idReserva.find());
        var devuelta = llamar("PATCH", "/api/reservas-equipos/" + idReserva.group(1) + "/devolucion", tokenUsuario, null);
        assertEquals(200, devuelta.statusCode(), devuelta.body());
        assertTrue(!devuelta.body().contains("\"fechaDevolucionReal\":null"), devuelta.body());
    }

    @Test
    void articulosDelSeedTraenImagen() throws Exception {
        String tokenAdmin = token(llamar("POST", "/api/auth/login", null, "{\"usuario\":\"admin\",\"contrasena\":\"admin1234\"}"));
        String libros = llamar("GET", "/api/libros", tokenAdmin, null).body();
        String equipos = llamar("GET", "/api/equipos", tokenAdmin, null).body();
        assertTrue(libros.split("\"imagenUrl\":\"/api/libros/", -1).length - 1 >= 5, "Los 5 libros del seed deben tener imagen");
        assertTrue(equipos.split("\"imagenUrl\":\"/api/equipos/", -1).length - 1 >= 3, "Los 3 equipos del seed deben tener imagen");

        Matcher url = Pattern.compile("\"imagenUrl\":\"([^\"]+)\"").matcher(libros);
        assertTrue(url.find());
        var descarga = http.send(HttpRequest.newBuilder(URI.create("http://localhost:" + puerto + url.group(1))).GET().build(),
                HttpResponse.BodyHandlers.ofByteArray());
        assertEquals(200, descarga.statusCode());
        assertEquals("image/jpeg", descarga.headers().firstValue("Content-Type").orElse(""));
        assertTrue(descarga.body().length > 5_000, "La imagen no debe estar vacía");
    }
}
