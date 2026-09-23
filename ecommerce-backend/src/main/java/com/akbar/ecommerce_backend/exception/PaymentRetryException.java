package com.akbar.ecommerce_backend.exception;

public class PaymentRetryException extends RuntimeException {

    public PaymentRetryException(String message) {
        super(message);
    }
}
