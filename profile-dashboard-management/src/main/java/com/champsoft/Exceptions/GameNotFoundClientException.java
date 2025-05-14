package com.champsoft.Exceptions;

public class GameNotFoundClientException extends RuntimeException {
    public GameNotFoundClientException(String message) {
        super(message);
    }
}
