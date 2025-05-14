package com.champsoft.Exceptions;

public class DashboardAggregationFailureException extends RuntimeException {
    public DashboardAggregationFailureException(String message) {
        super(message);
    }
    public DashboardAggregationFailureException(String message, Throwable cause) {
        super(message, cause);
    }
}