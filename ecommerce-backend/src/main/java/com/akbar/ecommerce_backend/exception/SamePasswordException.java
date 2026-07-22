package com.akbar.ecommerce_backend.exception;

public class SamePasswordException extends RuntimeException{

    public SamePasswordException(String message){
        super(message);
    }
}
