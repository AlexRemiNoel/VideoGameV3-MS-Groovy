package com.champsoft.Exceptions;

// General exception for when a downstream service is unavailable or returns an unexpected error
public class DownstreamServiceUnavailableException extends RuntimeException {
    public DownstreamServiceUnavailableException(String message) {
        super(message);
    }

    public DownstreamServiceUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}