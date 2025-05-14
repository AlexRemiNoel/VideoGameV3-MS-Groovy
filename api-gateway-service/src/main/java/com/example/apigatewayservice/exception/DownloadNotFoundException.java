package com.example.apigatewayservice.exception;

public class DownloadNotFoundException extends RuntimeException{

    public DownloadNotFoundException() {}

    public DownloadNotFoundException(String message) { super(message); }

    public DownloadNotFoundException(Throwable cause) { super(cause); }

    public DownloadNotFoundException(String message, Throwable cause) { super(message, cause); }
}
