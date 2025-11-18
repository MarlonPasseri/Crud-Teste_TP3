package com.crudsystem.exception;

/**
 * Exceção lançada quando um produto não é encontrado no sistema.
 */
public class ProductNotFoundException extends Exception {
    private static final long serialVersionUID = 1L;

    public ProductNotFoundException(String message) {
        super(message);
    }

    public ProductNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public ProductNotFoundException(Long id) {
        super("Produto com ID " + id + " não encontrado");
    }
}
