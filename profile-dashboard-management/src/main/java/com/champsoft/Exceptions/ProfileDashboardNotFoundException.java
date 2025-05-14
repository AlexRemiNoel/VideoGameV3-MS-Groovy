package com.champsoft.Exceptions;

public class ProfileDashboardNotFoundException extends RuntimeException { // This is like ResourceNotFound
    public ProfileDashboardNotFoundException(String message) {
        super(message);
    }
}