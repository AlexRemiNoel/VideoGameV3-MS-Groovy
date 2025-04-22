package com.champsoft.gamemanagement.BuisinessLogic;

import com.champsoft.gamemanagement.BusinessLogic.GameService;
import com.champsoft.gamemanagement.DataAccess.*;
import com.champsoft.gamemanagement.DataMapper.GameRequestMapper;
import com.champsoft.gamemanagement.DataMapper.GameResponseMapper;
import com.champsoft.gamemanagement.DataMapper.ReviewMapper;
import com.champsoft.gamemanagement.Presentation.DTOS.GameRequestModel;
import com.champsoft.gamemanagement.Presentation.DTOS.GameResponseModel;
import com.champsoft.gamemanagement.Presentation.DTOS.ReviewRequestModel;
import com.champsoft.gamemanagement.utils.exceptions.NotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class GameServiceTest {

    @Mock
    private GameResponseMapper gameResponseMapper;

    @Mock
    private GameRepository gameRepository;

    @Mock
    private GameRequestMapper gameRequestMapper;

    @Mock
    private ReviewMapper reviewMapper;

    // FIX: Add the mock for ReviewRepository
    @Mock
    private ReviewRepository reviewRepository;

    @InjectMocks
    private GameService gameService;

    private Game createTestGame() {
        GameId gameId = new GameId(UUID.randomUUID().toString());
        Game game = new Game();
        game.setGameId(gameId);
        game.setTitle("Test Game");
        game.setPrice(29.99);
        game.setReleaseDate(LocalDateTime.now());
        game.setDescription("A test game description.");
        game.setPublisher("Test Publisher");
        game.setDeveloper("Test Developer");
        game.setGenre(Genre.ACTION);
        game.setGame_user_id("testUser");
        game.setReviews(new ArrayList<>()); // Initialize as a mutable ArrayList
        return game;
    }

    private GameResponseModel createTestGameResponseModel() {
        GameResponseModel responseModel = new GameResponseModel();
        responseModel.setId(UUID.randomUUID().toString());
        responseModel.setTitle("Test Game");
        responseModel.setPrice(29.99);
        return responseModel;
    }

    private GameRequestModel createTestGameRequestModel() {
        GameRequestModel requestModel = new GameRequestModel();
        requestModel.setTitle("Test Game");
        requestModel.setPrice(29.99);
        return requestModel;
    }

    private Review createTestReview() {
        Review review = new Review();
        review.setReviewId(new ReviewId(UUID.randomUUID().toString()));
        review.setComment("Great game!");
        // Set other fields like rating, timestamp if needed by your service
        return review;
    }

    private ReviewRequestModel createTestReviewRequestModel() {
        ReviewRequestModel requestModel = new ReviewRequestModel();
        requestModel.setComment("Amazing!");
        // Set other fields like rating if needed
        return requestModel;
    }

    @Test
    public void whenGetGameById_existingUuid_thenReturnGameResponseModel() {
        // Arrange
        String uuid = UUID.randomUUID().toString();
        Game game = createTestGame();
        GameResponseModel responseModel = createTestGameResponseModel();
        when(gameRepository.findGameByGameId(new GameId(uuid))).thenReturn(game);
        when(gameResponseMapper.gameToGameResponseModel(game)).thenReturn(responseModel);

        // Act
        GameResponseModel result = gameService.getGameById(uuid);

        // Assert
        assertNotNull(result);
        assertEquals(responseModel.getId(), result.getId());
        verify(gameRepository, times(1)).findGameByGameId(new GameId(uuid));
        verify(gameResponseMapper, times(1)).gameToGameResponseModel(game);
    }

    @Test
    public void whenGetGameById_nonExistingUuid_thenThrowNotFoundException() {
        // Arrange
        String uuid = UUID.randomUUID().toString();
        when(gameRepository.findGameByGameId(new GameId(uuid))).thenReturn(null);

        // Act and Assert
        assertThrows(NotFoundException.class, () -> gameService.getGameById(uuid));
        verify(gameRepository, times(1)).findGameByGameId(new GameId(uuid));
        verify(gameResponseMapper, never()).gameToGameResponseModel((Game) any());
    }

    @Test
    public void whenGetAllGames_gamesExist_thenReturnListOfGameResponseModels() {
        // Arrange
        List<Game> games = Arrays.asList(createTestGame(), createTestGame());
        List<GameResponseModel> responseModels = Arrays.asList(createTestGameResponseModel(), createTestGameResponseModel());
        when(gameRepository.findAll()).thenReturn(games);
        // Note: If your GameResponseMapper has a method to map a List<Game> to List<GameResponseModel>, mock that.
        // Otherwise, you might need to mock the single item mapping for each game in the list.
        // Assuming gameResponseMapper.gameToGameResponseModel(List<Game>) is a valid method.
        when(gameResponseMapper.gameToGameResponseModel(games)).thenReturn(responseModels);


        // Act
        List<GameResponseModel> result = gameService.getAllGames();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(gameRepository, times(1)).findAll();
        verify(gameResponseMapper, times(1)).gameToGameResponseModel(games); // Verify the list mapping call
    }

    @Test
    public void whenGetAllGames_noGamesExist_thenReturnEmptyList() {
        // Arrange
        List<Game> emptyGameList = Collections.emptyList();
        List<GameResponseModel> emptyResponseModelList = Collections.emptyList();
        when(gameRepository.findAll()).thenReturn(emptyGameList);
        when(gameResponseMapper.gameToGameResponseModel(emptyGameList)).thenReturn(emptyResponseModelList);

        // Act
        List<GameResponseModel> result = gameService.getAllGames();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(gameRepository, times(1)).findAll();
        verify(gameResponseMapper, times(1)).gameToGameResponseModel(emptyGameList);
    }


    @Test
    public void whenCreateGame_validRequestModel_thenReturnGameResponseModel() {
        // Arrange
        GameRequestModel requestModel = createTestGameRequestModel();
        Game game = createTestGame();
        GameResponseModel responseModel = createTestGameResponseModel();
        when(gameRequestMapper.requestModelToEntity(requestModel)).thenReturn(game);
        when(gameRepository.save(game)).thenReturn(game);
        when(gameResponseMapper.gameToGameResponseModel(game)).thenReturn(responseModel);

        // Act
        GameResponseModel result = gameService.createGame(requestModel);

        // Assert
        assertNotNull(result);
        assertEquals(responseModel.getTitle(), result.getTitle());
        verify(gameRequestMapper, times(1)).requestModelToEntity(requestModel);
        verify(gameRepository, times(1)).save(game);
        verify(gameResponseMapper, times(1)).gameToGameResponseModel(game);
    }

    @Test
    public void whenCreateGame_invalidPrice_thenReturnNull() {
        // Arrange
        GameRequestModel requestModel = createTestGameRequestModel();
        requestModel.setPrice(-5.0);

        // Act
        GameResponseModel result = gameService.createGame(requestModel);

        // Assert
        assertNull(result);
        verify(gameRequestMapper, never()).requestModelToEntity(any());
        verify(gameRepository, never()).save(any());
        verify(gameResponseMapper, never()).gameToGameResponseModel((Game) any());
    }

    @Test
    public void whenUpdateGame_validRequestModel_thenReturnGameResponseModel() {
        // Arrange
        GameRequestModel requestModel = createTestGameRequestModel();
        // If your updateGame service method expects the ID in the RequestModel, set it here
         requestModel.setUserId("some-uuid"); // Uncomment and set ID if needed

        Game game = new Game();
        game.setGame_user_id("some-uuid");

        // This is the game entity returned by the repository save
        GameResponseModel responseModel = createTestGameResponseModel(); // This is the final response model

        // Mock the behavior of gameRequestMapper and gameRepository
        when(gameRequestMapper.requestModelToEntity(requestModel)).thenReturn(game);
        when(gameRepository.save(game)).thenReturn(game); // Mock saving the updated game
        when(gameResponseMapper.gameToGameResponseModel(game)).thenReturn(responseModel); // Mock mapping to response model

        // Act
        GameResponseModel result = gameService.updateGame(requestModel);

        // Assert
        assertNotNull(result);
        assertEquals(responseModel.getTitle(), result.getTitle());
        verify(gameRequestMapper, times(1)).requestModelToEntity(requestModel);
        verify(gameRepository, times(1)).save(game);
        verify(gameResponseMapper, times(1)).gameToGameResponseModel(game);
    }

    @Test
    public void whenUpdateGame_nonExistingGame_thenThrowNotFoundException() {
        // Arrange
        GameRequestModel requestModel = createTestGameRequestModel();
        // Set the ID in the request model for the non-existent game
        requestModel.setUserId(UUID.randomUUID().toString());

        // Assuming the service first tries to find the game by ID
        // If your updateGame method loads the existing game like this:
        // Game existingGame = gameRepository.findGameByGameId(new GameId(requestModel.getId()));
        // Then mocks need to reflect the 'not found' scenario:
        when(gameRepository.findGameByGameId(new GameId(requestModel.getUserId()))).thenReturn(null);

        // Act and Assert
        assertThrows(NotFoundException.class, () -> gameService.updateGame(requestModel));

        // Verify interactions
        verify(gameRepository, times(1)).findGameByGameId(new GameId(requestModel.getUserId()));
        verify(gameRequestMapper, never()).requestModelToEntity(any()); // Should not proceed to map if not found
        verify(gameRepository, never()).save(any()); // Should not proceed to save if not found
        verify(gameResponseMapper, never()).gameToGameResponseModel((Game) any()); // Should not proceed to map response
    }


    @Test
    public void whenDeleteGame_existingUuid_thenReturnGameResponseModel() {
        // Arrange
        String uuid = UUID.randomUUID().toString();
        Game game = createTestGame();
        GameResponseModel responseModel = createTestGameResponseModel();
        when(gameRepository.findGameByGameId(new GameId(uuid))).thenReturn(game);
        doNothing().when(gameRepository).delete(game);
        when(gameResponseMapper.gameToGameResponseModel(game)).thenReturn(responseModel);

        // Act
        GameResponseModel result = gameService.deleteGame(uuid);

        // Assert
        assertNotNull(result);
        assertEquals(responseModel.getTitle(), result.getTitle());
        verify(gameRepository, times(1)).findGameByGameId(new GameId(uuid));
        verify(gameRepository, times(1)).delete(game);
        verify(gameResponseMapper, times(1)).gameToGameResponseModel(game);
    }

    @Test
    public void whenDeleteGame_nonExistingUuid_thenThrowNotFoundException() {
        // Arrange
        String uuid = UUID.randomUUID().toString();
        when(gameRepository.findGameByGameId(new GameId(uuid))).thenReturn(null);

        // Act and Assert
        assertThrows(NotFoundException.class, () -> gameService.deleteGame(uuid));

        // Verify interactions
        verify(gameRepository, times(1)).findGameByGameId(new GameId(uuid));
        verify(gameRepository, never()).delete(any()); // Should not attempt to delete
        verify(gameResponseMapper, never()).gameToGameResponseModel((Game) any()); // Should not map response
    }


    @Test
    public void whenAddReview_existingGameId_thenReturnUpdatedGameResponseModel() {
        // Arrange
        String gameId = UUID.randomUUID().toString();
        Game game = createTestGame();
        ReviewRequestModel reviewRequestModel = createTestReviewRequestModel();
        Review review = createTestReview();
        GameResponseModel responseModel = createTestGameResponseModel();

        when(gameRepository.findGameByGameId(new GameId(gameId))).thenReturn(game);
        when(reviewMapper.reviewRequestModelToReview(reviewRequestModel)).thenReturn(review);

        // Mock the save call on the ReviewRepository
        when(reviewRepository.save(review)).thenReturn(review); // <-- Mocking the save call

        // Add the saved review to the game entity (simulate the service logic)
        game.getReviews().add(review); // Assuming your service adds the review to the game's list

        when(gameRepository.save(game)).thenReturn(game); // Mock saving the updated game entity
        when(gameResponseMapper.gameToGameResponseModel(game)).thenReturn(responseModel);

        // Act
        GameResponseModel result = gameService.addReview(reviewRequestModel, gameId);

        // Assert
        assertNotNull(result);
        assertEquals(responseModel.getTitle(), result.getTitle());
        // Assert that the review was added to the game entity mocked object
        // Depending on your service implementation, you might assert on the list size directly or via the saved game mock
        // assertEquals(1, game.getReviews().size()); // Assert on the mocked game object's list
        // assertEquals(review.getComment(), game.getReviews().get(0).getComment());

        verify(gameRepository, times(1)).findGameByGameId(new GameId(gameId));
        verify(reviewMapper, times(1)).reviewRequestModelToReview(reviewRequestModel);
        verify(reviewRepository, times(1)).save(review); // <-- Verify save on ReviewRepository
        verify(gameRepository, times(1)).save(game); // Verify save on GameRepository (if your service saves the Game entity after adding the review)
        verify(gameResponseMapper, times(1)).gameToGameResponseModel(game);
    }

    @Test
    public void whenAddReview_nonExistingGameId_thenThrowNotFoundException() {
        // Arrange
        String gameId = UUID.randomUUID().toString();
        ReviewRequestModel reviewRequestModel = createTestReviewRequestModel();

        when(gameRepository.findGameByGameId(new GameId(gameId))).thenReturn(null);

        // Act and Assert
        assertThrows(NotFoundException.class, () -> gameService.addReview(reviewRequestModel, gameId));

        // Verify interactions
        verify(gameRepository, times(1)).findGameByGameId(new GameId(gameId));
        verify(reviewMapper, never()).reviewRequestModelToReview(any());
        verify(reviewRepository, never()).save(any()); // Should not attempt to save review
        verify(gameRepository, never()).save(any()); // Should not attempt to save game
        verify(gameResponseMapper, never()).gameToGameResponseModel((Game) any());
    }
    public void addGameToUser(String uuid, String gameId){
        Game game = gameRepository.findGameByGameId(new GameId(uuid)); // <-- This line looks for a Game using the 'uuid' parameter (which you called 'uuid' and the parameter name 'gameId'). This seems incorrect logic. You should likely be finding the Game using the 'gameId' parameter, not the 'uuid' parameter.
        gameRepository.save(game); // <-- This just saves the game entity itself. It doesn't explicitly link the game to the user identified by the 'gameId' parameter unless your 'Game' entity has a field/relationship to the user and finding/saving implicitly handles this.
    }

    @Test
    public void whenUpdateGame_existingGameFoundAndSaveSuccessful_thenReturnUpdatedGameResponseModel() {
        // Arrange
        GameRequestModel inputRequestModel = createTestGameRequestModel();
        String userIdUsedForLookup = inputRequestModel.getUserId(); // Get the user ID from the request model

        // 1. Mock the behavior of gameRepository.findGameByGameId
        // This call uses the userId from the request model. It needs to return a non-null Game
        // for the 'if (game == null)' check to be skipped.
        Game foundGameByUserId = createTestGame(); // This game object itself doesn't need all fields populated for this specific test
        foundGameByUserId.setGameId(new GameId(userIdUsedForLookup)); // Set ID to match the lookup logic in the service method
        when(gameRepository.findGameByGameId(new GameId(userIdUsedForLookup))).thenReturn(foundGameByUserId);

        // 2. Mock the behavior of gameRequestMapper.requestModelToEntity
        // It is called with the input request model and should return a Game entity
        Game entityToSave = createTestGame(); // This represents the Game entity ready to be saved/updated
        // Important: If your update logic in the service relies on the ID being in the entityToSave
        // (which is common when using save for update), ensure createTestGame sets an ID or
        // set it here, ideally matching the ID you expect to be updated.
        // entityToSave.setGameId(new GameId("expected-game-id")); // Set the ID if needed

        when(gameRequestMapper.requestModelToEntity(inputRequestModel)).thenReturn(entityToSave);

        // 3. Mock the behavior of gameRepository.save
        // It is called with the entityToSave and should return the saved/updated Game entity
        Game savedEntity = createTestGame(); // This represents the Game entity returned AFTER saving
        // Ensure savedEntity has the state you expect AFTER the update
        // savedEntity.setGameId(entityToSave.getGameId()); // Keep the ID consistent
        savedEntity.setTitle("Updated Title from Save"); // Example: The saved entity reflects the update

        when(gameRepository.save(entityToSave)).thenReturn(savedEntity);

        // 4. Mock the behavior of gameResponseMapper.gameToGameResponseModel
        // It is called with the savedEntity and should return the final GameResponseModel
        GameResponseModel expectedResponseModel = createTestGameResponseModel();
        // Ensure the expectedResponseModel reflects the data from the savedEntity
        // expectedResponseModel.setId(savedEntity.getGameId().getUuid());
        // expectedResponseModel.setTitle(savedEntity.getTitle()); // Match the updated title

        when(gameResponseMapper.gameToGameResponseModel(savedEntity)).thenReturn(expectedResponseModel);

        // Act
        // Call the service method with the input request model
        GameResponseModel actualResponseModel = gameService.updateGame(inputRequestModel);

        // Assert
        // Verify that the returned response model is the one we expected
        assertNotNull(actualResponseModel);
        assertEquals(expectedResponseModel, actualResponseModel);
        // You can add more specific assertions if needed, e.g., comparing field by field

        // Verify the interactions with the mocked dependencies
        // Verify the initial find call was made with the correct GameId (derived from user ID)
        verify(gameRepository, times(1)).findGameByGameId(new GameId(userIdUsedForLookup));
        // Verify the request model was mapped to an entity
        verify(gameRequestMapper, times(1)).requestModelToEntity(inputRequestModel);
        // Verify the entity returned by the mapper was passed to the save method
        verify(gameRepository, times(1)).save(entityToSave);
        // Verify the entity returned by the save method was passed to the response mapper
        verify(gameResponseMapper, times(1)).gameToGameResponseModel(savedEntity);

        // Verify that the NotFoundException was NOT thrown (implicitly done by the test passing)
    }

}