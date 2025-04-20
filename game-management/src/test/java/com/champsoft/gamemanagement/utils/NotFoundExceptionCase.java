package com.champsoft.gamemanagement.utils;

import com.champsoft.gamemanagement.utils.exceptions.NotFoundException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NotFoundExceptionTest {

    @Test
    void defaultConstructor_shouldCreateException() {
        // Act
        NotFoundException exception = new NotFoundException();

        // Assert
        assertNotNull(exception);
        // Default RuntimeException constructor does not set a message
        assertNull(exception.getMessage(), "Default constructor should not set a message");
        assertNull(exception.getCause(), "Default constructor should not set a cause");
    }

    @Test
    void messageConstructor_shouldCreateExceptionWithMessage() {
        // Arrange
        String testMessage = "This resource was not found.";

        // Act
        NotFoundException exception = new NotFoundException(testMessage);

        // Assert
        assertNotNull(exception);
        assertEquals(testMessage, exception.getMessage(), "Message constructor should set the message");
        assertNull(exception.getCause(), "Message constructor should not set a cause");
    }

    @Test
    void causeConstructor_shouldCreateExceptionWithCause() {
        // Arrange
        Throwable testCause = new RuntimeException("Underlying issue occurred.");

        // Act
        NotFoundException exception = new NotFoundException(testCause);

        // Assert
        assertNotNull(exception);
        // When only a cause is provided, the message defaults to the cause's toString()
        assertEquals(testCause.toString(), exception.getMessage(), "Cause constructor should set message from cause");
        assertEquals(testCause, exception.getCause(), "Cause constructor should set the cause");
    }

    @Test
    void messageAndCauseConstructor_shouldCreateExceptionWithMessageAndCause() {
        // Arrange
        String testMessage = "Resource not found due to a specific problem.";
        Throwable testCause = new IllegalArgumentException("Invalid identifier.");

        // Act
        NotFoundException exception = new NotFoundException(testMessage, testCause);

        // Assert
        assertNotNull(exception);
        assertEquals(testMessage, exception.getMessage(), "Message and cause constructor should set the message");
        assertEquals(testCause, exception.getCause(), "Message and cause constructor should set the cause");
    }

    @Test
    void exception_shouldBeRuntimeException() {
        // Act
        NotFoundException exception = new NotFoundException();

        // Assert
        assertTrue(exception instanceof RuntimeException, "NotFoundException should be a RuntimeException");
    }

    @Test
    void exception_canBeThrownAndCaught() {
        // Arrange
        String testMessage = "Testing throwing the exception.";

        // Act & Assert
        assertThrows(NotFoundException.class, () -> {
            throw new NotFoundException(testMessage);
        }, "NotFoundException should be throwable and catchable");
    }
}