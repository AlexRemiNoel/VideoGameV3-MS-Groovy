package com.example.videogamev3.DownloadManagement.utils;

import com.example.videogamev3.DownloadManagement.utils.exceptions.DuplicateDownloadIDException;
import com.example.videogamev3.DownloadManagement.utils.exceptions.InvalidDownloadDataException;
import com.example.videogamev3.DownloadManagement.utils.exceptions.DownloadNotFoundException;
import jakarta.persistence.EntityNotFoundException; // Optional: If you might still throw this directly sometimes
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
// Import the correct WebFlux request object
import org.springframework.web.server.ServerWebExchange;

import static org.springframework.http.HttpStatus.*;

@RestControllerAdvice
@Slf4j
public class GlobalControllerExceptionHandler {

    // --- Handler for Download Not Found ---
    @ResponseStatus(NOT_FOUND) // 404
    @ExceptionHandler(DownloadNotFoundException.class)
    public HttpErrorInfo handleDownloadNotFoundException(ServerWebExchange exchange, DownloadNotFoundException ex) { // Use ServerWebExchange and specific exception
        return createHttpErrorInfo(NOT_FOUND, exchange, ex);
    }

    // --- Handler for Invalid Input Data ---
    // Changed to BAD_REQUEST (400) as it's more common for input validation
    @ResponseStatus(BAD_REQUEST) // 400
    @ExceptionHandler(InvalidDownloadDataException.class)
    public HttpErrorInfo handleInvalidInputException(ServerWebExchange exchange, InvalidDownloadDataException ex) { // Use ServerWebExchange and specific exception
        return createHttpErrorInfo(BAD_REQUEST, exchange, ex);
    }

    // --- Handler for Duplicate ID ---
    // Changed to CONFLICT (409) as it's more semantically correct for duplicates
    @ResponseStatus(CONFLICT) // 409
    @ExceptionHandler(DuplicateDownloadIDException.class)
    // Renamed method for clarity
    public HttpErrorInfo handleDuplicateDownloadIDException(ServerWebExchange exchange, DuplicateDownloadIDException ex) { // Use ServerWebExchange and specific exception
        return createHttpErrorInfo(CONFLICT, exchange, ex);
    }

    // --- Optional: Handler for generic JPA EntityNotFoundException ---
    // Keep this if findDownloadManagerOrFail might still throw the JPA version,
    // or if other parts of your app might throw it.
    @ResponseStatus(NOT_FOUND) // 404
    @ExceptionHandler(EntityNotFoundException.class)
    public HttpErrorInfo handleEntityNotFoundException(ServerWebExchange exchange, EntityNotFoundException ex) {
        log.warn("Handling generic EntityNotFoundException: {}", ex.getMessage()); // Log it differently maybe
        // Provide a slightly more generic message or use ex.getMessage()
        return createHttpErrorInfo(NOT_FOUND, exchange, new Exception("Requested resource not found."));
    }


    // --- Optional but Recommended: Catch-all for other unexpected exceptions ---
    @ResponseStatus(INTERNAL_SERVER_ERROR) // 500
    @ExceptionHandler(Exception.class) // Catch all other exceptions
    public HttpErrorInfo handleGenericException(ServerWebExchange exchange, Exception ex) {
        // Log unexpected errors with higher severity and stack trace
        log.error("An unexpected error occurred processing request for path: {}", getPath(exchange), ex);
        // Return a generic error message to the client
        return createHttpErrorInfo(INTERNAL_SERVER_ERROR, exchange, new Exception("An unexpected internal error occurred. Please contact support."));
    }


    // Updated helper method to use ServerWebExchange
    private HttpErrorInfo createHttpErrorInfo(HttpStatus httpStatus, ServerWebExchange exchange, Exception ex) {
        final String path = getPath(exchange);
        final String message = ex.getMessage() != null ? ex.getMessage() : "No specific error message provided"; // Handle null messages

        // Use INFO or DEBUG for expected exceptions, ERROR for the generic handler
        if (httpStatus != INTERNAL_SERVER_ERROR) {
            log.info("Returning HTTP status: {} for path: {}. Reason: {}", httpStatus, path, message);
        }
        // Note: The generic handler logs separately with ERROR level including stack trace

        // You might need to adjust HttpErrorInfo constructor based on its definition
        // Assuming it takes status, path, message
        return new HttpErrorInfo(httpStatus, path, message);
    }

    // Helper to extract path safely
    private String getPath(ServerWebExchange exchange) {
        if (exchange != null && exchange.getRequest() != null && exchange.getRequest().getPath() != null) {
            return exchange.getRequest().getPath().pathWithinApplication().value();
        }
        return "Unknown path";
    }
}