package com.example.videogamev3.presentationlayer;

import com.example.videogamev3.DownloadManagement.DataAccess.Download;
import com.example.videogamev3.DownloadManagement.DataAccess.DownloadId;
import com.example.videogamev3.DownloadManagement.DataAccess.DownloadRepository;
import com.example.videogamev3.DownloadManagement.DataAccess.DownloadStatus;
import com.example.videogamev3.DownloadManagement.Presentation.DownloadRequestModel;
import com.example.videogamev3.DownloadManagement.Presentation.DownloadResponseModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
properties = {"spring.datasource.url=jdbc:h2:mem:videogame-db"})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@ActiveProfiles("h2")
public class CustomerControllerIntegrationTest {
    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private DownloadRepository downloadRepository;

    // Constants
    private final String BASE_URI_DOWNLOADS = "/api/v1/downloads";
    private final String NOT_FOUND_DOWNLOAD_ID = UUID.randomUUID().toString();
    private final String SAMPLE_SOURCE_URL = "http://example.com/file.zip";

    private Download existingDownload;

    @BeforeEach
    void setUp() {
        downloadRepository.deleteAll();

        existingDownload = new Download(
                new DownloadId(UUID.randomUUID().toString()),
                "http://example.com/existing_file.iso",
                DownloadStatus.PENDING
        );
        downloadRepository.save(existingDownload);
    }
    private DownloadRequestModel buildDownloadRequestModel(String sourceUrl) {
        return new DownloadRequestModel(sourceUrl);
    }
    @Test
    @DisplayName("POST Download - Success")
    void whenValidDownloadRequest_thenCreateDownload() {
        // Arrange
        long initialCount = downloadRepository.count();
        DownloadRequestModel requestModel = buildDownloadRequestModel(SAMPLE_SOURCE_URL);

        // Act & Assert
        webTestClient.post()
                .uri(BASE_URI_DOWNLOADS)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestModel)
                .exchange()
                .expectStatus().isCreated() // Expect 201 Created
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(DownloadResponseModel.class)
                .value((response) -> {
                    assertNotNull(response);
                    assertNotNull(response.getId());
                    assertEquals(SAMPLE_SOURCE_URL, response.getSourceUrl());
                    assertEquals(DownloadStatus.PENDING.toString(), response.getStatus()); // Initial status
                });
        assertEquals(initialCount + 1, downloadRepository.count());
    }
//    @Test
//    @DisplayName("GET Download by ID - Success")
//    void whenGetDownloadByValidId_thenReturnDownload() {
//        // Arrange
//        String validId = existingDownload.getId().getUuid();
//        String expectedSourceUrl = existingDownload.getSourceUrl();
//
//        // Act & Assert
//        webTestClient.get()
//                .uri(BASE_URI_DOWNLOADS + "/" + validId)
//                .accept(MediaType.APPLICATION_JSON)
//                .exchange()
//                .expectStatus().isOk() // Expect 200 OK
//                .expectHeader().contentType(MediaType.APPLICATION_JSON)
//                .expectBody(DownloadResponseModel.class)
//                .value((response) -> {
//                    assertNotNull(response);
//                    assertEquals(validId, response.getId());
//                    assertEquals(expectedSourceUrl, response.getSourceUrl());
//                    assertEquals(existingDownload.getDownloadStatus().toString(), response.getStatus());
//                });
//    }
//
//    @Test
//    @DisplayName("GET Download by ID - Not Found")
//    void whenGetDownloadByNotFoundId_thenReturnNotFound() {
//        // Act & Assert
//        webTestClient.get()
//                .uri(BASE_URI_DOWNLOADS + "/" + NOT_FOUND_DOWNLOAD_ID)
//                .accept(MediaType.APPLICATION_JSON)
//                .exchange()
//                .expectStatus().isNotFound(); // Expect 404 Not Found
//        // Optionally check body if you have a consistent error response structure
//        // .expectBody()
//        // .jsonPath("$.message").isEqualTo("DownloadManager not found with id: " + NOT_FOUND_DOWNLOAD_ID);
//    }

    @Test
    @DisplayName("GET All Downloads - Success")
    void whenGetAllDownloads_thenReturnDownloadList() {
        // Arrange - setup() already creates one download
        // Optionally create more if needed for a more robust test
        Download secondDownload = new Download(
                new DownloadId(UUID.randomUUID().toString()),
                "http://example.com/another_file.zip",
                DownloadStatus.PENDING);
        downloadRepository.save(secondDownload);

        // Act & Assert
        webTestClient.get()
                .uri(BASE_URI_DOWNLOADS)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBodyList(DownloadResponseModel.class)
                .hasSize(2); // Because setup created one, and we added another
    }

//    @Test
//    @DisplayName("START Download - Success (from PENDING)")
//    void whenStartPendingDownload_thenStatusIsDownloading() {
//        // Arrange
//        String idToStart = existingDownload.getId().getUuid();
//        assertEquals(DownloadStatus.PENDING, downloadRepository.findById(idToStart).get().getDownloadStatus()); // Verify initial state
//
//        // Act & Assert
//        webTestClient.post()
//                .uri(BASE_URI_DOWNLOADS + "/" + idToStart + "/start")
//                .exchange()
//                .expectStatus().isOk()
//                .expectBody(DownloadResponseModel.class)
//                .value(response -> {
//                    assertEquals(idToStart, response.getId());
//                    assertEquals(DownloadStatus.DOWNLOADING.toString(), response.getStatus().toString());
//                });
//
//        // Assert database state
//        assertEquals(DownloadStatus.DOWNLOADING, downloadRepository.findById(idToStart).get().getDownloadStatus().toString());
//    }
//    @Test
//    @DisplayName("START Download - NoOp (already DOWNLOADING)")
//    void whenStartDownloadingDownload_thenStatusRemainsDownloading() {
//        String idToStart = existingDownload.getId().getUuid();
//        existingDownload.setDownloadStatus(DownloadStatus.DOWNLOADING);
//        downloadRepository.save(existingDownload);
//        assertEquals(DownloadStatus.DOWNLOADING, downloadRepository.findById(idToStart).get().getDownloadStatus()); // Verify initial state
//
//        // Act & Assert
//        webTestClient.post()
//                .uri(BASE_URI_DOWNLOADS + "/" + idToStart + "/start")
//                .exchange()
//                .expectStatus().isOk() // Service returns OK even if no state change
//                .expectBody(DownloadResponseModel.class)
//                .value(response -> {
//                    assertEquals(idToStart, response.getId());
//                    assertEquals(DownloadStatus.DOWNLOADING.toString(), response.getStatus()); // Status should remain DOWNLOADING
//                });
//    }
}