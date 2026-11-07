# NEXOU API

Sistema de reservas de biblioteca y equipos tecnológicos: modelo de dominio JPA, API REST con CRUD para las 7 entidades, manejo global de excepciones, siembra de datos (seed) y documentación OpenAPI con Swagger UI y Scalar.

Proyecto integrador desarrollado por un equipo de 3 integrantes:

- **Integrante A** — Infraestructura: repositorio, proyecto Spring Boot, conexión a PostgreSQL (Neon/Supabase), estructura de paquetes.
- **Integrante B** — Núcleo de dominio: `BaseEntity`, `Usuario`, `ConfiguracionUsuario`, `Categoria`, `Libro`, embeddable `Ubicacion`.
- **Integrante C** — Equipos y reservas: enum `EstadoReserva`, `EquipoTecnologico`, `ReservaLibro`, `ReservaEquipo`.

## Stack

Spring Boot · Spring Data JPA · PostgreSQL · Lombok · Maven · springdoc-openapi (Swagger UI) · Scalar

## Estructura de paquetes

```
src/main/java/com/cesde/nexou/
├── config/            (OpenApiConfig, CorsConfig, DataSeederConfig)
├── controller/        (7 controladores REST)
├── exception/        (RecursoNoEncontradoException, ReglaDeNegocioException, GlobalExceptionHandler)
├── model/base/        (BaseEntity)
├── model/enums/       (EstadoReserva)
├── model/embeddable/  (Ubicacion)
├── model/entity/      (7 entidades)
├── repository/        (7 repositorios con métodos personalizados)
└── service/           (lógica y reglas de negocio)
```

## Modelo de dominio

`BaseEntity` (`@MappedSuperclass`) es heredada por las 7 entidades y aporta los atributos transversales:
`id`, `fechaCreacion`, `fechaActualizacion`, `estadoActivo` (con `@PrePersist`/`@PreUpdate` para las fechas).

### Entidades

| Entidad | Descripción |
|---|---|
| `Usuario` | Persona que reserva libros y equipos. |
| `ConfiguracionUsuario` | Preferencias del usuario (idioma, tema, notificaciones). |
| `Libro` | Ítem de biblioteca, con ubicación física embebida y categorías. |
| `Categoria` | Categoría temática de un libro. |
| `EquipoTecnologico` | Equipo tecnológico disponible para préstamo. |
| `ReservaLibro` | Préstamo de un libro a un usuario. |
| `ReservaEquipo` | Préstamo de un equipo tecnológico a un usuario. |

### Relaciones

```
Usuario 1───1 ConfiguracionUsuario        (@OneToOne)
Usuario 1───N ReservaLibro                (@OneToMany / @ManyToOne)
Usuario 1───N ReservaEquipo               (@OneToMany / @ManyToOne)
Libro   1───N ReservaLibro                (@OneToMany / @ManyToOne)
EquipoTecnologico 1───N ReservaEquipo     (@OneToMany / @ManyToOne)
Libro   N───N Categoria                   (@ManyToMany, tabla libro_categoria)
```

### Embeddable

`Ubicacion` (`sede`, `piso`, `referencia`) — embebida en `Libro` con `@Embedded`, reemplaza el antiguo campo `Ubicacion_Fisica` (varchar) del SQL original.

### Enum

`EstadoReserva` (`ACTIVA`, `DEVUELTO`, `VENCIDO`, `CANCELADA`) — mapeado con `@Enumerated(EnumType.STRING)` en `ReservaLibro` y `ReservaEquipo`.

## Documentación de la API

Con la aplicación corriendo:

| Recurso | URL |
|---|---|
| Scalar (visor moderno) | http://localhost:8080/scalar |
| Swagger UI | http://localhost:8080/swagger-ui.html |
| Especificación OpenAPI (JSON) | http://localhost:8080/v3/api-docs |

También se incluye `postman_collection.json` con ejemplos de todas las peticiones.

## Seguridad, roles y DTOs

La API usa **JWT** (Spring Security, sin sesiones) y contraseñas con **BCrypt**. Hay dos roles:

