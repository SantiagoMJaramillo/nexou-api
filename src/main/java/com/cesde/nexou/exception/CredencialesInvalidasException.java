package com.cesde.nexou.exception;

/** Usuario o contraseña incorrectos (HTTP 401). */
public class CredencialesInvalidasException extends RuntimeException {
    public CredencialesInvalidasException(String mensaje) {
        super(mensaje);
    }
}
