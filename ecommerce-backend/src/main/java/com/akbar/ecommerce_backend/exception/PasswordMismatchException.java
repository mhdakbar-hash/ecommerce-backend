package com.akbar.ecommerce_backend.exception;

public class PasswordMismatchException extends RuntimeException{

    public PasswordMismatchException(String message){
        super(message);
    }
}
