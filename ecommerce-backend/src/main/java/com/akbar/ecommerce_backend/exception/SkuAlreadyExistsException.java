package com.akbar.ecommerce_backend.exception;

public class SkuAlreadyExistsException extends RuntimeException {

    public SkuAlreadyExistsException(String message) {
        super(message);
    }
}
