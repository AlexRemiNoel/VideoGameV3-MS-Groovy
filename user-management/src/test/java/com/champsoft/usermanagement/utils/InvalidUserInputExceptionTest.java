package com.champsoft.usermanagement.utils;

import com.champsoft.usermanagement.utils.exceptions.InvalidUserInputException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class InvalidUserInputExceptionTest {

    @Test
    void defaultConstructor_shouldCreateException() {
        // Act
        InvalidUserInputException exception = new InvalidUserInputException();

        // Assert
        assertNotNull(exception);
        // Default RuntimeException constructor does not set a message
        assertNull(exception.getMessage(), "Default constructor should not set a message");
        assertNull(exception.getCause(), "Default constructor should not set a cause");
    }

    @Test
    void messageConstructor_shouldCreateExceptionWithMessage() {
        // Arrange
        String testMessage = "Invalid data provided in the request.";

        // Act
        InvalidUserInputException exception = new InvalidUserInputException(testMessage);

        // Assert
        assertNotNull(exception);
        assertEquals(testMessage, exception.getMessage(), "Message constructor should set the message");
        assertNull(exception.getCause(), "Message constructor should not set a cause");
    }

    @Test
    void causeConstructor_shouldCreateExceptionWithCause() {
        // Arrange
        Throwable testCause = new IllegalArgumentException("Input format error.");

        // Act
        InvalidUserInputException exception = new InvalidUserInputException(testCause);

        // Assert
        assertNotNull(exception);
        // When only a cause is provided, the message defaults to the cause's toString()
        assertEquals(testCause.toString(), exception.getMessage(), "Cause constructor should set message from cause");
        assertEquals(testCause, exception.getCause(), "Cause constructor should set the cause");
    }

    @Test
    void messageAndCauseConstructor_shouldCreateExceptionWithMessageAndCause() {
        // Arrange
        String testMessage = "Processing failed due to invalid input.";
        Throwable testCause = new IllegalStateException("Data transformation failed.");

        // Act
        InvalidUserInputException exception = new InvalidUserInputException(testMessage, testCause);

        // Assert
        assertNotNull(exception);
        assertEquals(testMessage, exception.getMessage(), "Message and cause constructor should set the message");
        assertEquals(testCause, exception.getCause(), "Message and cause constructor should set the cause");
    }

    @Test
    void exception_shouldBeRuntimeException() {
        // Act
        InvalidUserInputException exception = new InvalidUserInputException();

        // Assert
        assertInstanceOf(RuntimeException.class, exception, "InvalidUserInputException should be a RuntimeException");
    }

    @Test
    void exception_canBeThrownAndCaught() {
        // Arrange
        String testMessage = "Testing if InvalidUserInputException is throwable.";

        // Act & Assert
        assertThrows(InvalidUserInputException.class, () -> {
            throw new InvalidUserInputException(testMessage);
        }, "InvalidUserInputException should be throwable and catchable");
    }
}