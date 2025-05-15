package com.example.videogamev3.DownloadManagement.utils; // Ensure this is the correct package

import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.time.ZonedDateTime;

@Getter
public class HttpErrorInfo {

    private final ZonedDateTime timestamp;
    private final String path;
    private final HttpStatus httpStatus;
    private final String message;

    // Ensure constructor matches:
    public HttpErrorInfo(ZonedDateTime timestamp, String path, HttpStatus httpStatus, String message) {
        this.timestamp = timestamp;
        this.httpStatus = httpStatus;
        this.path = path;
        this.message = message;
    }
}