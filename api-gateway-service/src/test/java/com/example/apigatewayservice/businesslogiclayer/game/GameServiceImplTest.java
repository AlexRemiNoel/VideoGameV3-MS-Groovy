package com.example.apigatewayservice.businesslogiclayer.game;

import com.example.apigatewayservice.DomainClientLayer.game.GameServiceClient;
import com.example.apigatewayservice.presentationlayer.game.GameRequestModel;
import com.example.apigatewayservice.presentationlayer.game.GameResponseModel;
import com.example.apigatewayservice.presentationlayer.game.ReviewRequestModel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GameServiceImplTest {

    @Mock
    private GameServiceClient gameServiceClient;

    @InjectMocks
    private GameServiceImpl gameService;

    private final String GAME_ID = "game-uuid-123";
    private final String USER_ID = "user-uuid-456";

    private GameResponseModel buildSampleGameResponseModel() {
        return GameResponseModel.builder()
                .id(GAME_ID)
                .title("Test Game")
                .build();
    }

    private GameRequestModel buildSampleGameRequestModel() {
        return GameRequestModel.builder()
                .title("Test Game")
                .UserId(USER_ID)
                .build();
    }

    private ReviewRequestModel buildSampleReviewRequestModel() {
        return ReviewRequestModel.builder()
                .comment("Good game")
                .rating("5")
                .build();
    }

    @Test
    void getAllGames_callsClient() {
        List<GameResponseModel> expectedResponse = List.of(buildSampleGameResponseModel());
        when(gameServiceClient.getAllGames()).thenReturn(expectedResponse);

        List<GameResponseModel> actualResponse = gameService.getAllGames();

        assertEquals(expectedResponse, actualResponse);
        verify(gameServiceClient, times(1)).getAllGames();
    }

    @Test
    void getGameById_callsClient() {
        GameResponseModel expectedResponse = buildSampleGameResponseModel();
        when(gameServiceClient.getGameByGameId(GAME_ID)).thenReturn(expectedResponse);

        GameResponseModel actualResponse = gameService.getGameById(GAME_ID);

        assertEquals(expectedResponse, actualResponse);
        verify(gameServiceClient, times(1)).getGameByGameId(GAME_ID);
    }

    @Test
    void createGame_callsClient() {
        GameRequestModel requestModel = buildSampleGameRequestModel();
        GameResponseModel expectedResponse = buildSampleGameResponseModel();
        when(gameServiceClient.createGame(requestModel)).thenReturn(expectedResponse);

        GameResponseModel actualResponse = gameService.createGame(requestModel);

        assertEquals(expectedResponse, actualResponse);
        verify(gameServiceClient, times(1)).createGame(requestModel);
    }

    @Test
    void updateGame_callsClient() {
        GameRequestModel requestModel = buildSampleGameRequestModel();
        GameResponseModel expectedResponse = buildSampleGameResponseModel();
        when(gameServiceClient.updateGame(requestModel)).thenReturn(expectedResponse);

        GameResponseModel actualResponse = gameService.updateGame(requestModel);

        assertEquals(expectedResponse, actualResponse);
        verify(gameServiceClient, times(1)).updateGame(requestModel);
    }

    @Test
    void deleteGame_callsClient() {
        doNothing().when(gameServiceClient).deleteGame(GAME_ID);

        gameService.deleteGame(GAME_ID);

        verify(gameServiceClient, times(1)).deleteGame(GAME_ID);
    }

    @Test
    void addReview_callsClient() {
        ReviewRequestModel reviewRequestModel = buildSampleReviewRequestModel();
        GameResponseModel expectedResponse = buildSampleGameResponseModel(); // Assuming review addition returns the game
        when(gameServiceClient.addReview(reviewRequestModel, GAME_ID)).thenReturn(expectedResponse);

        GameResponseModel actualResponse = gameService.addReview(reviewRequestModel, GAME_ID);

        assertEquals(expectedResponse, actualResponse);
        verify(gameServiceClient, times(1)).addReview(reviewRequestModel, GAME_ID);
    }
} 