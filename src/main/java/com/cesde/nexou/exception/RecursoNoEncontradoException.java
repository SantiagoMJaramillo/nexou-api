package com.cesde.nexou.exception;

/**
 * Se lanza cuando un recurso buscado (usuario, libro, reserva...) no existe.
 * El GlobalExceptionHandler la traduce a HTTP 404 Not Found.
 */
public class RecursoNoEncontradoException extends RuntimeException {

    public RecursoNoEncontradoException(String mensaje) {
        super(mensaje);
    }
}
