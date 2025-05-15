package com.example.apigatewayservice.presentationlayer.game;

import com.example.apigatewayservice.businesslogiclayer.game.GameService;
import com.example.apigatewayservice.exception.HttpErrorInfo;
import com.example.apigatewayservice.exception.InvalidInputException;
import com.example.apigatewayservice.exception.NotFoundException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
class GameControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private GameService gameService;

    @Autowired
    private ObjectMapper objectMapper; // For serializing request bodies if needed, or checking error details

    private final String BASE_URI_GAMES = "/api/v1/games";
    private final String VALID_GAME_ID = UUID.randomUUID().toString();
    private final String NOT_FOUND_GAME_ID = UUID.randomUUID().toString();
    private final String VALID_USER_ID = UUID.randomUUID().toString();

    // Custom exception for testing null message in handler
    private static class NullMessageNotFoundException extends NotFoundException {
        public NullMessageNotFoundException() {
            super((String) null); // Pass null message
        }
    }

    private GameResponseModel sampleGameResponse;
    private GameRequestModel sampleGameRequest;
    private ReviewRequestModel sampleReviewRequest;

    @BeforeEach
    void setUp() {
        sampleGameResponse = GameResponseModel.builder()
                .id(VALID_GAME_ID)
                .title("Test Game Title")
                .price(59.99)
                .description("A fantastic game experience.")
                .publisher("Awesome Games Inc.")
                .developer("Creative Studios")
                .genre("Adventure")
                .reviews(Collections.emptyList())
                .build();

        sampleGameRequest = GameRequestModel.builder()
                .title("Test Game Title")
                .price(59.99)
                .description("A fantastic game experience.")
                .publisher("Awesome Games Inc.")
                .developer("Creative Studios")
                .genre("Adventure")
                .UserId(VALID_USER_ID)
                .build();

        sampleReviewRequest = ReviewRequestModel.builder()
                .comment("Absolutely loved it!")
                .rating("5")
                .build();
    }

    // --- GET Game by ID ---
    @Test
    void getGameByGameId_whenGameExists_thenReturnGame() {
        when(gameService.getGameById(VALID_GAME_ID)).thenReturn(sampleGameResponse);

        webTestClient.get().uri(BASE_URI_GAMES + "/" + VALID_GAME_ID)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(GameResponseModel.class)
                .value(response -> {
                    assertNotNull(response);
                    assertEquals(sampleGameResponse.getId(), response.getId());
                    assertEquals(sampleGameResponse.getTitle(), response.getTitle());
                });
        verify(gameService, times(1)).getGameById(VALID_GAME_ID);
    }

    @Test
    void getGameByGameId_whenGameNotFound_thenReturnNotFound() {
        String errorMessage = "Game not found with id: " + NOT_FOUND_GAME_ID;
        when(gameService.getGameById(NOT_FOUND_GAME_ID)).thenThrow(new NotFoundException(errorMessage));

        webTestClient.get().uri(BASE_URI_GAMES + "/" + NOT_FOUND_GAME_ID)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(HttpErrorInfo.class)
                .value(error -> {
                    assertNotNull(error);
                    assertEquals(HttpStatus.NOT_FOUND, error.getHttpStatus());
                    assertTrue(error.getMessage().contains(errorMessage));
                });
        verify(gameService, times(1)).getGameById(NOT_FOUND_GAME_ID);
    }

    @Test
    void getGameByGameId_whenGameNotFoundAndExceptionMessageIsNull_thenReturnHttpStatusReasonPhrase() {
        when(gameService.getGameById(NOT_FOUND_GAME_ID)).thenThrow(new NullMessageNotFoundException());

        webTestClient.get().uri(BASE_URI_GAMES + "/" + NOT_FOUND_GAME_ID)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(HttpErrorInfo.class)
                .value(error -> {
                    assertNotNull(error);
                    assertEquals(HttpStatus.NOT_FOUND, error.getHttpStatus());
                    // Path should be correct
                    assertTrue(error.getPath().contains(BASE_URI_GAMES + "/" + NOT_FOUND_GAME_ID));
                    // Message should be the reason phrase for NOT_FOUND
                    assertEquals(HttpStatus.NOT_FOUND.getReasonPhrase(), error.getMessage());
                });
        verify(gameService, times(1)).getGameById(NOT_FOUND_GAME_ID);
    }

    // --- GET All Games ---
    @Test
    void getAllGames_whenGamesExist_thenReturnListOfGames() {
        List<GameResponseModel> expectedGames = List.of(sampleGameResponse);
        when(gameService.getAllGames()).thenReturn(expectedGames);

        webTestClient.get().uri(BASE_URI_GAMES)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBodyList(GameResponseModel.class)
                .hasSize(1)
                .value(list -> assertEquals(expectedGames.get(0).getId(), list.get(0).getId()));
        verify(gameService, times(1)).getAllGames();
    }

    @Test
    void getAllGames_whenNoGamesExist_thenReturnEmptyList() {
        when(gameService.getAllGames()).thenReturn(Collections.emptyList());

        webTestClient.get().uri(BASE_URI_GAMES)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBodyList(GameResponseModel.class)
                .hasSize(0);
        verify(gameService, times(1)).getAllGames();
    }

    // --- POST (Add Game) ---
    @Test
    void addGame_whenValidRequest_thenReturnCreatedGame() {
        when(gameService.createGame(any(GameRequestModel.class))).thenReturn(sampleGameResponse);

        webTestClient.post().uri(BASE_URI_GAMES)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(sampleGameRequest)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(GameResponseModel.class)
                .value(response -> {
                    assertNotNull(response);
                    assertEquals(sampleGameResponse.getId(), response.getId());
                });
        verify(gameService, times(1)).createGame(any(GameRequestModel.class));
    }

    @Test
    void addGame_whenInvalidRequest_thenReturnUnprocessableEntity() {
        String errorMessage = "Invalid game data provided.";
        when(gameService.createGame(any(GameRequestModel.class))).thenThrow(new InvalidInputException(errorMessage));

        webTestClient.post().uri(BASE_URI_GAMES)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(sampleGameRequest) // Body is still sent, service layer throws exception
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY)
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(HttpErrorInfo.class)
                .value(error -> {
                    assertNotNull(error);
                    assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, error.getHttpStatus());
                    assertTrue(error.getMessage().contains(errorMessage));
                });
        verify(gameService, times(1)).createGame(any(GameRequestModel.class));
    }

    // --- PUT (Update Game) ---
    @Test
    void updateGame_whenGameExistsAndValidRequest_thenReturnUpdatedGame() {
        when(gameService.updateGame(any(GameRequestModel.class))).thenReturn(sampleGameResponse);

        webTestClient.put().uri(BASE_URI_GAMES) // Assuming update is to the base collection URI
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(sampleGameRequest)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(GameResponseModel.class)
                .value(response -> {
                    assertNotNull(response);
                    assertEquals(sampleGameResponse.getId(), response.getId());
                });
        verify(gameService, times(1)).updateGame(any(GameRequestModel.class));
    }

    @Test
    void updateGame_whenGameNotFound_thenReturnNotFound() {
        String errorMessage = "Game to update not found.";
        when(gameService.updateGame(any(GameRequestModel.class))).thenThrow(new NotFoundException(errorMessage));

        webTestClient.put().uri(BASE_URI_GAMES)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(sampleGameRequest)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(HttpErrorInfo.class)
                .value(error -> {
                    assertNotNull(error);
                    assertEquals(HttpStatus.NOT_FOUND, error.getHttpStatus());
                    assertTrue(error.getMessage().contains(errorMessage));
                });
        verify(gameService, times(1)).updateGame(any(GameRequestModel.class));
    }
    
    // --- DELETE Game ---
    @Test
    void deleteGame_whenGameExists_thenReturnNoContent() {
        doNothing().when(gameService).deleteGame(VALID_GAME_ID);

        webTestClient.delete().uri(BASE_URI_GAMES + "/" + VALID_GAME_ID)
                .exchange()
                .expectStatus().isNoContent();
        verify(gameService, times(1)).deleteGame(VALID_GAME_ID);
    }

    @Test
    void deleteGame_whenGameNotFound_thenReturnNotFound() {
        String errorMessage = "Game to delete not found with id: " + NOT_FOUND_GAME_ID;
        doThrow(new NotFoundException(errorMessage)).when(gameService).deleteGame(NOT_FOUND_GAME_ID);

        webTestClient.delete().uri(BASE_URI_GAMES + "/" + NOT_FOUND_GAME_ID)
                .accept(MediaType.APPLICATION_JSON) // Expect error body
                .exchange()
                .expectStatus().isNotFound()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(HttpErrorInfo.class)
                .value(error -> {
                    assertNotNull(error);
                    assertEquals(HttpStatus.NOT_FOUND, error.getHttpStatus());
                    assertTrue(error.getMessage().contains(errorMessage));
                });
        verify(gameService, times(1)).deleteGame(NOT_FOUND_GAME_ID);
    }
    
    // --- POST (Add Review) ---
    @Test
    void addReviewToGame_whenGameExistsAndValidRequest_thenReturnGameWithReview() {
        when(gameService.addReview(any(ReviewRequestModel.class), eq(VALID_GAME_ID))).thenReturn(sampleGameResponse);

        webTestClient.post().uri(BASE_URI_GAMES + "/review/" + VALID_GAME_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(sampleReviewRequest)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk() // GameController returns OK for this
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(GameResponseModel.class)
                .value(response -> {
                    assertNotNull(response);
                    assertEquals(sampleGameResponse.getId(), response.getId());
                });
        verify(gameService, times(1)).addReview(any(ReviewRequestModel.class), eq(VALID_GAME_ID));
    }

    @Test
    void addReviewToGame_whenGameNotFound_thenReturnNotFound() {
        String errorMessage = "Game not found for adding review, id: " + NOT_FOUND_GAME_ID;
        when(gameService.addReview(any(ReviewRequestModel.class), eq(NOT_FOUND_GAME_ID)))
            .thenThrow(new NotFoundException(errorMessage));

        webTestClient.post().uri(BASE_URI_GAMES + "/review/" + NOT_FOUND_GAME_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(sampleReviewRequest)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(HttpErrorInfo.class)
                .value(error -> {
                    assertNotNull(error);
                    assertEquals(HttpStatus.NOT_FOUND, error.getHttpStatus());
                    assertTrue(error.getMessage().contains(errorMessage));
                });
        verify(gameService, times(1)).addReview(any(ReviewRequestModel.class), eq(NOT_FOUND_GAME_ID));
    }
    
    @Test
    void addReviewToGame_whenInvalidReviewData_thenReturnUnprocessableEntity() {
        String errorMessage = "Invalid review data.";
        when(gameService.addReview(any(ReviewRequestModel.class), eq(VALID_GAME_ID)))
            .thenThrow(new InvalidInputException(errorMessage));

        webTestClient.post().uri(BASE_URI_GAMES + "/review/" + VALID_GAME_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(sampleReviewRequest)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY)
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(HttpErrorInfo.class)
                .value(error -> {
                    assertNotNull(error);
                    assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, error.getHttpStatus());
                    assertTrue(error.getMessage().contains(errorMessage));
                });
        verify(gameService, times(1)).addReview(any(ReviewRequestModel.class), eq(VALID_GAME_ID));
    }
} 