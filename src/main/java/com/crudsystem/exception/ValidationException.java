package com.crudsystem.exception;

/**
 * Exceção lançada quando dados de entrada são inválidos.
 */
public class ValidationException extends Exception {
    private static final long serialVersionUID = 1L;

    public ValidationException(String message) {
        super(message);
    }

    public ValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
