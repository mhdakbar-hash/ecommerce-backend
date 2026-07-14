package com.akbar.ecommerce_backend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {


    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleEmailAlreadyExistsException(EmailAlreadyExistsException ex){

        ErrorResponse response = new ErrorResponse();

        response.setStatus(HttpStatus.CONFLICT.value());
        response.setMessage(ex.getMessage());
        response.setTimeStamp(LocalDateTime.now());

        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(response);

    }


}
