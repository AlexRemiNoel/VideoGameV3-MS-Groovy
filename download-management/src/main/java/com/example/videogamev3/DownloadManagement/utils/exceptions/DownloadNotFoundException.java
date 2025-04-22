package com.example.videogamev3.DownloadManagement.utils.exceptions;

public class DownloadNotFoundException extends RuntimeException{

    public DownloadNotFoundException() {}

    public DownloadNotFoundException(String message) { super(message); }

    public DownloadNotFoundException(Throwable cause) { super(cause); }

    public DownloadNotFoundException(String message, Throwable cause) { super(message, cause); }
}
