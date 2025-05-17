package com.champsoft.gamemanagement.Presentation;

import com.champsoft.gamemanagement.BusinessLogic.GameService;
import com.champsoft.gamemanagement.DataAccess.Game; // Consider if this is needed in a web layer test
import com.champsoft.gamemanagement.Presentation.DTOS.GameController; // Consider if this is needed with @SpringBootTest
import com.champsoft.gamemanagement.Presentation.DTOS.GameRequestModel;
import com.champsoft.gamemanagement.Presentation.DTOS.GameResponseModel;

import com.champsoft.gamemanagement.utils.GameAlreadyStartedException;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest; // Remove this
import org.springframework.boot.test.autoconfigure.web.client.AutoConfigureWebClient; // Not needed here, was a typo in thought process
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc; // Or this if using MockMvc

import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient; // Add this
import org.springframework.boot.test.context.SpringBootTest; // Use this
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient; // Keep this

// Removed imports for Mono and Flux

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

// import static org.hibernate.boot.jaxb.cfg.spi.JaxbCfgEventTypeEnum.fromValue; // Remove unused import
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.times;

// Use @SpringBootTest to load a full application context
@SpringBootTest // Use this annotation
@WebFluxTest(controllers = GameController.class)
@AutoConfigureWebTestClient // Add this annotation to configure WebTestClient for @SpringBootTest
// Remove @WebFluxTest(controllers = GameController.class)
public class ControllerIntegrationTest {

    @Autowired
    private WebTestClient webTestClient; // Injected by @AutoConfigureWebTestClient

    @MockitoBean // Create a Mockito mock for the GameService and add it to the context
    private GameService gameService; // Injected by @SpringBootTest and @MockitoBean

    private GameResponseModel response1;
    private GameResponseModel response2;
    private String id1;
    private String id2;
    private GameRequestModel post1; // Still needed if your start/update methods use it
    private GameRequestModel post2; // Still needed if your start/update methods use it

    @BeforeEach
    void setUp() {
        id1 = UUID.randomUUID().toString();
        response1 = new GameResponseModel();
        response1.setId(id1);
        response1.setTitle("Game1");
        response1.setGenre("Red");
        post1 = new GameRequestModel(); // Assuming these are used for specific test cases like start/update
        post1.setTitle("Game1");
        post1.setGenre("Red");

        id2 = UUID.randomUUID().toString();
        response2 = new GameResponseModel();
        response2.setId(id2);
        response2.setTitle("Game2");
        response2.setGenre("ACTION");
        post2 = new GameRequestModel(); // Assuming these are used for specific test cases like start/update
        post2.setTitle("Game1"); // This might be a typo, should it be Game2?
        post2.setGenre("Red"); // This might be a typo, should it be ACTION?
    }
    @Test
    @DisplayName("POST /api/v1/game - Success")
    void whenPostValidGame_thenReturnCreated() {
        // Arrange
        GameRequestModel requestModel = new GameRequestModel();
        requestModel.setTitle("FIFA 25");
        requestModel.setGenre("SPORT");

        GameResponseModel responseModel = new GameResponseModel();
        responseModel.setId(UUID.randomUUID().toString());
        responseModel.setTitle("FIFA 25");
        responseModel.setGenre("SPORT");

        given(gameService.createGame(any(GameRequestModel.class))).willReturn(responseModel);

        // Act & Assert
        webTestClient.post().uri("/api/v1/game")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestModel)
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(GameResponseModel.class)
                .isEqualTo(responseModel);

