package com.champsoft.profiledashboardmanagement.utils;

import com.champsoft.profiledashboardmanagement.utils.exceptions.DuplicateVinException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DuplicateVinExceptionTest {

    @Test
    void testDefaultConstructor() {
        DuplicateVinException exception = new DuplicateVinException();
        assertNull(exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    void testConstructorWithMessage() {
        String message = "Duplicate VIN found";
        DuplicateVinException exception = new DuplicateVinException(message);
        assertEquals(message, exception.getMessage());
    }

    @Test
    void testConstructorWithCause() {
        Throwable cause = new RuntimeException("Underlying cause");
        DuplicateVinException exception = new DuplicateVinException(cause);
        assertEquals(cause, exception.getCause());
    }

    @Test
    void testConstructorWithMessageAndCause() {
        String message = "Duplicate VIN found";
        Throwable cause = new RuntimeException("Underlying cause");
        DuplicateVinException exception = new DuplicateVinException(message, cause);
        assertEquals(message, exception.getMessage());
        assertEquals(cause, exception.getCause());
    }
}
