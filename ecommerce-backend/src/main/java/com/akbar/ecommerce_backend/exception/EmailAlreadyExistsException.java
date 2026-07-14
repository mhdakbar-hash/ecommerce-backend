package com.akbar.ecommerce_backend.exception;

public class EmailAlreadyExistsException extends RuntimeException{

    public EmailAlreadyExistsException(String message){

        super(message);
    }
}
