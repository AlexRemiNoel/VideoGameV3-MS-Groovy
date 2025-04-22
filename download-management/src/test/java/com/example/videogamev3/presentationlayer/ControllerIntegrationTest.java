package com.example.videogamev3.presentationlayer;

import com.example.videogamev3.DownloadManagement.BusinessLogic.DownloadService;
import com.example.videogamev3.DownloadManagement.DataAccess.DownloadStatus;
import com.example.videogamev3.DownloadManagement.Presentation.DownloadController;
import com.example.videogamev3.DownloadManagement.Presentation.DownloadRequestModel;
import com.example.videogamev3.DownloadManagement.Presentation.DownloadResponseModel;
import com.example.videogamev3.DownloadManagement.utils.exceptions.DownloadNotFoundException;
import com.example.videogamev3.DownloadManagement.utils.exceptions.DuplicateDownloadIDException;
import com.example.videogamev3.DownloadManagement.utils.exceptions.InvalidDownloadDataException;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest; // Or @WebMvcTest for servlet stack
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient; // Or MockMvc for servlet stack

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.*;
import static org.springframework.web.reactive.function.BodyInserters.fromValue; // For WebTestClient

@WebFluxTest(controllers = DownloadController.class) // Load only the Controller and related web config
class ControllerIntegrationTest {

    @Autowired
    private WebTestClient webTestClient; // Inject WebTestClient

    @MockitoBean // Create a Mockito mock for the DownloadService and add it to the context
    private DownloadService downloadService;

    private DownloadResponseModel response1;
    private DownloadResponseModel response2;
    private String id1;
    private String id2;

    @BeforeEach
    void setUp() {
        id1 = UUID.randomUUID().toString();
        response1 = new DownloadResponseModel(id1, "http://example.com/file1.zip", DownloadStatus.PENDING.toString());

        id2 = UUID.randomUUID().toString();
        response2 = new DownloadResponseModel(id2, "http://example.com/file2.iso", DownloadStatus.DOWNLOADING.toString());
    }

    @Test
    @DisplayName("POST /api/v1/downloads - Success")
    void whenPostDownload_withValidRequest_thenReturnCreated() {
        // Arrange
        DownloadRequestModel requestModel = new DownloadRequestModel("http://new.com/file.dat");
        DownloadResponseModel createdResponse = new DownloadResponseModel(UUID.randomUUID().toString(), requestModel.getSourceUrl(), DownloadStatus.PENDING.toString());

        given(downloadService.createDownload(any(DownloadRequestModel.class))).willReturn(createdResponse);

        // Act & Assert
        webTestClient.post().uri("/api/v1/downloads")
                .contentType(MediaType.APPLICATION_JSON)
                .body(fromValue(requestModel))
                .exchange() // Send the request
                .expectStatus().isCreated() // Verify HTTP status
                .expectHeader().contentType(MediaType.APPLICATION_JSON) // Verify content type
                .expectBody(DownloadResponseModel.class) // Verify response body type
                .isEqualTo(createdResponse); // Verify response body content

        then(downloadService).should(times(1)).createDownload(any(DownloadRequestModel.class));
    }

    @Test
    @DisplayName("GET /api/v1/downloads/{id} - Found")
    void whenGetDownloadById_andExists_thenReturnOk() {
        // Arrange
        given(downloadService.getDownload(id1)).willReturn(response1);

        // Act & Assert
        webTestClient.get().uri("/api/v1/downloads/{id}", id1)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(DownloadResponseModel.class)
                .isEqualTo(response1);

        then(downloadService).should(times(1)).getDownload(id1);
    }

