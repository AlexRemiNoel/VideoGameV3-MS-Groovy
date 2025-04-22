package com.example.videogamev3.DownloadManagement.utils.exceptions;

public class InvalidDownloadDataException extends RuntimeException{

    public InvalidDownloadDataException() {}

    public InvalidDownloadDataException(String message) { super(message); }

    public InvalidDownloadDataException(Throwable cause) { super(cause); }

    public InvalidDownloadDataException(String message, Throwable cause) { super(message, cause); }
}
