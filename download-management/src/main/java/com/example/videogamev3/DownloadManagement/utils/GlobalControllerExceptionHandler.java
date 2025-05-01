//package com.example.videogamev3.DownloadManagement.utils;
//
//import com.example.videogamev3.DownloadManagement.utils.exceptions.DuplicateDownloadIDException;
//import com.example.videogamev3.DownloadManagement.utils.exceptions.InvalidDownloadDataException;
//import com.example.videogamev3.DownloadManagement.utils.exceptions.DownloadNotFoundException;
//import jakarta.persistence.EntityNotFoundException;
//import jakarta.servlet.http.HttpServletRequest; // <-- CORRECT IMPORT
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity; // <-- Use ResponseEntity for more control
//import org.springframework.web.bind.annotation.ExceptionHandler;
//import org.springframework.web.bind.annotation.ResponseBody;
//import org.springframework.web.bind.annotation.ResponseStatus;
//import org.springframework.web.bind.annotation.RestControllerAdvice;
//// Remove the WebFlux import: import org.springframework.web.server.ServerWebExchange;
//
//import static org.springframework.http.HttpStatus.*;
//
//@RestControllerAdvice
//@Slf4j
//public class GlobalControllerExceptionHandler {
//
//    // --- Handler for Download Not Found ---
//    @ExceptionHandler(DownloadNotFoundException.class)
//    @ResponseStatus(NOT_FOUND) // Keep this annotation for documentation/clarity if desired
//    @ResponseBody // Ensure the return object is serialized to the response body
//    public HttpErrorInfo handleDownloadNotFoundException(HttpServletRequest request, DownloadNotFoundException ex) { // <-- Use HttpServletRequest
//        return createHttpErrorInfo(NOT_FOUND, request, ex);
//    }
//
//    // --- Handler for Invalid Input Data ---
//    @ExceptionHandler(InvalidDownloadDataException.class)
//    @ResponseStatus(BAD_REQUEST)
//    @ResponseBody
//    public HttpErrorInfo handleInvalidInputException(HttpServletRequest request, InvalidDownloadDataException ex) { // <-- Use HttpServletRequest
//        return createHttpErrorInfo(BAD_REQUEST, request, ex);
//    }
//
//    // --- Handler for Duplicate ID ---
//    @ExceptionHandler(DuplicateDownloadIDException.class)
//    @ResponseStatus(CONFLICT)
//    @ResponseBody
//    public HttpErrorInfo handleDuplicateDownloadIDException(HttpServletRequest request, DuplicateDownloadIDException ex) { // <-- Use HttpServletRequest
//        return createHttpErrorInfo(CONFLICT, request, ex);
//    }
//
//    // --- Optional: Handler for generic JPA EntityNotFoundException ---
//    @ExceptionHandler(EntityNotFoundException.class)
//    @ResponseStatus(NOT_FOUND)
//    @ResponseBody
//    public HttpErrorInfo handleEntityNotFoundException(HttpServletRequest request, EntityNotFoundException ex) { // <-- Use HttpServletRequest
//        log.warn("Handling generic EntityNotFoundException for path {}: {}", getPath(request) ,ex.getMessage());
//        // Wrap the generic JPA exception with a more user-friendly message
//        return createHttpErrorInfo(NOT_FOUND, request, new Exception("Requested resource not found."));
//    }
//
//
//    // --- Optional but Recommended: Catch-all for other unexpected exceptions ---
//    @ExceptionHandler(Exception.class)
//    @ResponseStatus(INTERNAL_SERVER_ERROR)
//    @ResponseBody
//    public HttpErrorInfo handleGenericException(HttpServletRequest request, Exception ex) { // <-- Use HttpServletRequest
//        // Log unexpected errors with higher severity and stack trace
//        log.error("An unexpected error occurred processing request for path: {}", getPath(request), ex);
//        // Return a generic error message to the client
//        return createHttpErrorInfo(INTERNAL_SERVER_ERROR, request, new Exception("An unexpected internal error occurred. Please contact support."));
//    }
//
//
//    // Updated helper method to use HttpServletRequest
//    private HttpErrorInfo createHttpErrorInfo(HttpStatus httpStatus, HttpServletRequest request, Exception ex) {
//        final String path = getPath(request);
//        final String message = ex.getMessage() != null ? ex.getMessage() : "No specific error message provided";
//
//        // Use INFO or WARN for expected exceptions, ERROR for the generic handler
//        if (httpStatus != INTERNAL_SERVER_ERROR) {
//            log.warn("Returning HTTP status: {} for path: {}. Reason: {}", httpStatus, path, message); // Changed to WARN for handled errors
//        }
//        // Note: The generic handler logs separately with ERROR level including stack trace
//
//        // Assuming HttpErrorInfo constructor takes status, path, message
//        return new HttpErrorInfo(httpStatus, path, message);
//    }
//
//    // Helper to extract path safely using HttpServletRequest
//    private String getPath(HttpServletRequest request) {
//        if (request != null) {
//            return request.getRequestURI(); // Use getRequestURI() for the full path
//        }
//        return "Unknown path";
//    }
//}


package com.example.videogamev3.DownloadManagement.utils;

