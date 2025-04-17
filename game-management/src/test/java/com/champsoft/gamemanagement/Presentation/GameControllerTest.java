package com.champsoft.gamemanagement.Presentation;

import com.champsoft.gamemanagement.BusinessLogic.GameService;
import com.champsoft.gamemanagement.Presentation.DTOS.GameController;
import com.champsoft.gamemanagement.Presentation.DTOS.GameRequestModel;
import com.champsoft.gamemanagement.Presentation.DTOS.GameResponseModel;
import com.champsoft.gamemanagement.Presentation.DTOS.ReviewRequestModel;
import com.champsoft.gamemanagement.utils.exceptions.NotFoundException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.util.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import org.junit.jupiter.api.Test;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;

import static net.bytebuddy.matcher.ElementMatchers.is;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static org.mockito.Mockito.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static org.springframework.mock.http.server.reactive.MockServerHttpRequest.post;
import static org.springframework.mock.http.server.reactive.MockServerHttpRequest.put;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = GameController.class)
@WebMvcTest(GameController.class)
public class GameControllerTest {


    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    private final String VALID_CUSTOMER_ID = "c3540a89-cb47-4c96-888e-ff96708db4d8";
    private final String NOT_FOUND_CUSTOMER_ID = "c3540a89-cb47-4c96-888e-ff96708db4d0";
    private final String INVALID_CUSTOMER_ID = "c3540a89-cb47-4c96-888e-ff96708db4d";
    @MockitoBean
    GameService gameService;
    @Autowired
    GameController gameController;
    @Test
    public void whenNoGameExist_ThenReturnEmptyList() {
//arrange
        when(gameService.getAllGames()).thenReturn(Collections.emptyList());
//act
        ResponseEntity<List<GameResponseModel>>
                gameResponseEntity = gameController.getAllGames();
//assert
        assertNotNull(gameResponseEntity);
        assertEquals(gameResponseEntity.getStatusCode(), HttpStatus.OK);
        assertArrayEquals(gameResponseEntity.getBody().toArray(),
                new ArrayList<GameResponseModel>().toArray());
        verify(gameService, times(1)).getAllGames();
    }
    @Test
    public void whenGameExist_ThenReturnGame() {
        //arrange
        GameResponseModel gameResponseModel = new GameResponseModel();
        List<GameResponseModel> gameResponseModelList = new ArrayList<>(
                Arrays.asList(gameResponseModel,gameResponseModel)
        );
        when (gameService.getAllGames()).thenReturn(gameResponseModelList);
        //act
        ResponseEntity<List<GameResponseModel>> gameResponseEntity = gameController.getAllGames();
        //assert
        assertNotNull(gameResponseEntity);
        assertEquals(gameResponseEntity.getStatusCode(), HttpStatus.OK);
        assertNotNull(gameResponseEntity.getBody());
        assertEquals(gameResponseEntity.getBody().size(), 2);
        assertArrayEquals(gameResponseEntity.getBody().toArray(), gameResponseModelList.toArray());
        verify(gameService, times(1)).getAllGames();

    }

