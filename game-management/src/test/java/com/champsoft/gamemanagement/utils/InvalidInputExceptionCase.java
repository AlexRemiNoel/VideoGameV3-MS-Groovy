package com.champsoft.gamemanagement.utils;

import com.champsoft.gamemanagement.utils.exceptions.InvalidInputException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class InvalidInputExceptionTest {

    @Test
    void defaultConstructor_shouldCreateException() {
        // Act
        InvalidInputException exception = new InvalidInputException();

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
        InvalidInputException exception = new InvalidInputException(testMessage);

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
        InvalidInputException exception = new InvalidInputException(testCause);

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
        InvalidInputException exception = new InvalidInputException(testMessage, testCause);

        // Assert
        assertNotNull(exception);
        assertEquals(testMessage, exception.getMessage(), "Message and cause constructor should set the message");
        assertEquals(testCause, exception.getCause(), "Message and cause constructor should set the cause");
    }

    @Test
    void exception_shouldBeRuntimeException() {
        // Act
        InvalidInputException exception = new InvalidInputException();

        // Assert
        assertTrue(exception instanceof RuntimeException, "InvalidInputException should be a RuntimeException");
    }

    @Test
    void exception_canBeThrownAndCaught() {
        // Arrange
        String testMessage = "Testing if InvalidInputException is throwable.";

        // Act & Assert
        assertThrows(InvalidInputException.class, () -> {
            throw new InvalidInputException(testMessage);
        }, "InvalidInputException should be throwable and catchable");
    }
}