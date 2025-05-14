package com.champsoft.Exceptions;// In ProfileDashboardAggregator: exception/
// (You would already have ResourceNotFoundException and InvalidInputException)

public class UserNotFoundClientException extends RuntimeException {
    public UserNotFoundClientException(String message) {
        super(message);
    }
}


