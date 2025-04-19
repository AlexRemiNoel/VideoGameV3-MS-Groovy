package com.champsoft.gamemanagement.Presentation;

import com.champsoft.gamemanagement.BusinessLogic.GameService;
// Removed unused import: import com.champsoft.gamemanagement.DataAccess.GameId;
import com.champsoft.gamemanagement.Presentation.DTOS.GameController; // Assuming this is the controller class
import com.champsoft.gamemanagement.Presentation.DTOS.GameRequestModel;
import com.champsoft.gamemanagement.Presentation.DTOS.GameResponseModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith; // Recommended for JUnit 5 with Mockito
import org.mockito.InjectMocks;
import org.mockito.Mock;
// Removed MockitoAnnotations.openMocks(this); because @ExtendWith(MockitoExtension.class) does this
import org.mockito.junit.jupiter.MockitoExtension; // JUnit 5 extension for Mockito
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
// Removed unused import: import org.springframework.beans.factory.annotation.Autowired;
// Removed unused import: import org.springframework.test.context.bean.override.mockito.MockitoBean;


import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;


// Use MockitoExtension for JUnit 5 integration
@ExtendWith(MockitoExtension.class)
public class GameControllerUnitTest {
    private final String VALID_GAME_ID = "c3540a89-cb47-4c96-888e-ff96708db4d8";
    private final String NOT_FOUND_GAME_ID = "c3540a89-cb47-4c96-888e-ff96708db4d0";
    private final String INVALID_GAME_ID = "c3540a89-cb47-4c96-888e-ff96708db4d"; // Note: Invalid UUID format
    private GameRequestModel gameRequestModel;
    private GameResponseModel gameResponseModel;

    // Use Mockito's @Mock annotation to create a mock instance
    @Mock
    GameService gameService;

    // Use Mockito's @InjectMocks to inject the mocks into the controller
    @InjectMocks
    GameController gameController;

    // Removed MockitoAnnotations.openMocks(this); - @ExtendWith handles this

    @BeforeEach
    void setUp() {
        gameRequestModel = new GameRequestModel();
        gameRequestModel.setTitle("My Game");
        gameRequestModel.setDescription("My first game");

        gameResponseModel = new GameResponseModel();
        gameResponseModel.setId(VALID_GAME_ID);
        gameResponseModel.setTitle("My Game");
        gameResponseModel.setDescription("My first game");
        // Populate other fields of gameResponseModel if necessary for your tests
        // gameResponseModel.setStatus("PENDING");
        // gameResponseModel.setSourceUrl("hello@gmail.com ");
    }

    @Test
    public void whenNoGamesExist_ThenReturnEmptyList() {
        //arrange
        when(gameService.getAllGames()).thenReturn(Collections.emptyList());
        //act
        ResponseEntity<List<GameResponseModel>>
                responseEntity = gameController.getAllGames(); // Changed variable name for consistency
        //assert
        assertNotNull(responseEntity);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        // Use assertEquals for lists for better comparison
        assertEquals(Collections.emptyList(), responseEntity.getBody());
        verify(gameService, times(1)).getAllGames();
    }

    @Test
    void whenValidRequest_thenCreateDownload() { // Renamed test to reflect creating a Game
        // Arrange
        when(gameService.createGame(any(GameRequestModel.class))).thenReturn(gameResponseModel);

        // Act
        ResponseEntity<GameResponseModel> responseEntity = gameController.addGame(gameRequestModel);

        // Assert
        assertNotNull(responseEntity);
        assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());
        // Use assertEquals for comparing objects if equals() is implemented in GameResponseModel
        // Or compare individual fields if equals() is not implemented
        assertEquals(gameResponseModel.getId(), responseEntity.getBody().getId());
        assertEquals(gameResponseModel.getTitle(), responseEntity.getBody().getTitle());
        assertEquals(gameResponseModel.getDescription(), responseEntity.getBody().getDescription());
        // Add assertions for other fields if needed
        // assertEquals(DownloadStatus.PENDING.toString(), responseEntity.getBody().getStatus());

        // Verify that the service method was called with the correct argument
        verify(gameService, times(1)).createGame(eq(gameRequestModel));
    }

    @Test
    void whenGameExists_thenReturnGame() { // Renamed test to reflect getting a Game
        // Arrange
        when(gameService.getGameById(VALID_GAME_ID)).thenReturn(gameResponseModel);

        // Act
        ResponseEntity<GameResponseModel> responseEntity = gameController.getGameByGameId_uuid(VALID_GAME_ID);

        // Assert
        assertNotNull(responseEntity);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());
        // Compare objects or fields
        assertEquals(gameResponseModel.getId(), responseEntity.getBody().getId());
        assertEquals(gameResponseModel.getTitle(), responseEntity.getBody().getTitle());
        // Add assertions for other fields

        verify(gameService, times(1)).getGameById(VALID_GAME_ID);
    }

    @Test
    void whenGameDoesNotExist_thenReturnNotFound() { // Added a test case for not found
        // Arrange
        when(gameService.getGameById(NOT_FOUND_GAME_ID)).thenReturn(null); // Assume service returns null for not found

        // Act
        ResponseEntity<GameResponseModel> responseEntity = gameController.getGameByGameId_uuid(NOT_FOUND_GAME_ID);

        // Assert
        assertNotNull(responseEntity);
        assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
        assertNull(responseEntity.getBody()); // Body should be null or an error object

        verify(gameService, times(1)).getGameById(NOT_FOUND_GAME_ID);
    }


    @Test
    void whenGamesExist_thenReturnGamesList() { // Renamed test
        // Arrange
        GameResponseModel anotherGame = new GameResponseModel();
        anotherGame.setId(UUID.randomUUID().toString()); // Use unique ID
        anotherGame.setTitle("Another Game");
        anotherGame.setDescription("Another description");

        List<GameResponseModel> gameList = Arrays.asList(gameResponseModel, anotherGame);
        when(gameService.getAllGames()).thenReturn(gameList);

        // Act
        ResponseEntity<List<GameResponseModel>> responseEntity = gameController.getAllGames();

        // Assert
        assertNotNull(responseEntity);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());
        assertEquals(2, responseEntity.getBody().size());
        // Use assertEquals for comparing lists if equals() is implemented in GameResponseModel
        // Otherwise, compare list contents based on unique identifiers or key properties
        assertEquals(gameList, responseEntity.getBody());


        verify(gameService, times(1)).getAllGames();
    }

    // Consider adding tests for update and delete methods in your controller
}