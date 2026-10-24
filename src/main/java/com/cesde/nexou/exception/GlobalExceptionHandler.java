package com.cesde.nexou.exception;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.ErrorResponse;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import jakarta.validation.ConstraintViolationException;

/**
 * Intercepta las excepciones lanzadas en los servicios y las convierte en
 * respuestas JSON con el formato {"mensaje": "..."} y el código HTTP adecuado.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<Map<String, String>> manejarNoEncontrado(RecursoNoEncontradoException ex) {
        Map<String, String> respuesta = new HashMap<>();
        respuesta.put("mensaje", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(respuesta);
    }

    @ExceptionHandler(ReglaDeNegocioException.class)
    public ResponseEntity<Map<String, String>> manejarReglaDeNegocio(ReglaDeNegocioException ex) {
        Map<String, String> respuesta = new HashMap<>();
        respuesta.put("mensaje", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(respuesta);
    }

    // Errores de validación de los cuerpos de entrada.
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> manejarValidacion(MethodArgumentNotValidException ex) {
        String detalle = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));
        Map<String, String> respuesta = new HashMap<>();
        respuesta.put("mensaje", "Datos inválidos - " + detalle);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(respuesta);
    }

    // Validaciones que se disparan al guardar (por ejemplo, en objetos anidados del body).
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, String>> manejarRestriccionDeValidacion(ConstraintViolationException ex) {
        String detalle = ex.getConstraintViolations().stream()
                .map(violacion -> violacion.getPropertyPath() + ": " + violacion.getMessage())
                .collect(Collectors.joining(", "));
        Map<String, String> respuesta = new HashMap<>();
        respuesta.put("mensaje", "Datos inválidos - " + detalle);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(respuesta);
    }

    // Body ausente, JSON mal formado o un campo con formato incorrecto (fecha, número, enum).
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, String>> manejarCuerpoIlegible(HttpMessageNotReadableException ex) {
        Map<String, String> respuesta = new HashMap<>();
        respuesta.put("mensaje", "El cuerpo de la petición no es un JSON válido o tiene campos con formato incorrecto");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(respuesta);
    }

    // Valor de ruta o de consulta con un tipo equivocado, por ejemplo /api/usuarios/abc.
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, String>> manejarTipoDeParametro(MethodArgumentTypeMismatchException ex) {
        Map<String, String> respuesta = new HashMap<>();
        respuesta.put("mensaje", "El parámetro '" + ex.getName() + "' tiene un valor inválido: " + ex.getValue());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(respuesta);
    }

    // Parámetro de consulta obligatorio que no se envió, por ejemplo diasExtra en la renovación.
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<Map<String, String>> manejarParametroFaltante(MissingServletRequestParameterException ex) {
        Map<String, String> respuesta = new HashMap<>();
        respuesta.put("mensaje", "Falta el parámetro obligatorio: " + ex.getParameterName());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(respuesta);
    }

    // Ruta que no corresponde a ningún endpoint.
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<Map<String, String>> manejarRutaNoEncontrada(NoResourceFoundException ex) {
        Map<String, String> respuesta = new HashMap<>();
        respuesta.put("mensaje", "Ruta no encontrada: /" + ex.getResourcePath());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(respuesta);
    }

    // Restricciones de la base de datos (valor duplicado, texto demasiado largo, referencia inválida).
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, String>> manejarIntegridadDeDatos(DataIntegrityViolationException ex) {
        Map<String, String> respuesta = new HashMap<>();
        respuesta.put("mensaje", "Los datos enviados no cumplen las restricciones de la base de datos "
                + "(valor duplicado, texto demasiado largo o referencia inválida)");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(respuesta);
    }

    // Cualquier otro error no previsto: se conserva el código HTTP propio de Spring si lo tiene; si no, 500.
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> manejarErrorNoPrevisto(Exception ex) {
        HttpStatusCode estado = ex instanceof ErrorResponse errorResponse
                ? errorResponse.getStatusCode()
                : HttpStatus.INTERNAL_SERVER_ERROR;
        Map<String, String> respuesta = new HashMap<>();
        respuesta.put("mensaje", estado.is5xxServerError()
                ? "Ocurrió un error interno en el servidor. Intente de nuevo más tarde"
                : "La petición no pudo procesarse: " + ex.getMessage());
        if (estado.is5xxServerError()) {
            log.error("Error no previsto al procesar la petición", ex);
        }
        return ResponseEntity.status(estado).body(respuesta);
    }
}