| Rol | Puede |
|---|---|
| `USER` | Registrarse, ver el catálogo, reservar a su nombre, ver/devolver/renovar **sus** reservas |
| `ADMIN` | Todo lo anterior + publicar/editar/eliminar libros, equipos y categorías, ver todas las reservas y usuarios |

| Endpoint público | Descripción |
|---|---|
| `POST /api/auth/registro` | Solo `{ "usuario", "contrasena" }` → crea cuenta `USER` y devuelve el token |
| `POST /api/auth/login` | `{ "usuario", "contrasena" }` → token JWT |
| `GET /health` | Health check (Render) |

El resto exige el header `Authorization: Bearer <token>`. En Swagger UI usa el botón **Authorize**.
`GET /api/auth/yo` devuelve el usuario del token y `GET /api/reservas-*/mis-reservas` sus reservas.

El **administrador inicial** se crea al arrancar (si no existe ninguno) con `ADMIN_USER` / `ADMIN_PASSWORD`.

Los controladores ya no exponen las entidades JPA: cada recurso tiene DTOs en el paquete `dto`
(`LibroDto.Request` / `LibroDto.Response`, etc.) con validación y sin datos sensibles (la contraseña nunca sale).
Las categorías de un libro se envían como `"categoriaIds": [1, 2]`.

### Perfil y datos para estadísticas

El registro sigue pidiendo solo usuario y contraseña. Cada persona puede completar, si quiere, su semestre (1-12) y su programa con
`PUT /api/auth/perfil` (`{"semestre": 4, "programa": "..."}`). Las respuestas de reservas incluyen `usuarioSemestre` y
`usuarioPrograma` para el análisis. Las reservas de equipos guardan además `fechaDevolucionReal` (para medir retrasos).

### Imágenes de artículos

Libros y equipos pueden tener una imagen (JPG, PNG o WEBP, máx. 1 MB; el frontend la reduce a ~900 px antes de subirla).
Se guarda en la base de datos (tabla `imagenes_articulo`) porque el disco de Render es efímero.

| Endpoint | Quién | Descripción |
|---|---|---|
| `PUT /api/libros/{id}/imagen` · `PUT /api/equipos/{id}/imagen` | ADMIN | Multipart, campo `archivo`. Reemplaza la imagen anterior |
| `DELETE /api/libros/{id}/imagen` · `DELETE /api/equipos/{id}/imagen` | ADMIN | Quita la imagen |
| `GET /api/libros/{id}/imagen` · `GET /api/equipos/{id}/imagen` | Público | Descarga la imagen (un `<img>` no puede enviar el token) |

Las respuestas de libros y equipos traen `imagenUrl` (relativa, con `?v=<versión>` para refrescar el caché) o `null`.
El tipo de archivo se valida por sus primeros bytes, no por el nombre.

**Imágenes de demostración:** los artículos del seed (5 libros y 3 equipos) traen una ilustración propia, guardada en
`src/main/resources/seed-images/`. `ImagenesInicialesConfig` las carga al arrancar solo si todavía no hay ninguna imagen guardada,
así que funciona también con una base ya existente y no pisa lo que suba el administrador. No son portadas reales: el admin puede
reemplazarlas desde el formulario de edición.
Para volver a generarlas: `pip install pillow` y `python scripts/generar_imagenes_demo.py` (escribe en `src/main/resources/seed-images/`).

### Variables de entorno

| Variable | Uso |
|---|---|
| `DB_URL`, `DB_USER`, `DB_PASSWORD` | PostgreSQL |
| `JWT_SECRET` | Secreto para firmar tokens (≥ 32 caracteres) |
| `ADMIN_USER`, `ADMIN_PASSWORD` | Administrador inicial |
| `CORS_ORIGENES` | URLs del frontend permitidas, separadas por coma (`*` solo en desarrollo) |
| `APP_TIMEZONE` | Zona horaria del servidor (por defecto `America/Bogota`) |

### Pruebas

`./mvnw test` ejecuta `FlujoCompletoTest` (BD H2 en memoria): registro, login, permisos por rol y reservas.

