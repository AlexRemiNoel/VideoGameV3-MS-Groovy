package com.example.videogamev3.DownloadManagement.utils;

import com.example.videogamev3.DownloadManagement.utils.exceptions.DownloadNotFoundException;
import com.example.videogamev3.DownloadManagement.utils.exceptions.DuplicateDownloadIDException;
import com.example.videogamev3.DownloadManagement.utils.exceptions.InvalidDownloadDataException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.ZonedDateTime;

import static org.springframework.http.HttpStatus.*;

@RestControllerAdvice
@Slf4j
public class GlobalControllerExceptionHandler {
    @ExceptionHandler(DownloadNotFoundException.class)
    @ResponseStatus(NOT_FOUND)
    @ResponseBody
    public HttpErrorInfo handleDownloadNotFoundException(HttpServletRequest request, DownloadNotFoundException ex) { // Use HttpServletRequest
        return createHttpErrorInfo(NOT_FOUND, request, ex);
    }
    @ExceptionHandler(InvalidDownloadDataException.class)
    @ResponseStatus(BAD_REQUEST)
    @ResponseBody
    public HttpErrorInfo handleInvalidInputException(HttpServletRequest request, InvalidDownloadDataException ex) { // Use HttpServletRequest
        return createHttpErrorInfo(BAD_REQUEST, request, ex);
    }
    @ExceptionHandler(DuplicateDownloadIDException.class)
    @ResponseStatus(CONFLICT)
    @ResponseBody
    public HttpErrorInfo handleDuplicateDownloadIDException(HttpServletRequest request, DuplicateDownloadIDException ex) { // Use HttpServletRequest
        return createHttpErrorInfo(CONFLICT, request, ex);
    }
    @ExceptionHandler(Exception.class)
    @ResponseStatus(INTERNAL_SERVER_ERROR)
    @ResponseBody
    public HttpErrorInfo handleGenericException(HttpServletRequest request, Exception ex) { // Use HttpServletRequest
        log.error("An unexpected error occurred processing request for path: {}", getPath(request), ex);
        return createHttpErrorInfo(INTERNAL_SERVER_ERROR, request, new Exception("An unexpected internal error occurred. Please contact support."));
    }
    private HttpErrorInfo createHttpErrorInfo(HttpStatus httpStatus, HttpServletRequest request, Exception ex) {
        final String path = getPath(request);
        final String message = ex.getMessage() != null ? ex.getMessage() : "No specific error message provided";

        if (httpStatus != INTERNAL_SERVER_ERROR) {
            log.warn("Returning HTTP status: {} for path: {}. Reason: {}", httpStatus, path, message);
        }

        return new HttpErrorInfo(ZonedDateTime.now(), path, httpStatus, message); // Pass ZonedDateTime directly
    }
    private String getPath(HttpServletRequest request) {
        if (request != null) {
            return request.getRequestURI();
        }
        return "Unknown path";
    }
}