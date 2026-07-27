package com.akbar.ecommerce_backend.exception;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ErrorResponse {

    @Schema(
            description = "HTTP status code",
            example = "404"
    )
    private  int status;


    @Schema(
            description = "Description of the error",
            example = "User not found"
    )
    private String message;


    @Schema(
            description = "Date and time when the error occurred",
            example = "2026-07-27T14:20:15"
    )
    private LocalDateTime timeStamp;


    @Schema(
            description = "Validation errors where the key is the field name and the value is the validation message"
    )
    private Map<String, String> errors;
}