        then(gameService).should(times(1)).createGame(any(GameRequestModel.class));
    }
    @Test
    @DisplayName("POST /api/v1/game/{id}/start - properly throws GameAlreadyStartedException")
    void whenGameAlreadyStarted_thenCustomExceptionHandledProperly() {
        // Arrange
        String gameId = UUID.randomUUID().toString();
        String errorMessage = "Game has already started";

        // Mock the service to throw the custom exception
        willThrow(new GameAlreadyStartedException(errorMessage))
                .given(gameService);

        // Act & Assert: Confirm the controller returns 409 with expected message
        webTestClient.post().uri("/api/v1/game/{id}/start", gameId)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.CONFLICT)
                .expectHeader().contentTypeCompatibleWith(MediaType.TEXT_PLAIN)
                .expectBody(String.class).isEqualTo(errorMessage);

        // Verify interaction with service
        then(gameService).should(times(1));
    }


    @Test
    @DisplayName("POST /api/v1/game/{id}/start - GameAlreadyStartedException -> 409 Conflict")
    void whenStartAlreadyStartedGame_thenReturnConflict() {
        // Arrange
        String gameId = UUID.randomUUID().toString();
        willThrow(new GameAlreadyStartedException("Game has already started"));

        // Act & Assert
        webTestClient.post().uri("/api/v1/game/{id}/start", gameId)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.CONFLICT)
                .expectBody(String.class)
                .isEqualTo("Game has already started");

        // Verify
        then(gameService).should(times(1));
    }


    @Test
    @DisplayName("POST /api/v1/game - Success")
    void whenPostGame_withValidRequest_thenReturnCreated() {
        // Arrange
        GameRequestModel requestModel = new GameRequestModel();
        requestModel.setTitle("New Game");
        requestModel.setGenre("SPORT");

        GameResponseModel createdResponse = new GameResponseModel();
        createdResponse.setId(UUID.randomUUID().toString());
        createdResponse.setTitle(requestModel.getTitle());
        createdResponse.setGenre(requestModel.getGenre());

        // Stub the service method to return the created response (standard object)
        given(gameService.createGame(any(GameRequestModel.class))).willReturn(createdResponse);

        // Act & Assert
        webTestClient.post().uri("/api/v1/game")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestModel)
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(GameResponseModel.class)
                .isEqualTo(createdResponse);

        // Verify that the service method was called
        then(gameService).should(times(1)).createGame(any(GameRequestModel.class));
    }

    @Test
    @DisplayName("GET /api/v1/game/{id} - Found")
    void whenGetGameById_andExists_thenReturnOk() {
        // Arrange
        // Stub the service method to return the response (standard object)
        given(gameService.getGameById(id1)).willReturn(response1);

        // Act & Assert
        webTestClient.get().uri("/api/v1/game/{id}", id1)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(GameResponseModel.class)
                .isEqualTo(response1);

        // Verify that the service method was called
        then(gameService).should(times(1)).getGameById(id1);
    }

    @Test
    @DisplayName("GET /api/v1/game/{id} - Not Found")
    void whenGetGameById_andNotExists_thenReturnNotFound() {
        // Arrange
        String nonExistentId = UUID.randomUUID().toString();
        // Stub the service method to throw the exception
        given(gameService.getGameById(nonExistentId)).willThrow(new EntityNotFoundException("Game not found"));

        // Act & Assert
        webTestClient.get().uri("/api/v1/game/{id}", nonExistentId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound();

        // Verify that the service method was called
        then(gameService).should(times(1)).getGameById(nonExistentId);
    }


    @Test
    @DisplayName("GET /api/v1/game - Success")
    void whenGetAllGames_thenReturnOkWithList() {
        // Arrange
        List<GameResponseModel> gameList = Arrays.asList(response1, response2);
        // Stub the service method to return the list (standard List)
        given(gameService.getAllGames()).willReturn(gameList);

        // Act & Assert
        webTestClient.get().uri("/api/v1/game")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBodyList(GameResponseModel.class)
                .isEqualTo(gameList);

        // Verify that the service method was called
        then(gameService).should(times(1)).getAllGames();
    }

    // Assuming your startGame method in GameController calls a service method like gameService.startGame(id)
    // And assuming that service method returns the updated GameResponseModel
    @Test
    @DisplayName("POST /api/v1/game/{id}/start - Success")
    void whenStartGame_thenReturnOk() {
        // Arrange
        GameResponseModel startedResponse = new GameResponseModel();
        startedResponse.setId(id1);
        startedResponse.setTitle("Game1"); // Assuming start doesn't change title/genre for this test
        startedResponse.setGenre("Red");   // Assuming start doesn't change title/genre for this test
        // Assuming a service method like gameService.startGame(id) exists and returns the updated game
        given(gameService.createGame(post1)).willReturn(startedResponse); // Stubbing a hypothetical startGame service method

        // Act & Assert
        webTestClient.post().uri("/api/v1/game/{id}/start", id1)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(GameResponseModel.class)
                .isEqualTo(startedResponse);

        // Verify the correct service method was called
        then(gameService).should(times(1)).createGame(post1); // Verifying the hypothetical startGame method
    }


    // Rest of your tests (update, delete) seem correct in their logic, assuming
    // the service methods are named and behave as you've mocked them.
    // I'll include the update test here, assuming your controller's PUT endpoint
    // calls a service method like gameService.updateGame(id, requestModel)
    @Test
    @DisplayName("PUT /api/v1/game/{id} - Success")
    void whenUpdateGame_withValidRequest_thenReturnOk() {
        // Arrange
        GameRequestModel updateRequest = new GameRequestModel();
        updateRequest.setTitle("Updated Game Title");
        updateRequest.setGenre("STRATEGY");

        GameResponseModel updatedResponse = new GameResponseModel();
        updatedResponse.setId(id1);
        updatedResponse.setTitle(updateRequest.getTitle());
        updatedResponse.setGenre(updateRequest.getGenre());

        // Stub the update method to return the updated response (standard object)
        // Assuming a service method gameService.updateGame(id, requestModel)
//        given(gameService.updateGame(eq(id1), any(GameRequestModel.class))).willReturn(updatedResponse);

        // Act & Assert
        webTestClient.put().uri("/api/v1/game/{id}", id1)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(updateRequest)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(GameResponseModel.class)
                .isEqualTo(updatedResponse);

        // Verify the update method was called
        then(gameService).should(times(1)).updateGame(eq(id1), any(GameRequestModel.class));
    }


    @Test
    @DisplayName("DELETE /api/v1/game/{id} - Success")
    void whenDeleteGame_andExists_thenReturnNoContent() {
        // Arrange
        willDoNothing().given(gameService).deleteGame(id1);

        // Act & Assert
        webTestClient.delete().uri("/api/v1/game/{id}", id1)
                .exchange()
                .expectStatus().isNoContent();

        // Verify that the service method was called
        then(gameService).should(times(1)).deleteGame(id1);
    }


    @Test
    @DisplayName("DELETE /api/v1/game/{id} - Not Found")
    void whenDeleteGame_andNotExists_thenReturnNotFound() {
        // Arrange
        String nonExistentId = UUID.randomUUID().toString();
        willThrow(new EntityNotFoundException("Cannot delete")).given(gameService).deleteGame(nonExistentId);

        // Act & Assert
        webTestClient.delete().uri("/api/v1/game/{id}", nonExistentId)
                .exchange()
                .expectStatus().isNotFound();

        // Verify that the service method was called
        then(gameService).should(times(1)).deleteGame(nonExistentId);
    }

}