    @Test
    @DisplayName("GET /api/v1/downloads/{id} - Not Found")
    void whenGetDownloadById_andNotExists_thenReturnNotFound() { // Or appropriate error based on global exception handler
        // Arrange
        String nonExistentId = UUID.randomUUID().toString();
        given(downloadService.getDownload(nonExistentId)).willThrow(new EntityNotFoundException("Download not found")); // Mock service throwing exception

        // Act & Assert
        webTestClient.get().uri("/api/v1/downloads/{id}", nonExistentId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                // Expect 404 Not Found IF you have a @ControllerAdvice handling EntityNotFoundException
                // Otherwise, it might be 500 Internal Server Error by default
                .expectStatus().isNotFound(); // Adjust if your exception handler returns a different status

        then(downloadService).should(times(1)).getDownload(nonExistentId);
    }


    @Test
    @DisplayName("GET /api/v1/downloads - Success")
    void whenGetAllDownloads_thenReturnOkWithList() {
        // Arrange
        List<DownloadResponseModel> downloadList = Arrays.asList(response1, response2);
        given(downloadService.getAllDownloads()).willReturn(downloadList);

        // Act & Assert
        webTestClient.get().uri("/api/v1/downloads")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBodyList(DownloadResponseModel.class) // Expect a list
                .isEqualTo(downloadList);

        then(downloadService).should(times(1)).getAllDownloads();
    }

    @Test
    @DisplayName("POST /api/v1/downloads/{id}/start - Success")
    void whenStartDownload_thenReturnOk() {
        // Arrange
        DownloadResponseModel startedResponse = new DownloadResponseModel(id1, response1.getSourceUrl(), DownloadStatus.DOWNLOADING.toString());
        given(downloadService.startDownload(id1)).willReturn(startedResponse);

        // Act & Assert
        webTestClient.post().uri("/api/v1/downloads/{id}/start", id1)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(DownloadResponseModel.class)
                .isEqualTo(startedResponse);

        then(downloadService).should(times(1)).startDownload(id1);
    }

    @Test
    @DisplayName("POST /api/v1/downloads/{id}/pause - Success")
    void whenPauseDownload_thenReturnOk() {
        // Arrange
        DownloadResponseModel pausedResponse = new DownloadResponseModel(id2, response2.getSourceUrl(), DownloadStatus.PAUSED.toString());
        given(downloadService.pauseDownload(id2)).willReturn(pausedResponse);

        // Act & Assert
        webTestClient.post().uri("/api/v1/downloads/{id}/pause", id2)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(DownloadResponseModel.class)
                .isEqualTo(pausedResponse);

        then(downloadService).should(times(1)).pauseDownload(id2);
    }

    @Test
    @DisplayName("POST /api/v1/downloads/{id}/resume - Success")
    void whenResumeDownload_thenReturnOk() {
        // Arrange
        // Assuming response1 was paused before
        DownloadResponseModel resumedResponse = new DownloadResponseModel(id1, response1.getSourceUrl(), DownloadStatus.DOWNLOADING.toString());
        given(downloadService.resumeDownload(id1)).willReturn(resumedResponse);

        // Act & Assert
        webTestClient.post().uri("/api/v1/downloads/{id}/resume", id1)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(DownloadResponseModel.class)
                .isEqualTo(resumedResponse);

        then(downloadService).should(times(1)).resumeDownload(id1);
    }

    @Test
    @DisplayName("POST /api/v1/downloads/{id}/cancel - Success")
    void whenCancelDownload_thenReturnOk() {
        // Arrange
        DownloadResponseModel cancelledResponse = new DownloadResponseModel(id1, response1.getSourceUrl(), DownloadStatus.CANCELLED.toString());
        given(downloadService.cancelDownload(id1)).willReturn(cancelledResponse);

        // Act & Assert
        webTestClient.post().uri("/api/v1/downloads/{id}/cancel", id1)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(DownloadResponseModel.class)
                .isEqualTo(cancelledResponse);

        then(downloadService).should(times(1)).cancelDownload(id1);
    }

    @Test
    @DisplayName("DELETE /api/v1/downloads/{id} - Success")
    void whenDeleteDownload_thenReturnNoContent() {
        // Arrange
        willDoNothing().given(downloadService).deleteDownload(id1); // Mock void method

        // Act & Assert
        webTestClient.delete().uri("/api/v1/downloads/{id}", id1)
                .exchange()
                .expectStatus().isNoContent() // Verify 204 No Content
                .expectBody().isEmpty(); // Verify empty body

        then(downloadService).should(times(1)).deleteDownload(id1);
    }

    @Test
    @DisplayName("DELETE /api/v1/downloads/{id} - Not Found")
    void whenDeleteDownload_andNotExists_thenReturnNotFound() { // Or appropriate error
        // Arrange
        String nonExistentId = UUID.randomUUID().toString();
        willThrow(new EntityNotFoundException("Cannot delete")).given(downloadService).deleteDownload(nonExistentId); // Mock void method throwing exception

        // Act & Assert
        webTestClient.delete().uri("/api/v1/downloads/{id}", nonExistentId)
                .exchange()
                .expectStatus().isNotFound(); // Adjust if your exception handler returns a different status

        then(downloadService).should(times(1)).deleteDownload(nonExistentId);
    }

    @Test
    @DisplayName("POST /api/v1/downloads - Duplicate ID Exception")
    void whenPostDownload_withDuplicateID_thenThrowsDuplicateDownloadID_thenReturnConflict() {
        // Arrange
        DownloadRequestModel requestModel = new DownloadRequestModel("http://colliding.com/file.zip");
        String expectedErrorMessage = "Duplicate Download ID: some-conflicting-id";
        given(downloadService.createDownload(any(DownloadRequestModel.class)))
                .willThrow(new DuplicateDownloadIDException(expectedErrorMessage));

        // Act & Assert
        webTestClient.post().uri("/api/v1/downloads")
                .contentType(MediaType.APPLICATION_JSON)
                .body(fromValue(requestModel))
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.CONFLICT) // Expect 409 Conflict
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                .expectBody(Map.class)
                .value(map -> org.assertj.core.api.Assertions.assertThat(map.get("message")).isEqualTo(expectedErrorMessage));

        then(downloadService).should(times(1)).createDownload(any(DownloadRequestModel.class));
    }

