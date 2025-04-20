package com.champsoft.gamemanagement.utils;

import com.champsoft.gamemanagement.utils.exceptions.DuplicateGameIDException;
import com.champsoft.gamemanagement.utils.exceptions.InvalidInputException;
import com.champsoft.gamemanagement.utils.exceptions.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.context.request.WebRequest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GlobalControllerExceptionHandlerTest {

    @InjectMocks
    private GlobalControllerExceptionHandler exceptionHandler;

    @Mock
    private WebRequest webRequest;

    private static final String TEST_PATH = "/test/path";
    private static final String TEST_MESSAGE = "Test exception message";

    @BeforeEach
    void setUp() {
        // Configure the mocked WebRequest to return a predictable path
        when(webRequest.getDescription(false)).thenReturn(TEST_PATH);
    }

    @Test
    void handleNotFoundException_shouldReturnNotFoundStatus() {
        NotFoundException exception = new NotFoundException(TEST_MESSAGE);

        HttpErrorInfo errorInfo = exceptionHandler.handleNotFoundException(webRequest, exception);

        assertEquals(HttpStatus.NOT_FOUND, errorInfo.getHttpStatus());
        assertEquals(TEST_PATH, errorInfo.getPath());
        assertEquals(TEST_MESSAGE, errorInfo.getMessage());
    }

    @Test
    void handleInvalidInputException_shouldReturnUnprocessableEntityStatus() {
        InvalidInputException exception = new InvalidInputException(TEST_MESSAGE);

        HttpErrorInfo errorInfo = exceptionHandler.handleInvalidInputException(webRequest, exception);

        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, errorInfo.getHttpStatus());
        assertEquals(TEST_PATH, errorInfo.getPath());
        assertEquals(TEST_MESSAGE, errorInfo.getMessage());
    }

    @Test
    void handleDuplicateVinException_shouldReturnUnprocessableEntityStatus() {
        DuplicateGameIDException exception = new DuplicateGameIDException(TEST_MESSAGE);

        HttpErrorInfo errorInfo = exceptionHandler.handleDuplicateVinException(webRequest, exception);

        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, errorInfo.getHttpStatus());
        assertEquals(TEST_PATH, errorInfo.getPath());
        assertEquals(TEST_MESSAGE, errorInfo.getMessage());
    }

    // Note: The private createHttpErrorInfo method is implicitly tested by the public handlers.
    // You could add a dedicated test if it had complex standalone logic, but here it's a simple factory.
}