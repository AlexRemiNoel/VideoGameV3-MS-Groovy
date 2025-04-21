package com.example.apigatewayservice.exception;


import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus; // Import ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice;
// Remove WebRequest import
// import org.springframework.web.context.request.WebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.server.ServerWebExchange; // Import ServerWebExchange

import java.time.ZonedDateTime;

@RestControllerAdvice
@Slf4j
public class GlobalControllerExceptionHandler {

    // Use ServerWebExchange instead of WebRequest
    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND) // Can use @ResponseStatus as well/instead of ResponseEntity
    public ResponseEntity<HttpErrorInfo> handleNotFoundException(ServerWebExchange exchange, NotFoundException ex) {
        log.warn("Handling NotFoundException: {}", ex.getMessage());
        return createHttpErrorInfo(HttpStatus.NOT_FOUND, exchange, ex);
    }

    // Use ServerWebExchange instead of WebRequest
    @ExceptionHandler(InvalidInputException.class)
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    public ResponseEntity<HttpErrorInfo> handleInvalidInputException(ServerWebExchange exchange, InvalidInputException ex) {
        log.warn("Handling InvalidInputException: {}", ex.getMessage());
        return createHttpErrorInfo(HttpStatus.UNPROCESSABLE_ENTITY, exchange, ex);
    }

    // Use ServerWebExchange instead of WebRequest
    @ExceptionHandler(RuntimeException.class) // Catch broader runtime issues
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<HttpErrorInfo> handleGenericRuntimeException(ServerWebExchange exchange, RuntimeException ex) {
        log.error("Handling unexpected RuntimeException: {}", ex.getMessage(), ex);
        String safeMessage = "An unexpected internal error occurred.";
        HttpErrorInfo errorInfo = new HttpErrorInfo(HttpStatus.INTERNAL_SERVER_ERROR, getPath(exchange), safeMessage);
        return new ResponseEntity<>(errorInfo, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // Use ServerWebExchange instead of WebRequest
    @ExceptionHandler(Exception.class) // Catch top-level Exception
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<HttpErrorInfo> handleRootException(ServerWebExchange exchange, Exception ex) {
        log.error("Handling unforeseen Exception: {}", ex.getMessage(), ex);
        String safeMessage = "An unexpected error occurred.";
        HttpErrorInfo errorInfo = new HttpErrorInfo(HttpStatus.INTERNAL_SERVER_ERROR, getPath(exchange), safeMessage);
        return new ResponseEntity<>(errorInfo, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // Helper to create the error response DTO - takes ServerWebExchange
    private ResponseEntity<HttpErrorInfo> createHttpErrorInfo(HttpStatus status, ServerWebExchange exchange, Exception ex) {
        final String path = getPath(exchange);
        final String message = ex.getMessage() != null ? ex.getMessage() : status.getReasonPhrase();
        final HttpErrorInfo errorInfo = new HttpErrorInfo(status, path, message);
        log.info("Returning HTTP status {} for path '{}' with message: {}", status, path, message);
        return new ResponseEntity<>(errorInfo, status);
    }

    // Helper to get the request path - takes ServerWebExchange
    private String getPath(ServerWebExchange exchange) {
        if (exchange != null && exchange.getRequest() != null && exchange.getRequest().getPath() != null) {
            // Use the path within the application context
            return exchange.getRequest().getPath().pathWithinApplication().value();
        }
        return "Unknown path";
    }

    // Helper to get the request path
    private String getPath(WebRequest request) {
        try {
            // request.getDescription(false) often returns "uri=/path/to/resource"
            String description = request.getDescription(false);
            if (description != null && description.startsWith("uri=")) {
                return description.substring(4); // Remove "uri=" prefix
            }
            return description != null ? description : "Unknown path";
        } catch (Exception e) {
            log.warn("Could not extract path from WebRequest", e);
            return "Unknown path";
        }
    }

    // Ensure HttpErrorInfo class is accessible here and has the necessary constructor/getters
    // Make sure its constructor matches the one used in createHttpErrorInfo
    /* Example matching HttpErrorInfo (ensure yours matches):
    @Getter
    public static class HttpErrorInfo {
        private final ZonedDateTime timestamp;
        private final String path;
        private final HttpStatus httpStatus;
        private final String message;

        public HttpErrorInfo(HttpStatus httpStatus, String path, String message) {
            this.timestamp = ZonedDateTime.now();
            this.httpStatus = httpStatus;
            this.path = path;
            this.message = message;
        }
        // Default constructor might still be needed for Jackson in getErrorMessage if not using @JsonCreator
        public HttpErrorInfo() { this(null, null, null); } // Example default
    }
    */
}