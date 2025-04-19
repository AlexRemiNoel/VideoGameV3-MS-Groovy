package com.example.apigatewayservice.exception; // Adjust package name!

// No Spring annotations needed here if handled globally by @RestControllerAdvice
public class InvalidInputException extends RuntimeException {
    public InvalidInputException(String message) {
        super(message);
    }

    public InvalidInputException(Throwable cause) {
        super(cause);
    }

    public InvalidInputException(String message, Throwable cause) {
        super(message, cause);
    }
}