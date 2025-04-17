package com.champsoft.gamemanagement.BuisinessLogic;

import com.champsoft.gamemanagement.BusinessLogic.GameService;
import com.champsoft.gamemanagement.DataAccess.Game;
import com.champsoft.gamemanagement.DataAccess.GameId;
import com.champsoft.gamemanagement.DataAccess.GameRepository;
import com.champsoft.gamemanagement.DataAccess.Review;
import com.champsoft.gamemanagement.DataMapper.GameRequestMapper;
import com.champsoft.gamemanagement.DataMapper.GameResponseMapper;
import com.champsoft.gamemanagement.DataMapper.ReviewMapper;
import com.champsoft.gamemanagement.Presentation.DTOS.GameRequestModel;
import com.champsoft.gamemanagement.Presentation.DTOS.GameResponseModel;
import com.champsoft.gamemanagement.Presentation.DTOS.ReviewRequestModel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class GameServiceTest {

    @MockitoBean
    private GameResponseMapper gameResponseMapper;

    @MockitoBean
    private GameRepository gameRepository;

    @MockitoBean
    private GameRequestMapper gameRequestMapper;

    @MockitoBean
    private ReviewMapper reviewMapper;

    @InjectMocks
    private GameService gameService;

    @Test
    void getGameById_existingGame_returnsGameResponseModel() {
        // Arrange
        String uuid = UUID.randomUUID().toString();
        Game game = new Game();
        game.setGameId(new GameId(uuid));
        GameResponseModel expectedResponse = new GameResponseModel();

        when(gameRepository.findGameByGameId_uuid(uuid)).thenReturn(game);
        when(gameResponseMapper.gameToGameResponseModel(game)).thenReturn(expectedResponse);

        // Act
        GameResponseModel actualResponse = gameService.getGameById(uuid);

        // Assert
        assertEquals(expectedResponse, actualResponse);
        verify(gameRepository, times(1)).findGameByGameId_uuid(uuid);
        verify(gameResponseMapper, times(1)).gameToGameResponseModel(game);
    }

    @Test
    void getGameById_nonExistingGame_returnsNull() {
        // Arrange
        String uuid = UUID.randomUUID().toString();

        when(gameRepository.findGameByGameId_uuid(uuid)).thenReturn(null);
        when(gameResponseMapper.gameToGameResponseModel((Game) null)).thenReturn(null); // Or handle null mapping in your mapper

        // Act
        GameResponseModel actualResponse = gameService.getGameById(uuid);

        // Assert
        assertNull(actualResponse);
        verify(gameRepository, times(1)).findGameByGameId_uuid(uuid);
        verify(gameResponseMapper, times(1)).gameToGameResponseModel((Game) null);
    }

    @Test
    void getAllGames_returnsListOfGameResponseModels() {
        // Arrange
        List<Game> games = new ArrayList<>();
        games.add(new Game());
        games.add(new Game());
        List<GameResponseModel> expectedResponse = new ArrayList<>();
        expectedResponse.add(new GameResponseModel());
        expectedResponse.add(new GameResponseModel());

        when(gameRepository.findAll()).thenReturn(games);
        when(gameResponseMapper.gameToGameResponseModel(games)).thenReturn(expectedResponse);

        // Act
        List<GameResponseModel> actualResponse = gameService.getAllGames();

        // Assert
        assertEquals(expectedResponse, actualResponse);
        verify(gameRepository, times(1)).findAll();
        verify(gameResponseMapper, times(1)).gameToGameResponseModel(games);
    }

    @Test
    void getAllGames_noGames_returnsEmptyList() {
        // Arrange
        List<Game> games = new ArrayList<>();
        List<GameResponseModel> expectedResponse = new ArrayList<>();

        when(gameRepository.findAll()).thenReturn(games);
        when(gameResponseMapper.gameToGameResponseModel(games)).thenReturn(expectedResponse);

        // Act
        List<GameResponseModel> actualResponse = gameService.getAllGames();

        // Assert
        assertEquals(expectedResponse, actualResponse);
        verify(gameRepository, times(1)).findAll();
        verify(gameResponseMapper, times(1)).gameToGameResponseModel(games);
    }

    @Test
    void createGame_validRequest_returnsCreatedGameResponseModel() {
        // Arrange
        GameRequestModel requestModel = new GameRequestModel();
        requestModel.setPrice(10.0);
        Game gameToSave = new Game();
        Game savedGame = new Game();
        GameResponseModel expectedResponse = new GameResponseModel();

        when(gameRequestMapper.requestModelToEntity(requestModel)).thenReturn(gameToSave);
        when(gameRepository.save(gameToSave)).thenReturn(savedGame);
        when(gameResponseMapper.gameToGameResponseModel(savedGame)).thenReturn(expectedResponse);

        // Act
        GameResponseModel actualResponse = gameService.createGame(requestModel);

        // Assert
        assertEquals(expectedResponse, actualResponse);
        verify(gameRequestMapper, times(1)).requestModelToEntity(requestModel);
        verify(gameRepository, times(1)).save(gameToSave);
        verify(gameResponseMapper, times(1)).gameToGameResponseModel(savedGame);
    }

    @Test
    void createGame_invalidPrice_returnsNull() {
        // Arrange
        GameRequestModel requestModel = new GameRequestModel();
        requestModel.setPrice(-5.0);

        // Act
        GameResponseModel actualResponse = gameService.createGame(requestModel);

        // Assert
        assertNull(actualResponse);
        verify(gameRequestMapper, never()).requestModelToEntity(any());
        verify(gameRepository, never()).save(any());
        verify(gameResponseMapper, never()).gameToGameResponseModel((Game) any());
    }

    @Test
    void updateGame_validRequest_returnsUpdatedGameResponseModel() {
        // Arrange
        GameRequestModel requestModel = new GameRequestModel();
        Game gameToUpdate = new Game();
        Game updatedGame = new Game();
        GameResponseModel expectedResponse = new GameResponseModel();

        when(gameRequestMapper.requestModelToEntity(requestModel)).thenReturn(gameToUpdate);
        when(gameRepository.save(gameToUpdate)).thenReturn(updatedGame);
        when(gameResponseMapper.gameToGameResponseModel(updatedGame)).thenReturn(expectedResponse);

        // Act
        GameResponseModel actualResponse = gameService.updateGame(requestModel);

        // Assert
        assertEquals(expectedResponse, actualResponse);
        verify(gameRequestMapper, times(1)).requestModelToEntity(requestModel);
        verify(gameRepository, times(1)).save(gameToUpdate);
        verify(gameResponseMapper, times(1)).gameToGameResponseModel(updatedGame);
    }

    @Test
    void deleteGame_existingGame_returnsDeletedGameResponseModel() {
        // Arrange
        String uuid = UUID.randomUUID().toString();
        Game gameToDelete = new Game();
        gameToDelete.setGameId(new GameId(uuid));
        GameResponseModel expectedResponse = new GameResponseModel();

        when(gameRepository.findGameByGameId_uuid(uuid)).thenReturn(gameToDelete);
        when(gameResponseMapper.gameToGameResponseModel(gameToDelete)).thenReturn(expectedResponse);
        doNothing().when(gameRepository).delete(gameToDelete);

        // Act
        GameResponseModel actualResponse = gameService.deleteGame(uuid);

        // Assert
        assertEquals(expectedResponse, actualResponse);
        verify(gameRepository, times(1)).findGameByGameId_uuid(uuid);
        verify(gameRepository, times(1)).delete(gameToDelete);
        verify(gameResponseMapper, times(1)).gameToGameResponseModel(gameToDelete);
    }

    @Test
    void deleteGame_nonExistingGame_returnsNull() {
        // Arrange
        String uuid = UUID.randomUUID().toString();

        when(gameRepository.findGameByGameId_uuid(uuid)).thenReturn(null);
        when(gameResponseMapper.gameToGameResponseModel((Game) null)).thenReturn(null); // Or handle null mapping

        // Act
        GameResponseModel actualResponse = gameService.deleteGame(uuid);

        // Assert
        assertNull(actualResponse);
        verify(gameRepository, times(1)).findGameByGameId_uuid(uuid);
        verify(gameRepository, never()).delete(any());
        verify(gameResponseMapper, times(1)).gameToGameResponseModel((Game) null);
    }

    @Test
    void addReview_existingGame_returnsUpdatedGameResponseModel() {
        // Arrange
        String gameId = UUID.randomUUID().toString();
        ReviewRequestModel reviewRequestModel = new ReviewRequestModel();
        Review review = new Review();
        Game game = new Game();
        game.setGameId(new GameId(gameId));
        Game updatedGame = new Game();
        updatedGame.setGameId(new GameId(gameId));
        List<Review> reviews = new ArrayList<>();
        reviews.add(review);
        updatedGame.setReviews(reviews);
        GameResponseModel expectedResponse = new GameResponseModel();

        when(gameRepository.findGameByGameId_uuid(gameId)).thenReturn(game);
        when(reviewMapper.reviewRequestModelToReview(reviewRequestModel)).thenReturn(review);
        when(gameRepository.save(game)).thenReturn(updatedGame);
        when(gameResponseMapper.gameToGameResponseModel(updatedGame)).thenReturn(expectedResponse);

        // Act
        GameResponseModel actualResponse = gameService.addReview(reviewRequestModel, gameId);

        // Assert
        assertEquals(expectedResponse, actualResponse);
        assertEquals(1, game.getReviews().size());
        verify(gameRepository, times(1)).findGameByGameId_uuid(gameId);
        verify(reviewMapper, times(1)).reviewRequestModelToReview(reviewRequestModel);
        verify(gameRepository, times(1)).save(game);
        verify(gameResponseMapper, times(1)).gameToGameResponseModel(updatedGame);
    }

    @Test
    void addReview_existingGameWithExistingReviews_returnsUpdatedGameResponseModelWithNewReview() {
        // Arrange
        String gameId = UUID.randomUUID().toString();
        ReviewRequestModel reviewRequestModel = new ReviewRequestModel();
        Review newReview = new Review();
        Game game = new Game();
        game.setGameId(new GameId(gameId));
        List<Review> existingReviews = new ArrayList<>();
        existingReviews.add(new Review());
        game.setReviews(existingReviews);
        Game updatedGame = new Game();
        updatedGame.setGameId(new GameId(gameId));
        List<Review> reviews = new ArrayList<>(existingReviews);
        reviews.add(newReview);
        updatedGame.setReviews(reviews);
        GameResponseModel expectedResponse = new GameResponseModel();

        when(gameRepository.findGameByGameId_uuid(gameId)).thenReturn(game);
        when(reviewMapper.reviewRequestModelToReview(reviewRequestModel)).thenReturn(newReview);
        when(gameRepository.save(game)).thenReturn(updatedGame);
        when(gameResponseMapper.gameToGameResponseModel(updatedGame)).thenReturn(expectedResponse);

        // Act
        GameResponseModel actualResponse = gameService.addReview(reviewRequestModel, gameId);

        // Assert
        assertEquals(expectedResponse, actualResponse);
        assertEquals(2, game.getReviews().size());
        verify(gameRepository, times(1)).findGameByGameId_uuid(gameId);
        verify(reviewMapper, times(1)).reviewRequestModelToReview(reviewRequestModel);
        verify(gameRepository, times(1)).save(game);
        verify(gameResponseMapper, times(1)).gameToGameResponseModel(updatedGame);
    }

    @Test
    void addReview_nonExistingGame_doesNotAddReview() {
        // Arrange
        String gameId = UUID.randomUUID().toString();
        ReviewRequestModel reviewRequestModel = new ReviewRequestModel();

        when(gameRepository.findGameByGameId_uuid(gameId)).thenReturn(null);

        // Act
        GameResponseModel actualResponse = gameService.addReview(reviewRequestModel, gameId);

        // Assert
        assertNull(actualResponse);
        verify(gameRepository, times(1)).findGameByGameId_uuid(gameId);
        verify(reviewMapper, never()).reviewRequestModelToReview(any());
        verify(gameRepository, never()).save(any());
        verify(gameResponseMapper, never()).gameToGameResponseModel((Game) any());
    }

    @Test
    void addGameToUser_existingGame_callsSave() {
        // Arrange
        String userId = UUID.randomUUID().toString();
        String gameId = UUID.randomUUID().toString();
        Game game = new Game();
        game.setGameId(new GameId(gameId));

        when(gameRepository.findGameByGameId_uuid(gameId)).thenReturn(game);
        when(gameRepository.save(game)).thenReturn(game);

        // Act
        gameService.addGameToUser(userId, gameId);

        // Assert
        verify(gameRepository, times(1)).findGameByGameId_uuid(gameId);
        verify(gameRepository, times(1)).save(game);
        // Note: The 'userId' parameter is currently not used in the implementation.
    }

    @Test
    void addGameToUser_nonExistingGame_doesNotCallSave() {
        // Arrange
        String userId = UUID.randomUUID().toString();
        String gameId = UUID.randomUUID().toString();

        when(gameRepository.findGameByGameId_uuid(gameId)).thenReturn(null);

        // Act
        gameService.addGameToUser(userId, gameId);

        // Assert
        verify(gameRepository, times(1)).findGameByGameId_uuid(gameId);
        verify(gameRepository, never()).save(any());
    }
}