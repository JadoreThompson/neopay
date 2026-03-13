package com.zenz.crypto_payment_gateway.api;

import com.zenz.crypto_payment_gateway.api.error.ResourceNotFound;
import com.zenz.crypto_payment_gateway.api.error.ServerError;
import com.zenz.crypto_payment_gateway.api.model.response.ErrorResponse;
import com.zenz.crypto_payment_gateway.api.model.response.SimpleErrorDetail;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.http.converter.HttpMessageNotReadableException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 400

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadable(HttpMessageNotReadableException exc) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(new SimpleErrorDetail("Invalid message received")));
    }

    @ExceptionHandler(ResourceNotFound.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(ResourceNotFound exc) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(new SimpleErrorDetail(exc.getMessage())));
    }

    // 500

    @ExceptionHandler(ServerError.class)
    public ResponseEntity<ErrorResponse> handleServerError(ServerError exc) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse(new SimpleErrorDetail(exc.getMessage())));
    }

    // Generic

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleGenericException(RuntimeException exc) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse(new SimpleErrorDetail("An unexpected error occurred " + exc.getClass().getName())));
    }
}
