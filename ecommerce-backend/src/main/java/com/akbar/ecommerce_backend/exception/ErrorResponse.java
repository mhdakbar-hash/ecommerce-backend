package com.akbar.ecommerce_backend.exception;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ErrorResponse {

    private  int status;

    private String message;

    private LocalDateTime timeStamp;

    private Map<String, String> errors;
}