    @Test
    public void whenGameNotFoundOnGet_ThenThrowNotFoundException() {
        //arrange
        String notFoundCustomerId = "some-not-found-uuid"; // Use a specific value for the ID
        String expectedErrorMessage = "Game with UUID: " + notFoundCustomerId + " not found";

        when(gameService.getGameById(notFoundCustomerId))
                .thenThrow(new NotFoundException(expectedErrorMessage)); // Pass the expected message to the exception

        //act
        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            gameController.getGameByGameId_uuid(notFoundCustomerId);
        });

        //assert
        assertEquals(expectedErrorMessage, exception.getMessage());

        verify(gameService, times(1)).getGameById(notFoundCustomerId);
    }
    @Test
    public void whenGameExists_ThenDeleteGame() {
        //arrange
        GameResponseModel deletedGame = new GameResponseModel(); // Or however you create an instance
        when(gameService.deleteGame(VALID_CUSTOMER_ID))
                .thenReturn(deletedGame);

        //act
        ResponseEntity<GameResponseModel> responseEntity = gameController.deleteGame(VALID_CUSTOMER_ID);

        //assert
        assertNotNull(responseEntity);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode()); // Corrected assertion order
        verify(gameService, times(1)).deleteGame(VALID_CUSTOMER_ID);
    }

    @Test
    public void whenCustomerDoesNotExistOnDelete_ThenThrowNotFoundException() {
        //arrange
        String notFoundGameId = "unknown-customer-id"; // Use a specific value
        doThrow(new NotFoundException("Unknown customerId: " + notFoundGameId))
                .when(gameService).deleteGame(notFoundGameId);

        //act
        NotFoundException exception = assertThrowsExactly(NotFoundException.class, () -> {
            gameController.deleteGame(notFoundGameId);
        });

        //assert
        assertEquals("Unknown customerId: " + notFoundGameId, exception.getMessage());
        verify(gameService, times(1)).deleteGame(notFoundGameId);
    }
    @Test
    public void whenGetAllGames_ThenReturnOkAndListOfGames() {
        // arrange
        // Create a list of mock GameResponseModel objects
        GameResponseModel game1 = new GameResponseModel();
        game1.setId("game123");
        game1.setTitle("Awesome Game 1");

        GameResponseModel game2 = new GameResponseModel();
        game2.setId("game456");
        game2.setTitle("Epic Adventure 2");

        List<GameResponseModel> mockGames = Arrays.asList(game1, game2);

        // Configure the mock gameService to return the list of games
        when(gameService.getAllGames()).thenReturn(mockGames);

        // act
        ResponseEntity<List<GameResponseModel>> responseEntity = gameController.getAllGames();

        // assert
        // Verify the HTTP status code is OK (200)
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

        // Verify the response body is not null
        assertNotNull(responseEntity.getBody());

        // Verify the response body contains the expected list of games
        assertEquals(mockGames.size(), responseEntity.getBody().size());
        assertEquals(game1.getId(), responseEntity.getBody().get(0).getId());
        assertEquals(game1.getTitle(), responseEntity.getBody().get(0).getTitle());
        assertEquals(game2.getId(), responseEntity.getBody().get(1).getId());
        assertEquals(game2.getTitle(), responseEntity.getBody().get(1).getTitle());

        // Verify that the gameService's getAllGames method was called exactly once
        verify(gameService, times(1)).getAllGames();
    }
    @Test
    public void whenGameExistsOnGet_ThenReturnOkAndGame() throws Exception {
        // Arrange
        GameResponseModel gameResponseModel = new GameResponseModel();
        gameResponseModel.setId(VALID_CUSTOMER_ID);
        gameResponseModel.setTitle("Test Game");
        when(gameService.getGameById(VALID_CUSTOMER_ID)).thenReturn(gameResponseModel);

        // Act & Assert
        mockMvc.perform(get("/api/v1/game/" + VALID_CUSTOMER_ID))
                .andExpect(status().isOk())
                .andExpect((ResultMatcher) content().contentType(MediaType.APPLICATION_JSON))
                .andExpect((ResultMatcher) jsonPath("$.gameId", is(VALID_CUSTOMER_ID)))
                .andExpect((ResultMatcher) jsonPath("$.title", is("Test Game")));

        verify(gameService, times(1)).getGameById(VALID_CUSTOMER_ID);
    }

