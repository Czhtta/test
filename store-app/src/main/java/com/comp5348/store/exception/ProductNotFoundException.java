package com.comp5348.store.exception;

/**
 * Product not found error
 * Throws an exception when the queried item does not exist.
 */
public class ProductNotFoundException extends RuntimeException {

    public ProductNotFoundException(String message) {
        super(message);
    }

    public ProductNotFoundException(Long id) {
        super("Product not found，ID: " + id);
    }
}