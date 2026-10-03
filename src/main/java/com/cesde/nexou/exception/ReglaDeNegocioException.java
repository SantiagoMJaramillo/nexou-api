package com.cesde.nexou.exception;

/**
 * Se lanza cuando se rompe una regla de negocio (sin stock, límite de reservas,
 * datos duplicados, estado inválido...). El GlobalExceptionHandler la traduce
 * a HTTP 400 Bad Request.
 */
public class ReglaDeNegocioException extends RuntimeException {

    public ReglaDeNegocioException(String mensaje) {
        super(mensaje);
    }
}