//    @Test
//    public void whenAddGame_validInput_thenReturnCreatedAndGame() throws Exception {
//        // Arrange
//        GameRequestModel gameRequestModel = new GameRequestModel();
//        gameRequestModel.setTitle("New Game");
//        gameRequestModel.setPrice(29.99);
//
//        GameResponseModel gameResponseModel = new GameResponseModel();
//        gameResponseModel.setId(UUID.randomUUID().toString());
//        gameResponseModel.setTitle("New Game");
//        gameResponseModel.setPrice(29.99);
//
//        when(gameService.createGame(any(GameRequestModel.class))).thenReturn(gameResponseModel);
//
//        // Act & Assert
//        mockMvc.perform(post("/api/v1/game")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(gameRequestModel)))
//                .andExpect(status().isCreated())
//                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
//                .andExpect(jsonPath("$.title", is("New Game")))
//                .andExpect(jsonPath("$.price", is(29.99)));
//
//        verify(gameService, times(1)).createGame(any(GameRequestModel.class));
//    }
//
//    @Test
//    public void whenAddGame_invalidInput_thenReturnBadRequest() throws Exception {
//        // Arrange
//        GameRequestModel gameRequestModel = new GameRequestModel();
//        gameRequestModel.setTitle("Invalid Game");
//        gameRequestModel.setPrice(-5.0); // Invalid price
//
//        when(gameService.createGame(any(GameRequestModel.class))).thenReturn(null);
//
//        // Act & Assert
//        mockMvc.perform(post("/api/v1/game")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(gameRequestModel)))
//                .andExpect(status().isBadRequest());
//
//        verify(gameService, times(1)).createGame(any(GameRequestModel.class));
//    }
//
//    @Test
//    public void whenUpdateGame_validInputAndExistingGame_thenReturnOkAndUpdatedGame() throws Exception {
//        // Arrange
//        GameRequestModel gameRequestModel = new GameRequestModel();
//        gameRequestModel.setTitle("Updated Game");
//        gameRequestModel.setPrice(39.99);
//
//        GameResponseModel updatedGameResponseModel = new GameResponseModel();
//        updatedGameResponseModel.setId(VALID_CUSTOMER_ID);
//        updatedGameResponseModel.setTitle("Updated Game");
//        updatedGameResponseModel.setPrice(39.99);
//
//        when(gameService.updateGame(any(GameRequestModel.class))).thenReturn(updatedGameResponseModel);
//
//        // Act & Assert
//        mockMvc.perform(put("/api/v1/game")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(gameRequestModel)))
//                .andExpect(status().isOk())
//                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
//                .andExpect(jsonPath("$.gameId", is(VALID_CUSTOMER_ID)))
//                .andExpect(jsonPath("$.title", is("Updated Game")))
//                .andExpect(jsonPath("$.price", is(39.99)));
//
//        verify(gameService, times(1)).updateGame(any(GameRequestModel.class));
//    }
//
//    @Test
//    public void whenUpdateGame_validInputAndNonExistingGame_thenReturnNotFound() throws Exception {
//        // Arrange
//        GameRequestModel gameRequestModel = new GameRequestModel();
//        gameRequestModel.setTitle("Non Existing Game");
//        gameRequestModel.setPrice(19.99);
//
//        when(gameService.updateGame(any(GameRequestModel.class))).thenReturn(null);
//
//        // Act & Assert
//        mockMvc.perform(put("/api/v1/game")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(gameRequestModel)))
//                .andExpect(status().isNotFound());
//
//        verify(gameService, times(1)).updateGame(any(GameRequestModel.class));
//    }
//
//    @Test
//    public void whenReviewGame_validInputAndExistingGame_thenReturnOkAndUpdatedGame() throws Exception {
//        // Arrange
//        String gameUuid = VALID_CUSTOMER_ID;
//        ReviewRequestModel reviewRequestModel = new ReviewRequestModel();
//        reviewRequestModel.setComment("Great game!");
//
//        GameResponseModel updatedGameResponseModel = new GameResponseModel();
//        updatedGameResponseModel.setId(gameUuid);
//        updatedGameResponseModel.setTitle("Existing Game");
//        // Assume reviews are updated in the response model
//        List<String> reviews = new ArrayList<>();
//        reviews.add("Great game!");
//        // updatedGameResponseModel.setReviews(reviews); // If you have a reviews field
//
//        when(gameService.addReview(any(ReviewRequestModel.class), eq(gameUuid))).thenReturn(updatedGameResponseModel);
//
//        // Act & Assert
//        mockMvc.perform(post("/api/v1/game/review/" + gameUuid)
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(reviewRequestModel)))
//                .andExpect(status().isOk())
//                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
//                .andExpect(jsonPath("$.gameId", is(gameUuid)))
//                .andExpect(jsonPath("$.title", is("Existing Game")));
//        // .andExpect(jsonPath("$.reviews", hasSize(1))) // If you have a reviews field
//        // .andExpect(jsonPath("$.reviews[0]", is("Great game!")));
//
//        verify(gameService, times(1)).addReview(any(ReviewRequestModel.class), eq(gameUuid));
//    }
//
//    @Test
//    public void whenReviewGame_validInputAndNonExistingGame_thenReturnNotFound() throws Exception {
//        // Arrange
//        String gameUuid = NOT_FOUND_CUSTOMER_ID;
//        ReviewRequestModel reviewRequestModel = new ReviewRequestModel();
//        reviewRequestModel.setComment("Terrible game!");
//
//        when(gameService.addReview(any(ReviewRequestModel.class), eq(gameUuid))).thenReturn(null);
//
//        // Act & Assert
//        mockMvc.perform(post("/api/v1/game/review/" + gameUuid)
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(reviewRequestModel)))
//                .andExpect(status().isNotFound());
//
//        verify(gameService, times(1)).addReview(any(ReviewRequestModel.class), eq(gameUuid));
//    }
//
//    @Test
//    public void whenAddToLibrary_validInput_thenReturnOk() throws Exception {
//        // Arrange
//        Map<String, String> requestBody = new HashMap<>();
//        requestBody.put("userUuid", UUID.randomUUID().toString());
//        requestBody.put("gameUuid", VALID_CUSTOMER_ID);
//
//        doNothing().when(gameService).addGameToUser(eq(requestBody.get("userUuid")), eq(requestBody.get("gameUuid")));
//
//        // Act & Assert
//        mockMvc.perform(post("/api/v1/game/addToLibrary")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(requestBody)))
//                .andExpect(status().isOk());
//
//        verify(gameService, times(1)).addGameToUser(eq(requestBody.get("userUuid")), eq(requestBody.get("gameUuid")));
//    }
}






//    @Test
//    public void whenCustomerExists_ThenReturnUpdatedCustomer() {
//        //arrange
//        GameRequestModel gameRequestModel = buildGameRequestModel("Betty");
//        GameResponseModel gameResponseModel = buildGameResponseModel("Betty");
//        when(gameService.updateGame(gameRequestModel))
//                .thenReturn(gameResponseModel);
//
//        //act
//        ResponseEntity<GameResponseModel> GameResponseEntity =
//                gameController.updateGame(gameRequestModel);
//
//        //assert
//        assertNotNull(GameResponseEntity);
//        assertEquals(GameResponseEntity.getStatusCode(), HttpStatus.OK);
//        assertNotNull(GameResponseEntity.getBody());
//        assertEquals(GameResponseEntity.getBody(), gameResponseModel);
//        verify(gameService, times(1).description("Betty"));
//    }
//
//    private GameRequestModel buildGameRequestModel(String betty) {
//        GameRequestModel gameRequestModel = new GameRequestModel();
//        gameRequestModel.setTitle(betty);
//        return gameRequestModel;
//    }
//    private GameResponseModel buildGameResponseModel(String betty) {
//        GameResponseModel gameResponseModel = new GameResponseModel();
//        gameResponseModel.setTitle(betty);
//        return gameResponseModel;
//    }



