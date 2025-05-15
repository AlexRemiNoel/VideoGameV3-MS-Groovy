package com.example.apigatewayservice.DomainClientLayer.download;

import com.example.apigatewayservice.exception.HttpErrorInfo;
import com.example.apigatewayservice.exception.InvalidInputException;
import com.example.apigatewayservice.exception.NotFoundException;
import com.example.apigatewayservice.presentationlayer.download.DownloadRequestModel;
import com.example.apigatewayservice.presentationlayer.download.DownloadResponseModel;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DownloadServiceClientTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private ObjectMapper objectMapper; // mapper in actual class, aliased here for consistency

    @InjectMocks
    private DownloadServiceClient downloadServiceClient;

    private final String DOWNLOAD_ID = UUID.randomUUID().toString();
    private final String DOWNLOAD_SERVICE_HOST = "download-service";
    private final String DOWNLOAD_SERVICE_PORT = "8082";
    private String BASE_URL;

    private DownloadResponseModel sampleDownloadResponse;
    private DownloadRequestModel sampleDownloadRequest;

    @BeforeEach
    void setUp() {
        downloadServiceClient = new DownloadServiceClient(restTemplate, objectMapper, DOWNLOAD_SERVICE_HOST, DOWNLOAD_SERVICE_PORT);
        BASE_URL = "http://" + DOWNLOAD_SERVICE_HOST + ":" + DOWNLOAD_SERVICE_PORT + "/api/v1/downloads";

        sampleDownloadResponse = DownloadResponseModel.builder()
                .id(DOWNLOAD_ID)
                .sourceUrl("http://example.com/file.zip")
                .status("COMPLETED")
                .build();

        sampleDownloadRequest = DownloadRequestModel.builder()
                .sourceUrl("http://example.com/file.zip")
                .build();
    }

    private HttpClientErrorException mockHttpClientErrorException(HttpStatus status, String responseBody) {
        return new HttpClientErrorException(status, status.getReasonPhrase(), responseBody.getBytes(), java.nio.charset.StandardCharsets.UTF_8);
    }

    // --- Success Cases ---
    @Test
    void createDownload_success() {
        when(restTemplate.postForObject(eq(BASE_URL), eq(sampleDownloadRequest), eq(DownloadResponseModel.class)))
                .thenReturn(sampleDownloadResponse);
        DownloadResponseModel result = downloadServiceClient.createDownload(sampleDownloadRequest);
        assertEquals(sampleDownloadResponse, result);
        verify(restTemplate).postForObject(eq(BASE_URL), eq(sampleDownloadRequest), eq(DownloadResponseModel.class));
    }

    @Test
    void getDownload_success() {
        String url = BASE_URL + "/" + DOWNLOAD_ID;
        when(restTemplate.getForObject(eq(url), eq(DownloadResponseModel.class))).thenReturn(sampleDownloadResponse);
        DownloadResponseModel result = downloadServiceClient.getDownload(DOWNLOAD_ID);
        assertEquals(sampleDownloadResponse, result);
        verify(restTemplate).getForObject(eq(url), eq(DownloadResponseModel.class));
    }

    @Test
    void getAllDownloads_success() {
        List<DownloadResponseModel> expectedResponse = List.of(sampleDownloadResponse);
        ResponseEntity<List<DownloadResponseModel>> responseEntity = new ResponseEntity<>(expectedResponse, HttpStatus.OK);
        when(restTemplate.exchange(eq(BASE_URL), eq(HttpMethod.GET), isNull(), any(ParameterizedTypeReference.class)))
                .thenReturn(responseEntity);
        List<DownloadResponseModel> result = downloadServiceClient.getAllDownloads();
        assertFalse(result.isEmpty());
        assertEquals(sampleDownloadResponse, result.get(0));
        verify(restTemplate).exchange(eq(BASE_URL), eq(HttpMethod.GET), isNull(), any(ParameterizedTypeReference.class));
    }

    @Test
    void updateDownload_success() {
        String url = BASE_URL + "/" + DOWNLOAD_ID;
        HttpEntity<DownloadRequestModel> requestEntity = new HttpEntity<>(sampleDownloadRequest);
        ResponseEntity<DownloadResponseModel> responseEntity = new ResponseEntity<>(sampleDownloadResponse, HttpStatus.OK);
        when(restTemplate.exchange(eq(url), eq(HttpMethod.PUT), eq(requestEntity), eq(DownloadResponseModel.class)))
                .thenReturn(responseEntity);

        DownloadResponseModel result = downloadServiceClient.updateDownload(DOWNLOAD_ID, sampleDownloadRequest);
        assertEquals(sampleDownloadResponse, result);
        verify(restTemplate).exchange(eq(url), eq(HttpMethod.PUT), eq(requestEntity), eq(DownloadResponseModel.class));
    }

    @Test
    void deleteDownload_success() {
        String url = BASE_URL + "/" + DOWNLOAD_ID;
        doNothing().when(restTemplate).delete(eq(url));
        downloadServiceClient.deleteDownload(DOWNLOAD_ID);
        verify(restTemplate).delete(eq(url));
    }

    // --- State Change Success Cases (testing postForStateChange indirectly) ---
    @Test
    void startDownload_success() {
        String url = BASE_URL + "/" + DOWNLOAD_ID + "/start";
        when(restTemplate.postForObject(eq(url), isNull(), eq(DownloadResponseModel.class))).thenReturn(sampleDownloadResponse);
        DownloadResponseModel result = downloadServiceClient.startDownload(DOWNLOAD_ID);
        assertEquals(sampleDownloadResponse, result);
        verify(restTemplate).postForObject(eq(url), isNull(), eq(DownloadResponseModel.class));
    }

    @Test
    void pauseDownload_success() {
        String url = BASE_URL + "/" + DOWNLOAD_ID + "/pause";
        when(restTemplate.postForObject(eq(url), isNull(), eq(DownloadResponseModel.class))).thenReturn(sampleDownloadResponse);
        DownloadResponseModel result = downloadServiceClient.pauseDownload(DOWNLOAD_ID);
        assertEquals(sampleDownloadResponse, result);
        verify(restTemplate).postForObject(eq(url), isNull(), eq(DownloadResponseModel.class));
    }

    @Test
    void resumeDownload_success() {
        String url = BASE_URL + "/" + DOWNLOAD_ID + "/resume";
        when(restTemplate.postForObject(eq(url), isNull(), eq(DownloadResponseModel.class))).thenReturn(sampleDownloadResponse);
        DownloadResponseModel result = downloadServiceClient.resumeDownload(DOWNLOAD_ID);
        assertEquals(sampleDownloadResponse, result);
        verify(restTemplate).postForObject(eq(url), isNull(), eq(DownloadResponseModel.class));
    }

    @Test
    void cancelDownload_success() {
        String url = BASE_URL + "/" + DOWNLOAD_ID + "/cancel";
        when(restTemplate.postForObject(eq(url), isNull(), eq(DownloadResponseModel.class))).thenReturn(sampleDownloadResponse);
        DownloadResponseModel result = downloadServiceClient.cancelDownload(DOWNLOAD_ID);
        assertEquals(sampleDownloadResponse, result);
        verify(restTemplate).postForObject(eq(url), isNull(), eq(DownloadResponseModel.class));
    }

    // --- Exception Cases ---
    @Test
    void getDownload_notFoundException() throws IOException {
        String url = BASE_URL + "/" + DOWNLOAD_ID;
        String errorMsgJson = "{\"message\":\"Download not found\"}";
        HttpClientErrorException ex = mockHttpClientErrorException(HttpStatus.NOT_FOUND, errorMsgJson);
        when(objectMapper.readValue(errorMsgJson, HttpErrorInfo.class))
                .thenReturn(new HttpErrorInfo(HttpStatus.NOT_FOUND, "/path", "Download not found"));
        when(restTemplate.getForObject(eq(url), eq(DownloadResponseModel.class))).thenThrow(ex);

        NotFoundException thrown = assertThrows(NotFoundException.class, () -> downloadServiceClient.getDownload(DOWNLOAD_ID));
        assertEquals("Download not found", thrown.getMessage());
        verify(objectMapper).readValue(errorMsgJson, HttpErrorInfo.class);
    }

    @Test
    void createDownload_invalidInputException() throws IOException {
        String errorMsgJson = "{\"message\":\"Invalid input\"}";
        HttpClientErrorException ex = mockHttpClientErrorException(HttpStatus.UNPROCESSABLE_ENTITY, errorMsgJson);
        when(objectMapper.readValue(errorMsgJson, HttpErrorInfo.class))
                .thenReturn(new HttpErrorInfo(HttpStatus.UNPROCESSABLE_ENTITY, "/path", "Invalid input"));
        when(restTemplate.postForObject(eq(BASE_URL), eq(sampleDownloadRequest), eq(DownloadResponseModel.class)))
                .thenThrow(ex);

        InvalidInputException thrown = assertThrows(InvalidInputException.class, () -> downloadServiceClient.createDownload(sampleDownloadRequest));
        assertEquals("Invalid input", thrown.getMessage());
        verify(objectMapper).readValue(errorMsgJson, HttpErrorInfo.class);
    }

    @Test
    void getDownload_genericHttpClientErrorException() {
        String url = BASE_URL + "/" + DOWNLOAD_ID;
        HttpClientErrorException originalEx = mockHttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error");
        when(restTemplate.getForObject(eq(url), eq(DownloadResponseModel.class))).thenThrow(originalEx);

        HttpClientErrorException thrown = assertThrows(HttpClientErrorException.class, () -> downloadServiceClient.getDownload(DOWNLOAD_ID));
        assertEquals(originalEx, thrown);
    }

    // --- getErrorMessage specific tests ---
    @Test
    void getErrorMessage_parsedSuccessfully() throws IOException {
        String actualErrorMessage = "Specific error from download service.";
        String errorBody = "{\"message\":\"" + actualErrorMessage + "\",\"path\":\"/api/v1/downloads/some-id\",\"httpStatus\":\"NOT_FOUND\"}";
        HttpClientErrorException ex = mockHttpClientErrorException(HttpStatus.NOT_FOUND, errorBody);
        when(objectMapper.readValue(errorBody, HttpErrorInfo.class))
                .thenReturn(new HttpErrorInfo(HttpStatus.NOT_FOUND, "/path", actualErrorMessage));
        // Trigger via a method that uses it for NOT_FOUND
        when(restTemplate.getForObject(anyString(), eq(DownloadResponseModel.class))).thenThrow(ex);

        try {
            downloadServiceClient.getDownload("some-id");
            fail("Exception expected");
        } catch (NotFoundException e) {
            assertEquals(actualErrorMessage, e.getMessage());
        }
        verify(objectMapper).readValue(errorBody, HttpErrorInfo.class);
    }

    @Test
    void getErrorMessage_objectMapperThrowsIOException() throws IOException {
        String rawErrorBody = "invalid-json-for-download-error";
        HttpClientErrorException ex = mockHttpClientErrorException(HttpStatus.UNPROCESSABLE_ENTITY, rawErrorBody);
        when(objectMapper.readValue(rawErrorBody, HttpErrorInfo.class)).thenThrow(new JsonProcessingException("Parse fail") {});
        when(restTemplate.postForObject(anyString(), any(), eq(DownloadResponseModel.class))).thenThrow(ex);

        try {
            downloadServiceClient.createDownload(sampleDownloadRequest); // Triggers UNPROCESSABLE_ENTITY path
            fail("Exception expected");
        } catch (InvalidInputException e) {
            // Fallback to raw response body per DownloadServiceClient's getErrorMessage
            assertEquals(rawErrorBody, e.getMessage());
        }
        verify(objectMapper).readValue(rawErrorBody, HttpErrorInfo.class);
    }

    @Test
    void getErrorMessage_objectMapperThrowsOtherException() throws IOException {
        String rawErrorBody = "some-other-unparseable-body";
        HttpClientErrorException ex = mockHttpClientErrorException(HttpStatus.NOT_FOUND, rawErrorBody);
        // Simulate a different exception during parsing
        when(objectMapper.readValue(rawErrorBody, HttpErrorInfo.class)).thenThrow(new RuntimeException("Unexpected parsing issue"));
        when(restTemplate.getForObject(anyString(), eq(DownloadResponseModel.class))).thenThrow(ex);

        try {
            downloadServiceClient.getDownload("some-id-for-other-ex");
            fail("Exception expected");
        } catch (NotFoundException e) {
            // Fallback to raw response body per DownloadServiceClient's getErrorMessage
            assertEquals(rawErrorBody, e.getMessage());
        }
        verify(objectMapper).readValue(rawErrorBody, HttpErrorInfo.class);
    }
} 