package com.example.apigatewayservice.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.server.ServerWebExchange;

import static org.springframework.http.HttpStatus.*;

@RestControllerAdvice
@Slf4j
public class GlobalControllerExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<HttpErrorInfo> handleNotFoundException(ServerWebExchange exchange, NotFoundException ex) {
        log.warn("Handling NotFoundException: {}", ex.getMessage());
        return createHttpErrorInfo(HttpStatus.NOT_FOUND, exchange, ex);
    }


    @ExceptionHandler(InvalidInputException.class)
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    public ResponseEntity<HttpErrorInfo> handleInvalidInputException(ServerWebExchange exchange, InvalidInputException ex) {
        log.warn("Handling InvalidInputException: {}", ex.getMessage());
        return createHttpErrorInfo(HttpStatus.UNPROCESSABLE_ENTITY, exchange, ex);
    }

    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(INTERNAL_SERVER_ERROR)
    public ResponseEntity<HttpErrorInfo> handleGenericRuntimeException(ServerWebExchange exchange, RuntimeException ex) {
        log.error("Handling unexpected RuntimeException: {}", ex.getMessage(), ex);
        String safeMessage = "An unexpected internal error occurred.";
        HttpErrorInfo errorInfo = new HttpErrorInfo(INTERNAL_SERVER_ERROR, getPath(exchange), safeMessage);
        return new ResponseEntity<>(errorInfo, INTERNAL_SERVER_ERROR);
    }


    @ExceptionHandler(Exception.class)
    @ResponseStatus(INTERNAL_SERVER_ERROR)
    public ResponseEntity<HttpErrorInfo> handleRootException(ServerWebExchange exchange, Exception ex) {
        log.error("Handling unforeseen Exception: {}", ex.getMessage(), ex);
        String safeMessage = "An unexpected error occurred.";
        HttpErrorInfo errorInfo = new HttpErrorInfo(INTERNAL_SERVER_ERROR, getPath(exchange), safeMessage);
        return new ResponseEntity<>(errorInfo, INTERNAL_SERVER_ERROR);
    }

    // --- Handler for Download Not Found ---
    @ExceptionHandler(DownloadNotFoundException.class)
    public ResponseEntity<HttpErrorInfo> handleDownloadNotFoundException(ServerWebExchange exchange, DownloadNotFoundException ex) { // Use ServerWebExchange and specific exception
        return createHttpErrorInfo(NOT_FOUND, exchange, ex);
    }

    // --- Handler for Invalid Input Data ---
    // Changed to BAD_REQUEST (400) as it's more common for input validation
    @ExceptionHandler(InvalidDownloadDataException.class)
    public ResponseEntity<HttpErrorInfo> handleInvalidDownloadDataException(ServerWebExchange exchange, InvalidDownloadDataException ex) { // Renamed to avoid conflict with the generic InvalidInputException handler
        return createHttpErrorInfo(BAD_REQUEST, exchange, ex);
    }

    // --- Handler for Duplicate ID ---
    // Changed to CONFLICT (409) as it's more semantically correct for duplicates
    @ExceptionHandler(DuplicateDownloadIDException.class)
    // Renamed method for clarity
    public ResponseEntity<HttpErrorInfo> handleDuplicateDownloadIDException(ServerWebExchange exchange, DuplicateDownloadIDException ex) { // Use ServerWebExchange and specific exception
        return createHttpErrorInfo(CONFLICT, exchange, ex);
    }



    private ResponseEntity<HttpErrorInfo> createHttpErrorInfo(HttpStatus status, ServerWebExchange exchange, Exception ex) {
        final String path = getPath(exchange);
        final String message = ex.getMessage() != null ? ex.getMessage() : status.getReasonPhrase();
        final HttpErrorInfo errorInfo = new HttpErrorInfo(status, path, message);
        log.info("Returning HTTP status {} for path '{}' with message: {}", status, path, message);
        return new ResponseEntity<>(errorInfo, status);
    }

    private String getPath(ServerWebExchange exchange) {
        if (exchange != null && exchange.getRequest() != null && exchange.getRequest().getPath() != null) {
            // Use the path within the application context
            return exchange.getRequest().getPath().pathWithinApplication().value();
        }
        return "Unknown path";
    }

//    private String getPath(WebRequest request) { // This method is for Servlet-based Spring MVC, not used in reactive stack.
//        try {
//            String description = request.getDescription(false);
//            if (description != null && description.startsWith("uri=")) {
//                return description.substring(4); // Remove "uri=" prefix
//            }
//            return description != null ? description : "Unknown path";
//        } catch (Exception e) {
//            log.warn("Could not extract path from WebRequest", e);
//            return "Unknown path";
//        }
//    }
}


