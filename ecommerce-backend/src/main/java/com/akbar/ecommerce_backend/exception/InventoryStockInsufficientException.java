package com.akbar.ecommerce_backend.exception;

public class InventoryStockInsufficientException extends RuntimeException {

    public InventoryStockInsufficientException(String message) {
        super(message);
    }
}
