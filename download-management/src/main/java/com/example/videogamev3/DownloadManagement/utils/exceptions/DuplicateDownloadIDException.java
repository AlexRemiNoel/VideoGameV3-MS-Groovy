package com.example.videogamev3.DownloadManagement.utils.exceptions;

public class DuplicateDownloadIDException extends RuntimeException{

    public DuplicateDownloadIDException() {}

    public DuplicateDownloadIDException(String message) { super(message); }

    public DuplicateDownloadIDException(Throwable cause) { super(cause); }

    public DuplicateDownloadIDException(String message, Throwable cause) { super(message, cause); }
}
