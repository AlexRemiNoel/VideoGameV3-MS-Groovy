package com.example.apigatewayservice.presentationlayer.download;

import com.example.apigatewayservice.businesslogiclayer.download.DownloadService;
import com.example.apigatewayservice.exception.HttpErrorInfo;
import com.example.apigatewayservice.exception.InvalidInputException;
import com.example.apigatewayservice.exception.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
class DownloadControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private DownloadService downloadService;

    private final String BASE_URI_DOWNLOADS = "/api/v1/downloads";
    private final String VALID_DOWNLOAD_ID = UUID.randomUUID().toString();
    private final String NOT_FOUND_DOWNLOAD_ID = UUID.randomUUID().toString();

    private DownloadResponseModel sampleDownloadResponse;
    private DownloadRequestModel sampleDownloadRequest;

    @BeforeEach
    void setUp() {
        sampleDownloadResponse = DownloadResponseModel.builder()
                .id(VALID_DOWNLOAD_ID)
                .sourceUrl("http://example.com/somefile.zip")
                .status("DOWNLOADING")
                .build();

        sampleDownloadRequest = DownloadRequestModel.builder()
                .sourceUrl("http://example.com/somefile.zip")
                .build();
    }

    // --- POST Create Download ---
    @Test
    void createDownload_whenValidRequest_thenReturnCreated() {
        when(downloadService.createDownload(any(DownloadRequestModel.class))).thenReturn(sampleDownloadResponse);

        webTestClient.post().uri(BASE_URI_DOWNLOADS)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(sampleDownloadRequest)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(DownloadResponseModel.class)
                .value(response -> {
                    assertNotNull(response);
                    assertEquals(sampleDownloadResponse.getId(), response.getId());
                });
        verify(downloadService, times(1)).createDownload(any(DownloadRequestModel.class));
    }
    
    @Test
    void createDownload_whenInvalidInput_thenReturnUnprocessableEntity() {
        String errorMessage = "Invalid source URL";
        when(downloadService.createDownload(any(DownloadRequestModel.class)))
            .thenThrow(new InvalidInputException(errorMessage));

        webTestClient.post().uri(BASE_URI_DOWNLOADS)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(sampleDownloadRequest)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY)
                .expectBody(HttpErrorInfo.class)
                .value(error -> {
                    assertNotNull(error);
                    assertTrue(error.getMessage().contains(errorMessage));
                });
    }

    // --- GET Download by ID ---
    @Test
    void getDownload_whenDownloadExists_thenReturnDownload() {
        when(downloadService.getDownload(VALID_DOWNLOAD_ID)).thenReturn(sampleDownloadResponse);

        webTestClient.get().uri(BASE_URI_DOWNLOADS + "/" + VALID_DOWNLOAD_ID)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(DownloadResponseModel.class)
                .value(response -> assertEquals(sampleDownloadResponse.getId(), response.getId()));
        verify(downloadService, times(1)).getDownload(VALID_DOWNLOAD_ID);
    }

    @Test
    void getDownload_whenDownloadNotFound_thenReturnNotFound() {
        String errorMessage = "Download not found: " + NOT_FOUND_DOWNLOAD_ID;
        when(downloadService.getDownload(NOT_FOUND_DOWNLOAD_ID)).thenThrow(new NotFoundException(errorMessage));

        webTestClient.get().uri(BASE_URI_DOWNLOADS + "/" + NOT_FOUND_DOWNLOAD_ID)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody(HttpErrorInfo.class)
                .value(error -> assertTrue(error.getMessage().contains(errorMessage)));
        verify(downloadService, times(1)).getDownload(NOT_FOUND_DOWNLOAD_ID);
    }

    // --- GET All Downloads ---
    @Test
    void getAllDownloads_thenReturnListOfDownloads() {
        List<DownloadResponseModel> expectedDownloads = List.of(sampleDownloadResponse);
        when(downloadService.getAllDownloads()).thenReturn(expectedDownloads);

        webTestClient.get().uri(BASE_URI_DOWNLOADS)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(DownloadResponseModel.class)
                .hasSize(1);
        verify(downloadService, times(1)).getAllDownloads();
    }
    
    @Test
    void getAllDownloads_whenNone_thenReturnEmptyList() {
        when(downloadService.getAllDownloads()).thenReturn(Collections.emptyList());

        webTestClient.get().uri(BASE_URI_DOWNLOADS)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(DownloadResponseModel.class)
                .hasSize(0);
    }

    // --- PUT Update Download ---
    @Test
    void updateDownload_whenValid_thenReturnOk() {
        when(downloadService.updateDownload(eq(VALID_DOWNLOAD_ID), any(DownloadRequestModel.class))).thenReturn(sampleDownloadResponse);
        webTestClient.put().uri(BASE_URI_DOWNLOADS + "/" + VALID_DOWNLOAD_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(sampleDownloadRequest)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(DownloadResponseModel.class)
                .value(response -> assertEquals(sampleDownloadResponse.getId(), response.getId()));
    }

    @Test
    void updateDownload_whenNotFound_thenReturnNotFound() {
        String errorMessage = "Cannot update, download not found.";
        when(downloadService.updateDownload(eq(NOT_FOUND_DOWNLOAD_ID), any(DownloadRequestModel.class)))
            .thenThrow(new NotFoundException(errorMessage));

        webTestClient.put().uri(BASE_URI_DOWNLOADS + "/" + NOT_FOUND_DOWNLOAD_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(sampleDownloadRequest)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody(HttpErrorInfo.class)
                .value(error -> assertTrue(error.getMessage().contains(errorMessage)));
    }

    // --- POST State Changes (start, pause, resume, cancel) ---
    private void testStateChangeEndpoint(String action, DownloadResponseModel expectedResponse, String downloadId) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        when(downloadService.getClass().getDeclaredMethod(action + "Download", String.class)
                .invoke(downloadService, downloadId)).thenReturn(expectedResponse);

        webTestClient.post().uri(BASE_URI_DOWNLOADS + "/" + downloadId + "/" + action)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(DownloadResponseModel.class)
                .value(response -> assertEquals(expectedResponse.getId(), response.getId()));
        // Verification will be done in individual test methods for clarity due to reflection
    }

    @Test
    void startDownload_whenExists_thenReturnOk() {
        when(downloadService.startDownload(VALID_DOWNLOAD_ID)).thenReturn(sampleDownloadResponse);
        webTestClient.post().uri(BASE_URI_DOWNLOADS + "/" + VALID_DOWNLOAD_ID + "/start")
                .accept(MediaType.APPLICATION_JSON).exchange().expectStatus().isOk();
        verify(downloadService).startDownload(VALID_DOWNLOAD_ID);
    }
    
    @Test
    void startDownload_whenNotFound_thenReturnNotFound() {
        String errorMessage = "Download not found for start: " + NOT_FOUND_DOWNLOAD_ID;
        when(downloadService.startDownload(NOT_FOUND_DOWNLOAD_ID)).thenThrow(new NotFoundException(errorMessage));
        webTestClient.post().uri(BASE_URI_DOWNLOADS + "/" + NOT_FOUND_DOWNLOAD_ID + "/start")
                .accept(MediaType.APPLICATION_JSON).exchange().expectStatus().isNotFound()
                .expectBody(HttpErrorInfo.class).value(error -> assertTrue(error.getMessage().contains(errorMessage)));
    }

    @Test
    void pauseDownload_whenExists_thenReturnOk() {
        when(downloadService.pauseDownload(VALID_DOWNLOAD_ID)).thenReturn(sampleDownloadResponse);
        webTestClient.post().uri(BASE_URI_DOWNLOADS + "/" + VALID_DOWNLOAD_ID + "/pause")
                .accept(MediaType.APPLICATION_JSON).exchange().expectStatus().isOk();
        verify(downloadService).pauseDownload(VALID_DOWNLOAD_ID);
    }

    @Test
    void resumeDownload_whenExists_thenReturnOk() {
        when(downloadService.resumeDownload(VALID_DOWNLOAD_ID)).thenReturn(sampleDownloadResponse);
        webTestClient.post().uri(BASE_URI_DOWNLOADS + "/" + VALID_DOWNLOAD_ID + "/resume")
                .accept(MediaType.APPLICATION_JSON).exchange().expectStatus().isOk();
        verify(downloadService).resumeDownload(VALID_DOWNLOAD_ID);
    }

    @Test
    void cancelDownload_whenExists_thenReturnOk() {
        when(downloadService.cancelDownload(VALID_DOWNLOAD_ID)).thenReturn(sampleDownloadResponse);
        webTestClient.post().uri(BASE_URI_DOWNLOADS + "/" + VALID_DOWNLOAD_ID + "/cancel")
                .accept(MediaType.APPLICATION_JSON).exchange().expectStatus().isOk();
        verify(downloadService).cancelDownload(VALID_DOWNLOAD_ID);
    }

    // --- DELETE Download ---
    @Test
    void deleteDownload_whenDownloadExists_thenReturnNoContent() {
        doNothing().when(downloadService).deleteDownload(VALID_DOWNLOAD_ID);
        webTestClient.delete().uri(BASE_URI_DOWNLOADS + "/" + VALID_DOWNLOAD_ID)
                .exchange()
                .expectStatus().isNoContent();
        verify(downloadService, times(1)).deleteDownload(VALID_DOWNLOAD_ID);
    }

    @Test
    void deleteDownload_whenDownloadNotFound_thenReturnNotFound() {
        String errorMessage = "Download to delete not found: " + NOT_FOUND_DOWNLOAD_ID;
        doThrow(new NotFoundException(errorMessage)).when(downloadService).deleteDownload(NOT_FOUND_DOWNLOAD_ID);
        webTestClient.delete().uri(BASE_URI_DOWNLOADS + "/" + NOT_FOUND_DOWNLOAD_ID)
                .accept(MediaType.APPLICATION_JSON) // Expect error response
                .exchange()
                .expectStatus().isNotFound()
                .expectBody(HttpErrorInfo.class)
                .value(error -> assertTrue(error.getMessage().contains(errorMessage)));
        verify(downloadService, times(1)).deleteDownload(NOT_FOUND_DOWNLOAD_ID);
    }
} 