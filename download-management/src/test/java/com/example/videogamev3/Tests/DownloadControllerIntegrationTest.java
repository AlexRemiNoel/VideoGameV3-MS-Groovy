package com.example.videogamev3.Tests;

import com.example.videogamev3.DownloadManagement.DataAccess.Download;
import com.example.videogamev3.DownloadManagement.DataAccess.DownloadId;
import com.example.videogamev3.DownloadManagement.DataAccess.DownloadRepository;
import com.example.videogamev3.DownloadManagement.DataAccess.DownloadStatus;
import com.example.videogamev3.DownloadManagement.Presentation.DownloadRequestModel;
import com.example.videogamev3.DownloadManagement.Presentation.DownloadResponseModel;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient; // For reactive test client

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
// @ActiveProfiles("test") // If you have a specific test profile for DB, etc.
class DownloadControllerIntegrationTest {

    @Autowired
    private WebTestClient webTestClient; // Spring Boot auto-configures this for testing

    @Autowired
    private DownloadRepository downloadRepository;

    private final String BASE_URI_DOWNLOADS = "/api/v1/downloads";
    private Download testDownload1, testDownload2;
    private String userId1 = "user-" + UUID.randomUUID();
    private String userId2 = "user-" + UUID.randomUUID();


    @BeforeEach
    void setUp() {
        downloadRepository.deleteAll(); // Clean before each test

        testDownload1 = new Download(new DownloadId(), "http://files.com/f1.iso", DownloadStatus.PENDING, userId1);
        testDownload2 = new Download(new DownloadId(), "http://files.com/f2.iso", DownloadStatus.DOWNLOADING, userId2);
        downloadRepository.saveAll(List.of(testDownload1, testDownload2));
    }

    @AfterEach
    void tearDown() {
        downloadRepository.deleteAll();
    }

    private DownloadRequestModel buildDownloadRequest(String url, String userId) {
        return new DownloadRequestModel(url, userId);
    }

    // --- POST /api/v1/downloads ---
    @Test
    void whenCreateDownload_thenReturnCreatedDownload() {
        DownloadRequestModel requestModel = buildDownloadRequest("http://newdownload.com/file.exe", "new-user-123");
        long initialCount = downloadRepository.count();

        webTestClient.post().uri(BASE_URI_DOWNLOADS)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestModel)
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(DownloadResponseModel.class)
                .value((response) -> {
                    assertNotNull(response.getId());
                    assertEquals(requestModel.getSourceUrl(), response.getSourceUrl());
                    assertEquals(requestModel.getUserId(), response.getUserId());
                    assertEquals(DownloadStatus.PENDING.toString(), response.getStatus());
                });
        