    @Test
    @DisplayName("POST /api/v1/downloads - Invalid Data Exception")
    void whenPostDownload_withInvalidData_thenThrowsInvalidData_thenReturnBadRequest() {
        // Arrange
        DownloadRequestModel invalidRequestModel = new DownloadRequestModel(""); // Example invalid data (empty URL)
        String expectedErrorMessage = "Source URL cannot be empty.";
        given(downloadService.createDownload(any(DownloadRequestModel.class)))
                .willThrow(new InvalidDownloadDataException(expectedErrorMessage));

        // Act & Assert
        webTestClient.post().uri("/api/v1/downloads")
                .contentType(MediaType.APPLICATION_JSON)
                .body(fromValue(invalidRequestModel))
                .exchange()
                .expectStatus().isBadRequest() // Expect 400 Bad Request
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                .expectBody(Map.class)
                .value(map -> org.assertj.core.api.Assertions.assertThat(map.get("message")).isEqualTo(expectedErrorMessage));

        then(downloadService).should(times(1)).createDownload(any(DownloadRequestModel.class));
    }
    @Test
    @DisplayName("PUT /api/v1/downloads/{id} - Invalid Data Exception")
    void whenPutDownload_withInvalidData_thenThrowsInvalidData_thenReturnBadRequest() {
        // Arrange
        String existingId = id1;
        DownloadRequestModel invalidRequestModel = new DownloadRequestModel(null); // Example invalid data
        String expectedErrorMessage = "Source URL cannot be null.";
        given(downloadService.updateDownload(eq(existingId), any(DownloadRequestModel.class)))
                .willThrow(new InvalidDownloadDataException(expectedErrorMessage));

        // Act & Assert
        webTestClient.put().uri("/api/v1/downloads/{id}", existingId)
                .contentType(MediaType.APPLICATION_JSON)
                .body(fromValue(invalidRequestModel))
                .exchange()
                .expectStatus().isBadRequest() // Expect 400 Bad Request
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                .expectBody(Map.class)
                .value(map -> org.assertj.core.api.Assertions.assertThat(map.get("message")).isEqualTo(expectedErrorMessage));

        then(downloadService).should(times(1)).updateDownload(eq(existingId), any(DownloadRequestModel.class));
    }

    // --- Test for DownloadNotFoundException on PUT ---
    @Test
    @DisplayName("PUT /api/v1/downloads/{id} - Custom Not Found Exception")
    void whenPutDownload_andNotExists_thenThrowsDownloadNotFound_thenReturnNotFound() {
        // Arrange
        String nonExistentId = UUID.randomUUID().toString();
        DownloadRequestModel validRequestModel = new DownloadRequestModel("http://valid.url/file.dat");
        String expectedErrorMessage = "Download with ID '" + nonExistentId + "' not found.";
        given(downloadService.updateDownload(eq(nonExistentId), any(DownloadRequestModel.class)))
                .willThrow(new DownloadNotFoundException(expectedErrorMessage)); // Service throws when trying to find the download to update

        // Act & Assert
        webTestClient.put().uri("/api/v1/downloads/{id}", nonExistentId)
                .contentType(MediaType.APPLICATION_JSON)
                .body(fromValue(validRequestModel))
                .exchange()
                .expectStatus().isNotFound() // Expect 404 Not Found
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                .expectBody(Map.class)
                .value(map -> org.assertj.core.api.Assertions.assertThat(map.get("message")).isEqualTo(expectedErrorMessage));

        then(downloadService).should(times(1)).updateDownload(eq(nonExistentId), any(DownloadRequestModel.class));
    }

    @Test
    @DisplayName("DELETE /api/v1/downloads/{id} - Custom Not Found Exception")
    void whenDeleteDownload_andNotExists_thenThrowsDownloadNotFound_thenReturnNotFound() {
        // Arrange
        String nonExistentId = UUID.randomUUID().toString();
        String expectedErrorMessage = "Download with ID '" + nonExistentId + "' not found.";
        // Use willThrow for void methods
        willThrow(new DownloadNotFoundException(expectedErrorMessage)).given(downloadService).deleteDownload(nonExistentId);

        // Act & Assert
        webTestClient.delete().uri("/api/v1/downloads/{id}", nonExistentId)
                .exchange()
                .expectStatus().isNotFound() // Expect 404 Not Found
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                .expectBody(Map.class)
                .value(map -> org.assertj.core.api.Assertions.assertThat(map.get("message")).isEqualTo(expectedErrorMessage));


        then(downloadService).should(times(1)).deleteDownload(nonExistentId);
    }
}