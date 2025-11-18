package com.crudsystem.exception;

/**
 * Exceção lançada quando ocorre erro em operações de acesso a dados.
 */
public class DataAccessException extends Exception {
    private static final long serialVersionUID = 1L;

    public DataAccessException(String message) {
        super(message);
    }

    public DataAccessException(String message, Throwable cause) {
        super(message, cause);
    }
}
