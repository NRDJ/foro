package com.duoc.foro.moderacion.exception;

/** ms-foros no respondió o respondió con un error inesperado. */
public class ServicioNoDisponibleException extends RuntimeException {

    public ServicioNoDisponibleException(String mensaje) {
        super(mensaje);
    }
}
