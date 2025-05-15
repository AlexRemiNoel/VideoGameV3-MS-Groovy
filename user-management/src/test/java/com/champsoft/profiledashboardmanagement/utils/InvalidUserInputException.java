package com.champsoft.profiledashboardmanagement.utils;

public class InvalidUserInputException extends RuntimeException{

    public InvalidUserInputException() {}

    public InvalidUserInputException(String message) { super(message); }

    public InvalidUserInputException(Throwable cause) { super(cause); }

    public InvalidUserInputException(String message, Throwable cause) { super(message, cause); }
}