## Despliegue

Incluye `Dockerfile` y `render.yaml` para Render. Ver la guía `GUIA_DESPLIEGUE.md` de la carpeta raíz.

## Reservas (cuerpos)

Las reservas usan DTOs: no se envía el usuario (sale del token; solo un ADMIN puede indicar `usuarioId`). Para crear una reserva de libro se envía `libroId` y
`diasPrestamo`. Para crear una reserva de equipo se envía `equipoId`,
`horaInicio` y `horaFin`. La renovación de libros recibe `diasExtra` como parámetro:
`PATCH /api/reservas-libros/{id}/renovacion?diasExtra=5`.

## Endpoints

Cada entidad tiene CRUD completo (`GET` todos, `GET /{id}`, `POST`, `PUT /{id}`, `DELETE /{id}`) más un método personalizado:

| Recurso | Ruta base | Método personalizado |
|---|---|---|
| Usuarios | `/api/usuarios` | `GET /correo/{correo}` · `GET /{id}/validar-limite` |
| Configuración de usuario | `/api/configuraciones` | `GET /usuario/{usuarioId}` |
| Categorías | `/api/categorias` | `GET /nombre/{nombre}` |
| Libros | `/api/libros` | `GET /isbn/{isbn}` |
| Equipos tecnológicos | `/api/equipos` | `GET /disponibles` |
| Reservas de libros | `/api/reservas-libros` | `GET /usuario/{usuarioId}` · `PATCH /{id}/devolucion` · `PATCH /{id}/renovacion` |
| Reservas de equipos | `/api/reservas-equipos` | `GET /usuario/{usuarioId}` · `PATCH /{id}/devolucion` |

### Reglas de negocio principales

- Un usuario puede tener como máximo **3 reservas activas** entre libros y equipos.
- Solo se presta si hay stock (`cantidadDisponible > 0`); prestar descuenta 1 y devolver suma 1.
- Los días de préstamo no pueden superar `diasPrestamoMax` del libro; la renovación permite máximo 10 días extra y solo sobre reservas activas.
- La reserva de un equipo no puede superar su `duracionMaximaHrs` y la hora de fin debe ser posterior a la de inicio.
- Correo de usuario, ISBN de libro y nombre de categoría son únicos; cada usuario tiene una sola configuración.
- No se elimina un usuario, libro o equipo con reservas registradas (se desactiva con `estadoActivo = false`), ni una categoría con libros, ni una reserva activa.

## Manejo de errores

Todas las excepciones se convierten en JSON con el formato `{"mensaje": "..."}` desde `GlobalExceptionHandler`:

| Excepción | Código HTTP |
|---|---|
| `RecursoNoEncontradoException` | 404 Not Found |
| `ReglaDeNegocioException` | 400 Bad Request |
| Errores de validación del cuerpo | 400 Bad Request |

## Datos de prueba (seed)

`DataSeederConfig` siembra al arrancar, solo si no hay usuarios: 3 categorías, 5 libros, 2 usuarios con su configuración (usuarios `laura` y `carlos` (con la contraseña de la variable `SEED_PASSWORD`, o una aleatoria si no se define)), 3 equipos, 3 reservas de libros y 2 de equipos, con el stock ya descontado.

## Flujo de trabajo

- Nunca se commitea directo a `main` ni `develop`: siempre `feature/nombre-funcionalidad` → Pull Request → revisión de al menos 1 compañero → merge a `develop`.
- `main` queda protegida; el merge final `develop → main` se hace solo cuando todo compila y el checklist de entrega está completo.

## Cómo compilar

```
./mvnw clean compile
```

Requiere Java 25 (LTS) y variables de entorno `DB_URL`, `DB_USER`, `DB_PASSWORD` para la conexión a PostgreSQL, más `JWT_SECRET` y `ADMIN_PASSWORD`.
Para desarrollo local basta con copiar `.env.template` como `.env` y completar esos valores: la aplicación lo carga al arrancar (el archivo está en `.gitignore`).
