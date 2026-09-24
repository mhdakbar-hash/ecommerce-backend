package com.akbar.ecommerce_backend.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mapping.PropertyReferenceException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {


    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleEmailAlreadyExistsException(EmailAlreadyExistsException ex){

        log.warn("Registration failed. Email already exists: {}", ex.getMessage());

        ErrorResponse response = new ErrorResponse();

        response.setStatus(HttpStatus.CONFLICT.value());
        response.setMessage(ex.getMessage());
        response.setTimeStamp(LocalDateTime.now());

        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(response);

    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException ex) {

        ErrorResponse response = new ErrorResponse();
        Map<String, String> errorsMap = new HashMap<>();

        List<FieldError> fieldErrors = ex.getBindingResult().getFieldErrors();

        for (FieldError error : fieldErrors) {
            errorsMap.put(error.getField(), error.getDefaultMessage());
        }

        log.warn("Validation failed: {}", errorsMap);

        response.setStatus(HttpStatus.BAD_REQUEST.value());
        response.setMessage("Validation Failed");
        response.setTimeStamp(LocalDateTime.now());
        response.setErrors(errorsMap);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(response);

    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(ResourceNotFoundException ex){

        log.warn("Resource not found: {}", ex.getMessage());

        ErrorResponse response = new ErrorResponse();

        response.setStatus(HttpStatus.NOT_FOUND.value());
        response.setMessage(ex.getMessage());
        response.setTimeStamp(LocalDateTime.now());

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(response);
    }


    @ExceptionHandler ({
            PasswordMismatchException.class,
            InvalidOldPasswordException.class,
            SamePasswordException.class})

    public ResponseEntity<ErrorResponse> handlePasswordExceptions(RuntimeException exception){

        log.warn("Password operation failed: {}", exception.getMessage());

        ErrorResponse response = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                exception.getMessage(),
                LocalDateTime.now(),
                null);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(response);
    }


    @ExceptionHandler(PropertyReferenceException.class)
    public ResponseEntity<ErrorResponse> handlePropertyReferenceException(PropertyReferenceException exception){

        log.warn("Invalid sort field requested: {}", exception.getPropertyName());

        ErrorResponse response = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Invalid sort field: " + exception.getPropertyName(),
                LocalDateTime.now(),
                null
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(response);
    }


    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGlobalException(Exception exception){

        log.error("Unexpected exception occurred.",exception);

        ErrorResponse response = new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Something went wrong. Please try again later.",
                LocalDateTime.now(),
                null
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(response);
    }



    @ExceptionHandler(SkuAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleSkuAlreadyExistsException(SkuAlreadyExistsException exception) {

        log.warn("Product creation failed. Sku already exists: {}", exception.getMessage());

        ErrorResponse response = new ErrorResponse(
                HttpStatus.CONFLICT.value(),
                exception.getMessage(),
                LocalDateTime.now(),
                null
        );

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(response);


    }



    @ExceptionHandler(CategoryAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleCategoryAlreadyExistsException(CategoryAlreadyExistsException exception) {

        log.warn("Category creation failed. Category already exists: {}", exception.getMessage());

        ErrorResponse response = new ErrorResponse(
                HttpStatus.CONFLICT.value(),
                exception.getMessage(),
                LocalDateTime.now(),
                null
        );

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(response);

    }


    @ExceptionHandler(CategoryInUseException.class)
    public ResponseEntity<ErrorResponse> handleCategoryInUseException(CategoryInUseException exception) {

        log.warn("Category deletion failed. Category is in use: {}",
                exception.getMessage());

        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.CONFLICT.value(),
                exception.getMessage(),
                LocalDateTime.now(),
                null);

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(errorResponse);
    }



    @ExceptionHandler(InventoryAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleInventoryAlreadyExistsException(InventoryAlreadyExistsException exception) {

        log.warn("Inventory creation failed. Inventory already exists: {}",
                exception.getMessage());

        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.CONFLICT.value(),
                exception.getMessage(),
                LocalDateTime.now(),
                null);

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(errorResponse);
    }



    @ExceptionHandler(InventoryStockInsufficientException.class)
    public ResponseEntity<ErrorResponse> handleInventoryStockInsufficientException(
            InventoryStockInsufficientException exception) {

        log.warn("Stock removal failed. Insufficient inventory stock: {}",
                exception.getMessage());

        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.CONFLICT.value(),
                exception.getMessage(),
                LocalDateTime.now(),
                null);

        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(errorResponse);
    }



    @ExceptionHandler(CartEmptyException.class)
    public ResponseEntity<ErrorResponse> handleCartEmptyException(CartEmptyException exception) {

        log.warn("Order creation failed. Cart is empty: {}",
                exception.getMessage());

        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.CONFLICT.value(),
                exception.getMessage(),
                LocalDateTime.now(),
                null);

        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(errorResponse);
    }




    @ExceptionHandler(OrderCancellationException.class)
    public ResponseEntity<ErrorResponse> handleOrderCancellationException(OrderCancellationException exception) {

        log.warn("Order cancellation failed: {}",
                exception.getMessage());

        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.CONFLICT.value(),
                exception.getMessage(),
                LocalDateTime.now(),
                null);

        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(errorResponse);

    }




    @ExceptionHandler(PaymentInitiationException.class)
    public ResponseEntity<ErrorResponse> handlePaymentInitiationException(PaymentInitiationException exception) {

        log.warn("Payment initiation failed: {}",
                exception.getMessage());

        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.CONFLICT.value(),
                exception.getMessage(),
                LocalDateTime.now(),
                null);

        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(errorResponse);

    }




    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadableException(
            HttpMessageNotReadableException exception) {


        log.warn("Request body could not be parsed: {}",
                exception.getMessage());

        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Invalid request body",
                LocalDateTime.now(),
                null);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(errorResponse);

    }




    @ExceptionHandler(PaymentRetryException.class)
    public ResponseEntity<ErrorResponse> handlePaymentRetryException(PaymentRetryException exception) {

        log.warn("Payment retry failed: {}",
                exception.getMessage());

        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.CONFLICT.value(),
                exception.getMessage(),
                LocalDateTime.now(),
                null);

        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(errorResponse);

    }




    @ExceptionHandler(PaymentRefundException.class)
    public ResponseEntity<ErrorResponse> handlePaymentRefundException(PaymentRefundException exception) {

        log.warn("Payment refund failed: {}",
                exception.getMessage());

        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.CONFLICT.value(),
                exception.getMessage(),
                LocalDateTime.now(),
                null);

        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(errorResponse);
    }


}
