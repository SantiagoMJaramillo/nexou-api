# NEXOU API

Sistema de reservas de biblioteca y equipos tecnológicos (Avance 1 — Modelado de dominio y mapeo de entidades JPA).

Proyecto integrador desarrollado por un equipo de 3 integrantes:

- **Integrante A** — Infraestructura: repositorio, proyecto Spring Boot, conexión a PostgreSQL (Neon/Supabase), estructura de paquetes.
- **Integrante B** — Núcleo de dominio: `BaseEntity`, `Usuario`, `ConfiguracionUsuario`, `Categoria`, `Libro`, embeddable `Ubicacion`.
- **Integrante C** — Equipos y reservas: enum `EstadoReserva`, `EquipoTecnologico`, `ReservaLibro`, `ReservaEquipo`.

## Stack

Spring Boot · Spring Data JPA · PostgreSQL · Lombok · Maven

## Estructura de paquetes

```
src/main/java/com/cesde/nexou/
├── model/base/        (BaseEntity)
├── model/enums/       (EstadoReserva)
├── model/embeddable/  (Ubicacion)
└── model/entity/      (7 entidades)
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

## Flujo de trabajo

- Nunca se commitea directo a `main` ni `develop`: siempre `feature/nombre-funcionalidad` → Pull Request → revisión de al menos 1 compañero → merge a `develop`.
- `main` queda protegida; el merge final `develop → main` se hace solo cuando todo compila y el checklist de entrega está completo.

## Cómo compilar

```
./mvnw clean compile
```

Requiere Java 21 y variables de entorno `DB_URL`, `DB_USER`, `DB_PASSWORD` para la conexión a PostgreSQL.
