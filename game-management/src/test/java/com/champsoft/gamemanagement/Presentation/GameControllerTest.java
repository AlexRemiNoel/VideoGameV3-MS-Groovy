package com.champsoft.gamemanagement.Presentation;

import com.champsoft.gamemanagement.BusinessLogic.GameService;
import com.champsoft.gamemanagement.DataAccess.GameId;
import com.champsoft.gamemanagement.Presentation.DTOS.GameController;
import com.champsoft.gamemanagement.Presentation.DTOS.GameRequestModel;
import com.champsoft.gamemanagement.Presentation.DTOS.GameResponseModel;
import com.champsoft.gamemanagement.Presentation.DTOS.ReviewRequestModel;
import com.champsoft.gamemanagement.utils.exceptions.NotFoundException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.*;

import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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

    // Keep BASE_URL to match the controller's class-level mapping
    private static final String BASE_URL = "/api/v1/game";

    // ... (rest of your existing tests like whenNoGameExist_ThenReturnEmptyList, etc.) ...

    @Test
    public void whenGameExistsOnGet_ThenReturnOkAndGame() throws Exception {
        // Arrange
        GameResponseModel gameResponseModel = new GameResponseModel();
        gameResponseModel.setId(VALID_CUSTOMER_ID);
        gameResponseModel.setTitle("Test Game");
        when(gameService.getGameById(VALID_CUSTOMER_ID)).thenReturn(gameResponseModel);

        // Act & Assert
        // GET mapping is at /api/v1/game/{uuid}
        mockMvc.perform(get(BASE_URL + "/{uuid}", VALID_CUSTOMER_ID)) // Use BASE_URL + "/{uuid}"
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(VALID_CUSTOMER_ID)))
                .andExpect(jsonPath("$.title", is("Test Game")));

        verify(gameService, times(1)).getGameById(VALID_CUSTOMER_ID);
    }

    @Test
    void deleteGame_shouldReturnOk_whenGameExists() throws Exception {
        // Arrange
        String gameUuid = UUID.randomUUID().toString();
        GameResponseModel deletedGameModel = new GameResponseModel();
        deletedGameModel.setId(gameUuid);
        deletedGameModel.setTitle("Deleted Game");
        // Populate other fields of deletedGameModel as necessary for verification

        when(gameService.deleteGame(eq(gameUuid))).thenReturn(deletedGameModel);

        // Act & Assert
        // DELETE mapping is at /api/v1/game/{uuid}
        mockMvc.perform(delete(BASE_URL + "/{uuid}", gameUuid)) // Use BASE_URL + "/{uuid}"
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(objectMapper.writeValueAsString(deletedGameModel)));
    }

    @Test
    void deleteGame_shouldReturnNotFound_whenGameDoesNotExist() throws Exception {
        // Arrange
        String gameUuid = UUID.randomUUID().toString();

        when(gameService.deleteGame(eq(gameUuid))).thenReturn(null); // Or doThrow(new NotFoundException(...))

        // Act & Assert
        // DELETE mapping is at /api/v1/game/{uuid}
        mockMvc.perform(delete(BASE_URL + "/{uuid}", gameUuid)) // Use BASE_URL + "/{uuid}"
                .andExpect(status().isNotFound())
                .andExpect(content().string(""));
    }

    @Test
    void reviewGame_shouldReturnOk_whenGameExistsAndReviewAdded() throws Exception {
        // Arrange
        String gameUuid = UUID.randomUUID().toString();
        ReviewRequestModel reviewRequest = new ReviewRequestModel();
        reviewRequest.setComment("Great game!");
        reviewRequest.setRating(String.valueOf(5));
        // Populate other fields of reviewRequest as needed

        GameResponseModel reviewedGameModel = new GameResponseModel();
        reviewedGameModel.setId(gameUuid);
        reviewedGameModel.setTitle("Game with Review");
        // Populate other fields of reviewedGameModel including the added review

        when(gameService.addReview(any(ReviewRequestModel.class), eq(gameUuid)))
                .thenReturn(reviewedGameModel);

        // Act & Assert
        // POST mapping is at /api/v1/game/review/{uuid}
        mockMvc.perform(post(BASE_URL + "/review/{uuid}", gameUuid) // Use BASE_URL + "/review/{uuid}"
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reviewRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(objectMapper.writeValueAsString(reviewedGameModel)));
    }

    @Test
    void reviewGame_shouldReturnNotFound_whenGameDoesNotExist() throws Exception {
        // Arrange
        String gameUuid = UUID.randomUUID().toString();
        ReviewRequestModel reviewRequest = new ReviewRequestModel();
        reviewRequest.setComment("Great game!");
        reviewRequest.setRating(String.valueOf(5));
        // Populate other fields of reviewRequest as needed

        when(gameService.addReview(any(ReviewRequestModel.class), eq(gameUuid)))
                .thenReturn(null);

        // Act & Assert
        // POST mapping is at /api/v1/game/review/{uuid}
        mockMvc.perform(post(BASE_URL + "/review/{uuid}", gameUuid) // Use BASE_URL + "/review/{uuid}"
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reviewRequest)))
                .andExpect(status().isNotFound())
                .andExpect(content().string(""));
    }

    @Test
    void updateGame_shouldReturnOk_whenGameExistsAndUpdated() throws Exception {
        // Arrange
        String gameUuid = UUID.randomUUID().toString();
        GameRequestModel requestModel = new GameRequestModel();
        // Based on your controller, the ID might be expected in the RequestModel or the PathVariable
        // Since the controller uses @PathVariable String uuid, we'll include it in the path.
        // If your service ONLY uses the ID from the GameRequestModel, you might need to set it here.
        // Let's assume GameRequestModel needs the ID for the service call:
//        requestModel.setId(gameUuid); // Add this line if GameRequestModel has an ID field

        requestModel.setTitle("Updated Title");
        requestModel.setPrice(69.99);
        // Populate other fields of requestModel as needed for the update

        GameResponseModel updatedGameModel = new GameResponseModel();
        updatedGameModel.setId(gameUuid); // Assuming the response model includes the ID
        updatedGameModel.setTitle("Updated Title");
        updatedGameModel.setPrice(69.99);
        // Populate other fields of updatedGameModel to reflect the expected updated state

        // Assuming the updateGame service method receives the request model and returns the updated response model
        when(gameService.updateGame(any(GameRequestModel.class))).thenReturn(updatedGameModel);

        // Act & Assert
        // Corrected PUT mapping to include the UUID in the path: /api/v1/game/{uuid}
        mockMvc.perform(put(BASE_URL + "/{uuid}", gameUuid) // <-- FIX: Use BASE_URL + "/{uuid}"
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestModel)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(objectMapper.writeValueAsString(updatedGameModel)));
    }

    @Test
    void updateGame_shouldReturnNotFound_whenGameDoesNotExist() throws Exception {
        // Arrange
        String gameUuid = UUID.randomUUID().toString();
        GameRequestModel requestModel = new GameRequestModel();
        // Assuming GameRequestModel needs the ID for the service call:
//        requestModel.setId(gameUuid); // Add this line if GameRequestModel has an ID field

        requestModel.setTitle("Attempt to Update Non-existent");
        requestModel.setPrice(100.00);
        // Populate other fields of requestModel as needed

        // Assuming the updateGame service method returns null when the game doesn't exist.
        when(gameService.updateGame(any(GameRequestModel.class))).thenReturn(null);

        // Act & Assert
        // Corrected PUT mapping to include the UUID in the path: /api/v1/game/{uuid}
        mockMvc.perform(put(BASE_URL + "/{uuid}", gameUuid) // <-- FIX: Use BASE_URL + "/{uuid}"
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestModel)))
                .andExpect(status().isNotFound()) // Expecting 404 Not Found (as per your controller's logic)
                .andExpect(content().string("")); // Expect an empty response body for 404
    }

    // ... (rest of your test class) ...
}