        assertEquals(initialCount + 1, downloadRepository.count());
    }

    // --- GET /api/v1/downloads/{id} ---
    @Test
    void whenGetDownloadById_andExists_thenReturnDownload() {
        String existingId = testDownload1.getId().getUuid();

        webTestClient.get().uri(BASE_URI_DOWNLOADS + "/" + existingId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(DownloadResponseModel.class)
                .value((response) -> {
                    assertEquals(existingId, response.getId());
                    assertEquals(testDownload1.getSourceUrl(), response.getSourceUrl());
                    assertEquals(testDownload1.getUserId(), response.getUserId());
                });
    }

    @Test
    void whenGetDownloadById_andNotExists_thenReturnNotFound() {
        String nonExistentId = UUID.randomUUID().toString();
        webTestClient.get().uri(BASE_URI_DOWNLOADS + "/" + nonExistentId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.httpStatus").isEqualTo("NOT_FOUND")
                .jsonPath("$.message").isEqualTo("Download not found with ID: " + nonExistentId);
    }

    // --- PUT /api/v1/downloads/{id} ---
    @Test
    void whenUpdateDownload_andExists_thenReturnUpdatedDownload() {
        String existingId = testDownload1.getId().getUuid();
        DownloadRequestModel updateRequest = buildDownloadRequest("http://updated.com/newfile.zip", testDownload1.getUserId());

        webTestClient.put().uri(BASE_URI_DOWNLOADS + "/" + existingId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(updateRequest)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(DownloadResponseModel.class)
                .value((response) -> {
                    assertEquals(existingId, response.getId());
                    assertEquals(updateRequest.getSourceUrl(), response.getSourceUrl());
                    // Assuming status is not changed by this update operation
                    assertEquals(testDownload1.getDownloadStatus().toString(), response.getStatus());
                });

        Download updatedInDb = downloadRepository.findDownloadById_Uuid(existingId);
        assertNotNull(updatedInDb);
        assertEquals(updateRequest.getSourceUrl(), updatedInDb.getSourceUrl());
    }
    
    @Test
    void whenUpdateDownload_andNotExists_thenReturnNotFound() {
        String nonExistentId = UUID.randomUUID().toString();
        DownloadRequestModel updateRequest = buildDownloadRequest("http://updated.com/newfile.zip", "some-user");

        webTestClient.put().uri(BASE_URI_DOWNLOADS + "/" + nonExistentId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(updateRequest)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.httpStatus").isEqualTo("NOT_FOUND")
                .jsonPath("$.message").isEqualTo("Download not found with ID: " + nonExistentId);
    }


    // --- GET /api/v1/downloads ---
    @Test
    void whenGetAllDownloads_thenReturnListOfDownloads() {
        webTestClient.get().uri(BASE_URI_DOWNLOADS)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBodyList(DownloadResponseModel.class)
                .hasSize(2) // Based on @BeforeEach setup
                .value(list -> {
                    assertTrue(list.stream().anyMatch(d -> d.getId().equals(testDownload1.getId().getUuid())));
                    assertTrue(list.stream().anyMatch(d -> d.getId().equals(testDownload2.getId().getUuid())));
                });
    }
    
    @Test
    void whenGetAllDownloads_andNoDownloads_thenReturnEmptyList() {
        downloadRepository.deleteAll(); // Ensure no downloads

        webTestClient.get().uri(BASE_URI_DOWNLOADS)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBodyList(DownloadResponseModel.class)
                .hasSize(0);
    }

    // --- POST /api/v1/downloads/{id}/start ---
    @Test
    void whenStartDownload_andIsPending_thenReturnOkWithDownloadingStatus() {
        String pendingId = testDownload1.getId().getUuid(); // testDownload1 is PENDING

        webTestClient.post().uri(BASE_URI_DOWNLOADS + "/" + pendingId + "/start")
                .exchange()
                .expectStatus().isOk()
                .expectBody(DownloadResponseModel.class)
                .value(response -> {
                    assertEquals(pendingId, response.getId());
                    assertEquals(DownloadStatus.DOWNLOADING.toString(), response.getStatus());
                });
        
        Download inDb = downloadRepository.findDownloadById_Uuid(pendingId);
        assertEquals(DownloadStatus.DOWNLOADING, inDb.getDownloadStatus());
    }
    
    @Test
    void whenStartDownload_andNotExists_thenReturnNotFound() {
        String nonExistentId = UUID.randomUUID().toString();
        webTestClient.post().uri(BASE_URI_DOWNLOADS + "/" + nonExistentId + "/start")
                .exchange()
                .expectStatus().isNotFound();
    }

    // --- POST /api/v1/downloads/{id}/pause ---
    @Test
    void whenPauseDownload_andIsDownloading_thenReturnOkWithPausedStatus() {
        String downloadingId = testDownload2.getId().getUuid(); // testDownload2 is DOWNLOADING

        webTestClient.post().uri(BASE_URI_DOWNLOADS + "/" + downloadingId + "/pause")
                .exchange()
                .expectStatus().isOk()
                .expectBody(DownloadResponseModel.class)
                .value(response -> {
                    assertEquals(downloadingId, response.getId());
                    assertEquals(DownloadStatus.PAUSED.toString(), response.getStatus());
                });
        
        Download inDb = downloadRepository.findDownloadById_Uuid(downloadingId);
        assertEquals(DownloadStatus.PAUSED, inDb.getDownloadStatus());
    }

    // --- POST /api/v1/downloads/{id}/resume ---
    @Test
    void whenResumeDownload_andIsPaused_thenReturnOkWithDownloadingStatus() {
        // First, set a download to PAUSED
        String downloadId = testDownload1.getId().getUuid();
        testDownload1.setDownloadStatus(DownloadStatus.PAUSED);
        downloadRepository.save(testDownload1);

        webTestClient.post().uri(BASE_URI_DOWNLOADS + "/" + downloadId + "/resume")
                .exchange()
                .expectStatus().isOk()
                .expectBody(DownloadResponseModel.class)
                .value(response -> {
                    assertEquals(downloadId, response.getId());
                    assertEquals(DownloadStatus.DOWNLOADING.toString(), response.getStatus());
                });
        
        Download inDb = downloadRepository.findDownloadById_Uuid(downloadId);
        assertEquals(DownloadStatus.DOWNLOADING, inDb.getDownloadStatus());
    }

    // --- POST /api/v1/downloads/{id}/cancel ---
    @Test
    void whenCancelDownload_andIsPending_thenReturnOkWithCancelledStatus() {
        String pendingId = testDownload1.getId().getUuid(); // testDownload1 is PENDING

        webTestClient.post().uri(BASE_URI_DOWNLOADS + "/" + pendingId + "/cancel")
                .exchange()
                .expectStatus().isOk()
                .expectBody(DownloadResponseModel.class)
                .value(response -> {
                    assertEquals(pendingId, response.getId());
                    assertEquals(DownloadStatus.CANCELLED.toString(), response.getStatus());
                });
        
        Download inDb = downloadRepository.findDownloadById_Uuid(pendingId);
        assertEquals(DownloadStatus.CANCELLED, inDb.getDownloadStatus());
    }

    // --- DELETE /api/v1/downloads/{id} ---
    @Test
    void whenDeleteDownload_andExists_thenReturnNoContent() {
        String existingId = testDownload1.getId().getUuid();
        long initialCount = downloadRepository.count();

        webTestClient.delete().uri(BASE_URI_DOWNLOADS + "/" + existingId)
                .exchange()
                .expectStatus().isNoContent();
        
        assertFalse(downloadRepository.existsDownloadById_Uuid(existingId));
        assertEquals(initialCount - 1, downloadRepository.count());
    }

    @Test
    void whenDeleteDownload_andNotExists_thenReturnNotFound() {
        String nonExistentId = UUID.randomUUID().toString();
        webTestClient.delete().uri(BASE_URI_DOWNLOADS + "/" + nonExistentId)
                .exchange()
                .expectStatus().isNotFound();
    }
    
    // --- GET /api/v1/downloads/user/{userId} ---
    @Test
    void whenGetDownloadsByUserId_andUserHasDownloads_thenReturnList() {
        // Add specific downloads for another user to test filtering
        Download user1Download3 = new Download(new DownloadId(), "http://files.com/user1_f3.iso", DownloadStatus.COMPLETED, userId1);
        downloadRepository.save(user1Download3);
        
        webTestClient.get().uri(BASE_URI_DOWNLOADS + "/user/" + userId1)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(DownloadResponseModel.class)
                .hasSize(2) // testDownload1 and user1Download3 belong to userId1
                .value(list -> {
                    assertTrue(list.stream().allMatch(d -> d.getUserId().equals(userId1)));
                    assertTrue(list.stream().anyMatch(d -> d.getId().equals(testDownload1.getId().getUuid())));
                    assertTrue(list.stream().anyMatch(d -> d.getId().equals(user1Download3.getId().getUuid())));
                });
    }

    @Test
    void whenGetDownloadsByUserId_andUserHasNoDownloads_thenReturnEmptyList() {
         String nonExistentUserId = "user-no-downloads";
         webTestClient.get().uri(BASE_URI_DOWNLOADS + "/user/" + nonExistentUserId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(DownloadResponseModel.class)
                .hasSize(0);
    }
}