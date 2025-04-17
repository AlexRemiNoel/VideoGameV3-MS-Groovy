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
        game.setUserId("testUser");
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
        return review;
    }

    private ReviewRequestModel createTestReviewRequestModel() {
        ReviewRequestModel requestModel = new ReviewRequestModel();
        requestModel.setComment("Amazing!");
        return requestModel;
    }

    @Test
    public void whenGetGameById_existingUuid_thenReturnGameResponseModel() {
        // Arrange
        String uuid = UUID.randomUUID().toString();
        Game game = createTestGame();
        GameResponseModel responseModel = createTestGameResponseModel();
        when(gameRepository.findGameByGameId_uuid(new GameId(uuid))).thenReturn(game);
        when(gameResponseMapper.gameToGameResponseModel(game)).thenReturn(responseModel);

        // Act
        GameResponseModel result = gameService.getGameById(uuid);

        // Assert
        assertNotNull(result);
        assertEquals(responseModel.getId(), result.getId());
        verify(gameRepository, times(1)).findGameByGameId_uuid(new GameId(uuid));
        verify(gameResponseMapper, times(1)).gameToGameResponseModel(game);
    }

    @Test
    public void whenGetGameById_nonExistingUuid_thenThrowNotFoundException() {
        // Arrange
        String uuid = UUID.randomUUID().toString();
        when(gameRepository.findGameByGameId_uuid(new GameId(uuid))).thenReturn(null);

        // Act and Assert
        assertThrows(NotFoundException.class, () -> gameService.getGameById(uuid));
        verify(gameRepository, times(1)).findGameByGameId_uuid(new GameId(uuid));
        verify(gameResponseMapper, never()).gameToGameResponseModel((Game) any());
    }

    @Test
    public void whenGetAllGames_gamesExist_thenReturnListOfGameResponseModels() {
        // Arrange
        List<Game> games = Arrays.asList(createTestGame(), createTestGame());
        List<GameResponseModel> responseModels = Arrays.asList(createTestGameResponseModel(), createTestGameResponseModel());
        when(gameRepository.findAll()).thenReturn(games);
        when(gameResponseMapper.gameToGameResponseModel(games)).thenReturn(responseModels);

        // Act
        List<GameResponseModel> result = gameService.getAllGames();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(gameRepository, times(1)).findAll();
        verify(gameResponseMapper, times(1)).gameToGameResponseModel(games);
    }

    @Test
    public void whenGetAllGames_noGamesExist_thenReturnEmptyList() {
        // Arrange
        when(gameRepository.findAll()).thenReturn(Collections.emptyList());
        when(gameResponseMapper.gameToGameResponseModel(Collections.emptyList())).thenReturn(Collections.emptyList());

        // Act
        List<GameResponseModel> result = gameService.getAllGames();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(gameRepository, times(1)).findAll();
        verify(gameResponseMapper, times(1)).gameToGameResponseModel(Collections.emptyList());
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
        Game game = createTestGame();
        GameResponseModel responseModel = createTestGameResponseModel();
        when(gameRequestMapper.requestModelToEntity(requestModel)).thenReturn(game);
        when(gameRepository.save(game)).thenReturn(game);
        when(gameResponseMapper.gameToGameResponseModel(game)).thenReturn(responseModel);

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
    public void whenDeleteGame_existingUuid_thenReturnGameResponseModel() {
        // Arrange
        String uuid = UUID.randomUUID().toString();
        Game game = createTestGame();
        GameResponseModel responseModel = createTestGameResponseModel();
        when(gameRepository.findGameByGameId_uuid(new GameId(uuid))).thenReturn(game);
        doNothing().when(gameRepository).delete(game);
        when(gameResponseMapper.gameToGameResponseModel(game)).thenReturn(responseModel);

        // Act
        GameResponseModel result = gameService.deleteGame(uuid);

        // Assert
        assertNotNull(result);
        assertEquals(responseModel.getTitle(), result.getTitle());
        verify(gameRepository, times(1)).findGameByGameId_uuid(new GameId(uuid));
        verify(gameRepository, times(1)).delete(game);
        verify(gameResponseMapper, times(1)).gameToGameResponseModel(game);
    }

//    @Test
//    public void whenDeleteGame_nonExistingUuid_thenReturnNullAndNotThrowException() {
//        // Arrange
//        String uuid = UUID.randomUUID().toString();
//        when(gameRepository.findGameByGameId_uuid(new GameId(uuid))).thenReturn(null);
//
//        // Act
//        GameResponseModel result = gameService.deleteGame(uuid);
//
//        // Assert
//        assertNull(result);
//        verify(gameRepository, times(1)).findGameByGameId_uuid(new GameId(uuid));
//        verify(gameRepository, never()).delete(any());
//        verify(gameResponseMapper, never()).gameToGameResponseModel((Game) any());
//    }


    @Test
    public void whenAddReview_existingGameId_thenReturnUpdatedGameResponseModel() {
        // Arrange
        String gameId = UUID.randomUUID().toString();
        Game game = createTestGame();
        ReviewRequestModel reviewRequestModel = createTestReviewRequestModel();
        Review review = createTestReview();
        GameResponseModel responseModel = createTestGameResponseModel();
        when(gameRepository.findGameByGameId_uuid(new GameId(gameId))).thenReturn(game);
        when(reviewMapper.reviewRequestModelToReview(reviewRequestModel)).thenReturn(review);
        when(gameRepository.save(game)).thenReturn(game);
        when(gameResponseMapper.gameToGameResponseModel(game)).thenReturn(responseModel);

        // Act
        GameResponseModel result = gameService.addReview(reviewRequestModel, gameId);

        // Assert
        assertNotNull(result);
        assertEquals(responseModel.getTitle(), result.getTitle());
        assertEquals(1, game.getReviews().size());
        assertEquals(review.getComment(), game.getReviews().get(0).getComment());
        verify(gameRepository, times(1)).findGameByGameId_uuid(new GameId(gameId));
        verify(reviewMapper, times(1)).reviewRequestModelToReview(reviewRequestModel);
        verify(gameRepository, times(1)).save(game);
        verify(gameResponseMapper, times(1)).gameToGameResponseModel(game);
    }

//    @Test
//    public void whenAddReview_nonExistingGameId_thenReturnNull() {
//        // Arrange
//        String gameId = UUID.randomUUID().toString();
//        ReviewRequestModel reviewRequestModel = createTestReviewRequestModel();
//        when(gameRepository.findGameByGameId_uuid(new GameId(gameId))).thenReturn(null);
//
//        // Act
//        GameResponseModel result = gameService.addReview(reviewRequestModel, gameId);
//
//        // Assert
//        assertNull(result);
//        verify(gameRepository, times(1)).findGameByGameId_uuid(new GameId(gameId));
//        verify(reviewMapper, never()).reviewRequestModelToReview(any());
//        verify(gameRepository, never()).save(any());
//        verify(gameResponseMapper, never()).gameToGameResponseModel((Game) any());
//    }



}