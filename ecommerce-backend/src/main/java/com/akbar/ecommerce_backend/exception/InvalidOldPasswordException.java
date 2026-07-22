package com.akbar.ecommerce_backend.exception;

public class InvalidOldPasswordException extends RuntimeException{

    public InvalidOldPasswordException(String message){
        super(message);
    }
}
