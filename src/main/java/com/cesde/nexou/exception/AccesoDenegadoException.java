package com.cesde.nexou.exception;

/** Se lanza cuando el usuario autenticado no puede acceder a un recurso ajeno (HTTP 403). */
public class AccesoDenegadoException extends RuntimeException {
    public AccesoDenegadoException(String mensaje) {
        super(mensaje);
    }
}
