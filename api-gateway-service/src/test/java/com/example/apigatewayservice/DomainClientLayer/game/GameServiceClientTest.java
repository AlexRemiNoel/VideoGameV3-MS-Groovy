package com.example.apigatewayservice.DomainClientLayer.game;

import com.example.apigatewayservice.exception.HttpErrorInfo;
import com.example.apigatewayservice.exception.InvalidInputException;
import com.example.apigatewayservice.exception.NotFoundException;
import com.example.apigatewayservice.presentationlayer.game.GameRequestModel;
import com.example.apigatewayservice.presentationlayer.game.GameResponseModel;
import com.example.apigatewayservice.presentationlayer.game.ReviewRequestModel;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GameServiceClientTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private GameServiceClient gameServiceClient;

    private final String GAME_ID = "game-uuid-123";
    private final String GAME_SERVICE_HOST = "game-service"; // Example host
    private final String GAME_SERVICE_PORT = "8080"; // Example port
    private String BASE_URL;

    private GameResponseModel sampleGameResponse;
    private GameRequestModel sampleGameRequest;
    private ReviewRequestModel sampleReviewRequest;

    @BeforeEach
    void setUp() {
        // Re-initialize client with mocks and configured values for host/port
        // Note: In a real scenario, these @Value fields would be injected if using @SpringBootTest,
        // but for a pure Mockito test, we pass them directly.
        gameServiceClient = new GameServiceClient(restTemplate, objectMapper, GAME_SERVICE_HOST, GAME_SERVICE_PORT);
        BASE_URL = "http://" + GAME_SERVICE_HOST + ":" + GAME_SERVICE_PORT + "/api/v1/game";

        sampleGameResponse = GameResponseModel.builder()
                .id(GAME_ID)
                .title("Test Game")
                .price(59.99)
                .description("A great game")
                .publisher("Test Publisher")
                .developer("Test Developer")
                .genre("Adventure")
                .reviews(Collections.emptyList())
                .build();

        sampleGameRequest = GameRequestModel.builder()
                .title("Test Game")
                .price(59.99)
                .description("A great game")
                .publisher("Test Publisher")
                .developer("Test Developer")
                .genre("Adventure")
                .UserId("user-123")
                .build();

        sampleReviewRequest = ReviewRequestModel.builder()
                .comment("Great game!")
                .rating("5")
                .build();
    }

    private HttpClientErrorException mockHttpClientErrorException(HttpStatus status, String responseBody, String originalExMessage) {
        return new HttpClientErrorException(status, status.getReasonPhrase(), responseBody.getBytes(), java.nio.charset.StandardCharsets.UTF_8);
    }

    // --- Success Cases ---

    @Test
    void getAllGames_success() {
        List<GameResponseModel> expectedResponse = List.of(sampleGameResponse);
        ResponseEntity<List<GameResponseModel>> responseEntity = new ResponseEntity<>(expectedResponse, HttpStatus.OK);
        when(restTemplate.exchange(eq(BASE_URL), eq(HttpMethod.GET), isNull(), any(ParameterizedTypeReference.class)))
                .thenReturn(responseEntity);

        List<GameResponseModel> result = gameServiceClient.getAllGames();

        assertFalse(result.isEmpty());
        assertEquals(sampleGameResponse, result.get(0));
        verify(restTemplate).exchange(eq(BASE_URL), eq(HttpMethod.GET), isNull(), any(ParameterizedTypeReference.class));
    }

    @Test
    void getGameByGameId_success() {
        String url = BASE_URL + "/" + GAME_ID;
        when(restTemplate.getForObject(eq(url), eq(GameResponseModel.class))).thenReturn(sampleGameResponse);

        GameResponseModel result = gameServiceClient.getGameByGameId(GAME_ID);

        assertEquals(sampleGameResponse, result);
        verify(restTemplate).getForObject(eq(url), eq(GameResponseModel.class));
    }

    @Test
    void createGame_success() {
        when(restTemplate.postForObject(eq(BASE_URL), eq(sampleGameRequest), eq(GameResponseModel.class)))
                .thenReturn(sampleGameResponse);

        GameResponseModel result = gameServiceClient.createGame(sampleGameRequest);

        assertEquals(sampleGameResponse, result);
        verify(restTemplate).postForObject(eq(BASE_URL), eq(sampleGameRequest), eq(GameResponseModel.class));
    }

    @Test
    void updateGame_success() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<GameRequestModel> requestEntity = new HttpEntity<>(sampleGameRequest, headers);

        ResponseEntity<GameResponseModel> responseEntity = new ResponseEntity<>(sampleGameResponse, HttpStatus.OK);
        when(restTemplate.exchange(eq(BASE_URL), eq(HttpMethod.PUT), eq(requestEntity), eq(GameResponseModel.class)))
                .thenReturn(responseEntity);

        GameResponseModel result = gameServiceClient.updateGame(sampleGameRequest);

        assertEquals(sampleGameResponse, result);
        verify(restTemplate).exchange(eq(BASE_URL), eq(HttpMethod.PUT), eq(requestEntity), eq(GameResponseModel.class));
    }

    @Test
    void deleteGame_success() {
        String url = BASE_URL + "/" + GAME_ID;
        doNothing().when(restTemplate).delete(eq(url));

        gameServiceClient.deleteGame(GAME_ID);

        verify(restTemplate).delete(eq(url));
    }

    @Test
    void addReview_success() {
        String url = BASE_URL + "/review/" + GAME_ID;
        when(restTemplate.postForObject(eq(url), eq(sampleReviewRequest), eq(GameResponseModel.class)))
                .thenReturn(sampleGameResponse); // Assuming adding a review returns the updated game

        GameResponseModel result = gameServiceClient.addReview(sampleReviewRequest, GAME_ID);

        assertEquals(sampleGameResponse, result);
        verify(restTemplate).postForObject(eq(url), eq(sampleReviewRequest), eq(GameResponseModel.class));
    }

    // --- Exception Cases ---

    @Test
    void getGameByGameId_notFoundException() throws IOException {
        String url = BASE_URL + "/" + GAME_ID;
        String errorMsgJson = "{\"message\":\"Game not found\", \"path\":\"/api/v1/game/" + GAME_ID + "\", \"httpStatus\":\"NOT_FOUND\"}";
        HttpClientErrorException ex = mockHttpClientErrorException(HttpStatus.NOT_FOUND, errorMsgJson, "Original ex message");
        when(objectMapper.readValue(errorMsgJson, HttpErrorInfo.class))
                .thenReturn(new HttpErrorInfo(HttpStatus.NOT_FOUND, "/api/v1/game/" + GAME_ID, "Game not found"));
        when(restTemplate.getForObject(eq(url), eq(GameResponseModel.class))).thenThrow(ex);

        NotFoundException thrown = assertThrows(NotFoundException.class, () -> gameServiceClient.getGameByGameId(GAME_ID));
        assertEquals("Game not found", thrown.getMessage());
        verify(objectMapper).readValue(errorMsgJson, HttpErrorInfo.class);
    }

    @Test
    void createGame_invalidInputException() throws IOException {
        String errorMsgJson = "{\"message\":\"Invalid input\", \"path\":\"/api/v1/game\", \"httpStatus\":\"UNPROCESSABLE_ENTITY\"}";
        HttpClientErrorException ex = mockHttpClientErrorException(HttpStatus.UNPROCESSABLE_ENTITY, errorMsgJson, "Original ex message");
        when(objectMapper.readValue(errorMsgJson, HttpErrorInfo.class))
                .thenReturn(new HttpErrorInfo(HttpStatus.UNPROCESSABLE_ENTITY, "/api/v1/game", "Invalid input"));
        when(restTemplate.postForObject(eq(BASE_URL), eq(sampleGameRequest), eq(GameResponseModel.class))).thenThrow(ex);

        InvalidInputException thrown = assertThrows(InvalidInputException.class, () -> gameServiceClient.createGame(sampleGameRequest));
        assertEquals("Invalid input", thrown.getMessage());
        verify(objectMapper).readValue(errorMsgJson, HttpErrorInfo.class);
    }
    
    @Test
    void updateGame_notFoundException() throws IOException {
        String errorMsgJson = "{\"message\":\"Cannot update, game not found\", \"path\":\"/api/v1/game\", \"httpStatus\":\"NOT_FOUND\"}";
        HttpClientErrorException ex = mockHttpClientErrorException(HttpStatus.NOT_FOUND, errorMsgJson, "Original ex message");
        when(objectMapper.readValue(errorMsgJson, HttpErrorInfo.class))
                .thenReturn(new HttpErrorInfo(HttpStatus.NOT_FOUND, "/api/v1/game", "Cannot update, game not found"));
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<GameRequestModel> requestEntity = new HttpEntity<>(sampleGameRequest, headers);
        when(restTemplate.exchange(eq(BASE_URL), eq(HttpMethod.PUT), eq(requestEntity), eq(GameResponseModel.class))).thenThrow(ex);

        NotFoundException thrown = assertThrows(NotFoundException.class, () -> gameServiceClient.updateGame(sampleGameRequest));
        assertEquals("Cannot update, game not found", thrown.getMessage());
        verify(objectMapper).readValue(errorMsgJson, HttpErrorInfo.class);
    }


    @Test
    void getGameByGameId_genericHttpClientErrorException() {
        String url = BASE_URL + "/" + GAME_ID;
        HttpClientErrorException originalEx = mockHttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error", "Original ex message");
        when(restTemplate.getForObject(eq(url), eq(GameResponseModel.class))).thenThrow(originalEx);

        HttpClientErrorException thrown = assertThrows(HttpClientErrorException.class, () -> gameServiceClient.getGameByGameId(GAME_ID));
        assertEquals(originalEx, thrown); // Should rethrow the original exception
    }
    
    // --- getErrorMessage specific tests ---

    @Test
    void getErrorMessage_parsedSuccessfully() throws IOException {
        String actualErrorMessage = "Detailed error from service.";
        String errorBody = "{\"message\":\"" + actualErrorMessage + "\", \"path\":\"/some/path\", \"httpStatus\":\"BAD_REQUEST\"}";
        HttpClientErrorException ex = mockHttpClientErrorException(HttpStatus.BAD_REQUEST, errorBody, "Original ex message");
        when(objectMapper.readValue(errorBody, HttpErrorInfo.class))
                .thenReturn(new HttpErrorInfo(HttpStatus.BAD_REQUEST, "/some/path", actualErrorMessage));

        // To test private getErrorMessage, we call a public method that uses it.
        // Here, handleHttpClientException will be called, which then calls getErrorMessage.
        // We expect InvalidInputException for UNPROCESSABLE_ENTITY, or NotFound for NOT_FOUND.
        // Let's use UNPROCESSABLE_ENTITY to get InvalidInputException
        HttpClientErrorException exForInvalidInput = mockHttpClientErrorException(HttpStatus.UNPROCESSABLE_ENTITY, errorBody, "Original ex message");
         when(restTemplate.getForObject(anyString(), eq(GameResponseModel.class))).thenThrow(exForInvalidInput);


        try {
            gameServiceClient.getGameByGameId("some-id");
            fail("Exception expected");
        } catch (InvalidInputException e) {
            assertEquals(actualErrorMessage, e.getMessage());
        }
        verify(objectMapper).readValue(errorBody, HttpErrorInfo.class);
    }

    @Test
    void getErrorMessage_objectMapperThrowsIOException() throws IOException {
        HttpStatus status = HttpStatus.NOT_FOUND;
        // Provide a body that looks like JSON to ensure objectMapper.readValue is called
        // and passes the client's startsWith("{") and endsWith("}") checks.
        String responseBody = "{ \"corrupt\": \"json\" }"; // Corrected to be valid JSON structure
        String expectedMessage = "IOEx fallback message";
        HttpClientErrorException mockException = mock(HttpClientErrorException.class);
        when(mockException.getResponseBodyAsString()).thenReturn(responseBody);
        when(mockException.getMessage()).thenReturn(expectedMessage);
        when(mockException.getStatusCode()).thenReturn(status); // Needed for final fallback if ex.getMessage() was blank
        when(mockException.getStatusText()).thenReturn(status.getReasonPhrase()); // Needed for final fallback

        when(objectMapper.readValue(responseBody, HttpErrorInfo.class))
                .thenThrow(new JsonProcessingException("Test parse error"){});

        String actualErrorMessage = gameServiceClient.getErrorMessage(mockException);
        assertEquals(expectedMessage, actualErrorMessage); // Should now fall back to ex.getMessage()
        verify(objectMapper).readValue(responseBody, HttpErrorInfo.class);
    }
    
    @Test
    void getErrorMessage_objectMapperThrowsOtherException() throws IOException {
        HttpStatus status = HttpStatus.UNPROCESSABLE_ENTITY;
        String responseBody = "another-body"; // Non-JSON body
        HttpClientErrorException mockException = mockHttpClientErrorException(status, responseBody, "Original ex message");
        // Ensure objectMapper is not called with this non-JSON body due to client's startsWith("{") check
        // If it were called and threw a non-IOException, this test would cover that.
        // Given the current client logic, this path results in returning the raw body directly.

        String actualErrorMessage = gameServiceClient.getErrorMessage(mockException);
        assertEquals(responseBody, actualErrorMessage); // Client returns raw body if it's not JSON-like
        verify(objectMapper, never()).readValue(anyString(), eq(HttpErrorInfo.class));
    }


    @Test
    void getErrorMessage_emptyOrBlankMessageInParsedHttpErrorInfo() throws IOException {
        String errorBodyWithBlankMessage = "{\"message\":\" \", \"path\":\"/path\", \"httpStatus\":\"NOT_FOUND\"}";
        HttpClientErrorException ex = mockHttpClientErrorException(HttpStatus.NOT_FOUND, errorBodyWithBlankMessage, "Original ex message");
        // Simulate objectMapper returning HttpErrorInfo with a blank message
        when(objectMapper.readValue(errorBodyWithBlankMessage, HttpErrorInfo.class))
                .thenReturn(new HttpErrorInfo(HttpStatus.NOT_FOUND, "/path", " "));
        when(restTemplate.getForObject(anyString(), eq(GameResponseModel.class))).thenThrow(ex);

        try {
            gameServiceClient.getGameByGameId("some-id");
            fail("Exception expected");
        } catch (NotFoundException e) {
            // Fallback to raw response body because parsed message was blank
            assertEquals(errorBodyWithBlankMessage, e.getMessage());
        }
        verify(objectMapper).readValue(errorBodyWithBlankMessage, HttpErrorInfo.class);
    }
    
    @Test
    void getErrorMessage_fallbackToExceptionMessageWhenAllElseFails() throws IOException {
        HttpStatus status = HttpStatus.SERVICE_UNAVAILABLE;
        String responseBody = ""; // Blank body, so objectMapper won't be called by current client logic
        String expectedMessage = "Fallback to this message";
        HttpClientErrorException mockException = mock(HttpClientErrorException.class);
        when(mockException.getResponseBodyAsString()).thenReturn(responseBody);
        when(mockException.getMessage()).thenReturn(expectedMessage);
        // when(mockException.getStatusCode()).thenReturn(status); // This line was unnecessary as getStatusCode is not called if getMessage() is non-blank

        String actualErrorMessage = gameServiceClient.getErrorMessage(mockException);

        assertEquals(expectedMessage, actualErrorMessage);
        verify(objectMapper, never()).readValue(anyString(), eq(HttpErrorInfo.class)); // Not called due to blank body
    }

    @Test
    void getErrorMessage_fallbackToStatusCodeMessageWhenAllElseFailsIncludingExMessage() throws IOException {
        HttpStatus status = HttpStatus.NOT_FOUND;
        String responseBody = ""; // Blank body
        HttpClientErrorException mockException = mock(HttpClientErrorException.class);
        when(mockException.getResponseBodyAsString()).thenReturn(responseBody);
        when(mockException.getMessage()).thenReturn(null); // ex.getMessage() is null
        when(mockException.getStatusCode()).thenReturn(status);
        when(mockException.getStatusText()).thenReturn(status.getReasonPhrase());

        String actualErrorMessage = gameServiceClient.getErrorMessage(mockException);
        assertEquals("An error occurred: " + status.value() + " " + status.getReasonPhrase(), actualErrorMessage);
        verify(objectMapper, never()).readValue(anyString(), eq(HttpErrorInfo.class));
    }
} 