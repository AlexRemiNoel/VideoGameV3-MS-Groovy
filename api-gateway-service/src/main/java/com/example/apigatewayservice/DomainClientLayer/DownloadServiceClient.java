package com.example.apigatewayservice.DomainClientLayer;

import com.example.apigatewayservice.presentationlayer.DownloadRequestModel;
import com.example.apigatewayservice.presentationlayer.DownloadResponseModel;
import com.example.apigatewayservice.exception.HttpErrorInfo;
import com.example.apigatewayservice.exception.InvalidInputException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.support.NotFoundException;
import org.springframework.http.HttpMethod;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.List;

import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY;

@Slf4j
@Component
public class DownloadServiceClient {

    private final RestTemplate restTemplate;
    private final ObjectMapper mapper;
    private final String DOWNLOAD_SERVICE_BASE_URL;

    public DownloadServiceClient(
            RestTemplate restTemplate,
            ObjectMapper mapper,
            @Value("${app.download-service.host}") String downloadServiceHost,
            @Value("${app.download-service.port}") String downloadServicePort
    ) {
        this.restTemplate = restTemplate;
        this.mapper = mapper;
        this.DOWNLOAD_SERVICE_BASE_URL = "http://" + downloadServiceHost + ":" + downloadServicePort + "/api/v1/downloads"; // Base path from backend controller
        log.info("Download Service Base URL: {}", DOWNLOAD_SERVICE_BASE_URL);
    }

    // === Client Methods ===

    public DownloadResponseModel createDownload(DownloadRequestModel requestModel) {
        String url = DOWNLOAD_SERVICE_BASE_URL;
        log.debug("3. Client sending POST request to: {}", url);
        try {
            DownloadResponseModel response = restTemplate.postForObject(url, requestModel, DownloadResponseModel.class);
            log.debug("5. Client received response from POST");
            return response;
        } catch (HttpClientErrorException ex) {
            log.warn("Client POST request failed: {}", ex.getMessage());
            throw handleHttpClientException(ex);
        }
    }

    public DownloadResponseModel getDownload(String id) {
        String url = DOWNLOAD_SERVICE_BASE_URL + "/" + id;
        log.debug("3. Client sending GET request to: {}", url);
        try {
            DownloadResponseModel response = restTemplate.getForObject(url, DownloadResponseModel.class);
            log.debug("5. Client received response from GET by ID");
            return response;
        } catch (HttpClientErrorException ex) {
            log.warn("Client GET by ID request failed: {}", ex.getMessage());
            throw handleHttpClientException(ex);
        }
    }

    public List<DownloadResponseModel> getAllDownloads() {
        String url = DOWNLOAD_SERVICE_BASE_URL;
        log.debug("3. Client sending GET request to: {}", url);
        try {
            List<DownloadResponseModel> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<DownloadResponseModel>>() {}
            ).getBody();
            log.debug("5. Client received response from GET All");
            return response;
        } catch (HttpClientErrorException ex) {
            log.warn("Client GET All request failed: {}", ex.getMessage());
            throw handleHttpClientException(ex);
        }
    }

    // Helper for state change POST requests
    private DownloadResponseModel postForStateChange(String id, String action) {
        String url = DOWNLOAD_SERVICE_BASE_URL + "/" + id + "/" + action;
        log.debug("3. Client sending POST request for action '{}' to: {}", action, url);
        try {
            // These POSTs don't send a body, but expect a response
            DownloadResponseModel response = restTemplate.postForObject(url, null, DownloadResponseModel.class);
            log.debug("5. Client received response from POST action '{}'", action);
            return response;
        } catch (HttpClientErrorException ex) {
            log.warn("Client POST action '{}' request failed: {}", action, ex.getMessage());
            throw handleHttpClientException(ex);
        }
    }

    public DownloadResponseModel startDownload(String id) {
        return postForStateChange(id, "start");
    }

    public DownloadResponseModel pauseDownload(String id) {
        return postForStateChange(id, "pause");
    }

    public DownloadResponseModel resumeDownload(String id) {
        return postForStateChange(id, "resume");
    }

    public DownloadResponseModel cancelDownload(String id) {
        return postForStateChange(id, "cancel");
    }

    public void deleteDownload(String id) {
        String url = DOWNLOAD_SERVICE_BASE_URL + "/" + id;
        log.debug("3. Client sending DELETE request to: {}", url);
        try {
            restTemplate.delete(url);
            log.debug("5. Client received response from DELETE");
        } catch (HttpClientErrorException ex) {
            log.warn("Client DELETE request failed: {}", ex.getMessage());
            throw handleHttpClientException(ex);
        }
    }


    // === Error Handling (Copy from InventoryServiceClient or centralize) ===

    private RuntimeException handleHttpClientException(HttpClientErrorException ex) {
        // Use static imports for HttpStatus
        if (ex.getStatusCode() == NOT_FOUND) {
            log.warn("Converting {} to NotFoundException", ex.getStatusCode());
            return new NotFoundException(getErrorMessage(ex));
        }
        if (ex.getStatusCode() == UNPROCESSABLE_ENTITY) {
            log.warn("Converting {} to InvalidInputException", ex.getStatusCode());
            return new InvalidInputException(getErrorMessage(ex));
        }
        // Add handling for other relevant status codes if needed (e.g., BAD_REQUEST)
        // if (ex.getStatusCode() == HttpStatus.BAD_REQUEST) { ... }

        log.warn("Got an unexpected HTTP error: {}, will rethrow it", ex.getStatusCode());
        log.warn("Error body: {}", ex.getResponseBodyAsString());
        return ex; // Rethrow original exception if not handled specifically
    }

    private String getErrorMessage(HttpClientErrorException ex) {
        try {
            // Assumes downstream service returns error body matching HttpErrorInfo
            return mapper.readValue(ex.getResponseBodyAsString(), HttpErrorInfo.class).getMessage();
        } catch (IOException ioex) {
            log.error("Error parsing error message from HttpClientErrorException: {}", ioex.getMessage());
            return ex.getResponseBodyAsString(); // Fallback
        } catch (Exception e) {
            log.error("Unexpected error parsing error message: {}", e.getMessage());
            return ex.getResponseBodyAsString(); // Fallback
        }
    }
}