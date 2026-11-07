package com.cesde.nexou.dto;

import java.time.LocalDateTime;

import com.cesde.nexou.model.entity.Usuario;
import com.cesde.nexou.model.enums.Rol;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** DTOs de usuario (uso del administrador). El registro público usa AuthDto. */
public final class UsuarioDto {

    private UsuarioDto() {
    }

    public record Request(
            @NotBlank(message = "el usuario es obligatorio")
            @Size(max = 100, message = "no puede superar los 100 caracteres") String usuario,
            @Size(min = 4, max = 100, message = "debe tener entre 4 y 100 caracteres") String contrasena,
            @Email(message = "no es un correo válido")
            @Size(max = 150, message = "no puede superar los 150 caracteres") String correo,
            Rol rol,
            @Size(max = 20, message = "no puede superar los 20 caracteres") String codigoEstudiante,
            @Size(max = 100, message = "no puede superar los 100 caracteres") String programa,
            Integer semestre,
            @Size(max = 20, message = "no puede superar los 20 caracteres") String telefono,
            Boolean estadoActivo) {

        public Usuario aEntidad() {
            Usuario u = new Usuario();
            u.setNombreUsuario(usuario == null ? null : usuario.trim());
            u.setContrasena(contrasena);
            u.setCorreo(correo == null || correo.isBlank() ? null : correo.trim());
            u.setRol(rol);
            u.setCodigoEstudiante(codigoEstudiante);
            u.setPrograma(programa);
            u.setSemestre(semestre);
            u.setTelefono(telefono);
            u.setEstadoActivo(estadoActivo);
            return u;
        }
    }

    public record Response(
            Long id,
            String usuario,
            String correo,
            Rol rol,
            String codigoEstudiante,
            String programa,
            Integer semestre,
            String telefono,
            Boolean estadoActivo,
            LocalDateTime fechaCreacion) {

        public static Response de(Usuario u) {
            return new Response(u.getId(), u.getNombreUsuario(), u.getCorreo(),
                    u.getRol() != null ? u.getRol() : Rol.USER,
                    u.getCodigoEstudiante(), u.getPrograma(), u.getSemestre(), u.getTelefono(),
                    u.getEstadoActivo(), u.getFechaCreacion());
        }
    }
}
