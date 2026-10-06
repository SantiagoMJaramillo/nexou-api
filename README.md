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

Las reservas usan directamente sus entidades JPA como cuerpo y respuesta JSON; no se
requieren DTOs. Para crear una reserva de libro se envía `usuario.id`, `libro.id` y
`diasPrestamo`. Para crear una reserva de equipo se envía `usuario.id`, `equipo.id`,
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

`DataSeederConfig` siembra al arrancar, solo si no hay usuarios: 3 categorías, 5 libros, 2 usuarios con su configuración (`laura.restrepo@cesde.net`, `carlos.gomez@cesde.net`), 3 equipos, 3 reservas de libros y 2 de equipos, con el stock ya descontado.

## Flujo de trabajo

- Nunca se commitea directo a `main` ni `develop`: siempre `feature/nombre-funcionalidad` → Pull Request → revisión de al menos 1 compañero → merge a `develop`.
- `main` queda protegida; el merge final `develop → main` se hace solo cuando todo compila y el checklist de entrega está completo.

## Cómo compilar

```
./mvnw clean compile
```

Requiere Java 21 y variables de entorno `DB_URL`, `DB_USER`, `DB_PASSWORD` para la conexión a PostgreSQL.
