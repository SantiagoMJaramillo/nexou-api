package com.cesde.nexou.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** DTOs de autenticación: el registro solo pide usuario y contraseña. */
public final class AuthDto {

    private AuthDto() {
    }

    public record Registro(
            @NotBlank(message = "el usuario es obligatorio")
            @Size(min = 3, max = 50, message = "debe tener entre 3 y 50 caracteres") String usuario,
            @NotBlank(message = "la contraseña es obligatoria")
            @Size(min = 4, max = 100, message = "debe tener entre 4 y 100 caracteres") String contrasena) {
    }

    public record Login(
            @NotBlank(message = "el usuario es obligatorio") String usuario,
            @NotBlank(message = "la contraseña es obligatoria") String contrasena) {
    }

    /** Datos opcionales del perfil; sirven para las estadísticas de la institución. */
    public record Perfil(
            @jakarta.validation.constraints.Min(value = 1, message = "debe ser entre 1 y 12")
            @jakarta.validation.constraints.Max(value = 12, message = "debe ser entre 1 y 12") Integer semestre,
            @Size(max = 100, message = "no puede superar los 100 caracteres") String programa) {
    }

    public record Respuesta(String token, UsuarioDto.Response usuario) {
    }
}
