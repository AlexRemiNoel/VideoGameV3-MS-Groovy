package com.champsoft.gamemanagement.utils.exceptions;

public class DuplicateGameIDException extends RuntimeException{

    public DuplicateGameIDException() {}

    public DuplicateGameIDException(String message) { super(message); }

    public DuplicateGameIDException(Throwable cause) { super(cause); }

    public DuplicateGameIDException(String message, Throwable cause) { super(message, cause); }
}