import com.example.videogamev3.DownloadManagement.utils.exceptions.DuplicateDownloadIDException;
import com.example.videogamev3.DownloadManagement.utils.exceptions.InvalidDownloadDataException;
import com.example.videogamev3.DownloadManagement.utils.exceptions.DownloadNotFoundException;
import jakarta.persistence.EntityNotFoundException; // Keep if you might throw this from blocking JPA calls wrapped in reactive types
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
// Remove HttpServletRequest import: import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ServerWebExchange; // <-- Use WebFlux ServerWebExchange

import static org.springframework.http.HttpStatus.*;

@RestControllerAdvice
@Slf4j
public class GlobalControllerExceptionHandler {

    // --- Handler for Download Not Found ---
    @ExceptionHandler(DownloadNotFoundException.class)
    @ResponseStatus(NOT_FOUND) // Keep this annotation for documentation/clarity if desired
    @ResponseBody // Ensure the return object is serialized to the response body
    public HttpErrorInfo handleDownloadNotFoundException(ServerWebExchange exchange, DownloadNotFoundException ex) { // <-- Use ServerWebExchange
        return createHttpErrorInfo(NOT_FOUND, exchange, ex);
    }

    // --- Handler for Invalid Input Data ---
    @ExceptionHandler(InvalidDownloadDataException.class)
    @ResponseStatus(BAD_REQUEST)
    @ResponseBody
    public HttpErrorInfo handleInvalidInputException(ServerWebExchange exchange, InvalidDownloadDataException ex) { // <-- Use ServerWebExchange
        return createHttpErrorInfo(BAD_REQUEST, exchange, ex);
    }

    // --- Handler for Duplicate ID ---
    @ExceptionHandler(DuplicateDownloadIDException.class)
    @ResponseStatus(CONFLICT)
    @ResponseBody
    public HttpErrorInfo handleDuplicateDownloadIDException(ServerWebExchange exchange, DuplicateDownloadIDException ex) { // <-- Use ServerWebExchange
        return createHttpErrorInfo(CONFLICT, exchange, ex);
    }

    // --- Optional: Handler for generic JPA EntityNotFoundException ---
    // This assumes you might be using JPA in blocking calls within your reactive service.
    @ExceptionHandler(EntityNotFoundException.class)
    @ResponseStatus(NOT_FOUND)
    @ResponseBody
    public HttpErrorInfo handleEntityNotFoundException(ServerWebExchange exchange, EntityNotFoundException ex) { // <-- Use ServerWebExchange
        log.warn("Handling generic EntityNotFoundException for path {}: {}", getPath(exchange) ,ex.getMessage());
        // Wrap the generic JPA exception with a more user-friendly message
        return createHttpErrorInfo(NOT_FOUND, exchange, new Exception("Requested resource not found."));
    }


    // --- Optional but Recommended: Catch-all for other unexpected exceptions ---
    @ExceptionHandler(Exception.class)
    @ResponseStatus(INTERNAL_SERVER_ERROR)
    @ResponseBody
    public HttpErrorInfo handleGenericException(ServerWebExchange exchange, Exception ex) { // <-- Use ServerWebExchange
        // Log unexpected errors with higher severity and stack trace
        log.error("An unexpected error occurred processing request for path: {}", getPath(exchange), ex);
        // Return a generic error message to the client
        return createHttpErrorInfo(INTERNAL_SERVER_ERROR, exchange, new Exception("An unexpected internal error occurred. Please contact support."));
    }


    // Updated helper method to use ServerWebExchange
    private HttpErrorInfo createHttpErrorInfo(HttpStatus httpStatus, ServerWebExchange exchange, Exception ex) {
        final String path = getPath(exchange);
        final String message = ex.getMessage() != null ? ex.getMessage() : "No specific error message provided";

        // Use INFO or WARN for expected exceptions, ERROR for the generic handler
        if (httpStatus != INTERNAL_SERVER_ERROR) {
            log.warn("Returning HTTP status: {} for path: {}. Reason: {}", httpStatus, path, message);
        }
        // Note: The generic handler logs separately with ERROR level including stack trace

        // Assuming HttpErrorInfo constructor takes status, path, message
        return new HttpErrorInfo(httpStatus, path, message); // Assuming HttpErrorInfo constructor exists
    }

    // Helper to extract path safely using ServerWebExchange
    private String getPath(ServerWebExchange exchange) {
        if (exchange != null && exchange.getRequest() != null && exchange.getRequest().getPath() != null) {
            // Use the path within the application context
            return exchange.getRequest().getPath().pathWithinApplication().value();
        }
        return "Unknown path";
    }

    // --- HttpErrorInfo Class (Make sure this exists) ---
    // You need a class like this defined somewhere accessible.
    // (Adjust fields/constructor as needed for your actual implementation)
    /*
    public static class HttpErrorInfo {
        private final java.time.ZonedDateTime timestamp;
        private final int status;
        private final String error;
        private final String path;
        private final String message;

        public HttpErrorInfo(HttpStatus httpStatus, String path, String message) {
            this.timestamp = java.time.ZonedDateTime.now();
            this.status = httpStatus.value();
            this.error = httpStatus.getReasonPhrase();
            this.path = path;
            this.message = message;
        }

        // Add Getters if needed for serialization (Lombok's @Data or @Value would also work)
        public java.time.ZonedDateTime getTimestamp() { return timestamp; }
        public int getStatus() { return status; }
        public String getError() { return error; }
        public String getPath() { return path; }
        public String getMessage() { return message; }
    }
    */

}