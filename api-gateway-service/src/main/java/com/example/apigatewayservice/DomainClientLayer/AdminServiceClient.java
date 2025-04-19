package com.example.apigatewayservice.DomainClientLayer;

import com.example.apigatewayservice.presentationlayer.AdminRequestModel;
import org.springframework.cloud.gateway.support.NotFoundException;
import org.springframework.http.HttpMethod;
import com.example.apigatewayservice.exception.HttpErrorInfo;
import com.example.apigatewayservice.exception.InvalidInputException;
import com.example.apigatewayservice.presentationlayer.AdminResponseModel;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.List;

@Component
@Slf4j
public class AdminServiceClient {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final String adminServiceUrl;

    public AdminServiceClient(
            RestTemplate restTemplate,
            ObjectMapper objectMapper,
            @Value("user-management") String adminServiceHost,
            @Value("8080") String adminServicePort
    ) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.adminServiceUrl = "http://" + adminServiceHost + ":" + adminServicePort + "/api/v1/admin";
        log.info("Admin Service URL: {}", adminServiceUrl);
    }

    public List<AdminResponseModel> getAllAdmins() {
        try {
            String url = adminServiceUrl;
            log.debug("Fetching all admins from URL: {}", url);
            List<AdminResponseModel> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<AdminResponseModel>>() {}
            ).getBody();
            log.debug("Received {} admins", response != null ? response.size() : 0);
            return response;
        } catch (HttpClientErrorException ex) {
            log.warn("getAllAdmins failed with status: {}", ex.getStatusCode());
            throw handleHttpClientException(ex);
        }
    }

    public AdminResponseModel getAdminById(String uuid) {
        try {
            String url = adminServiceUrl + "/" + uuid;
            log.debug("Fetching admin by ID from URL: {}", url);
            AdminResponseModel response = restTemplate.getForObject(url, AdminResponseModel.class);
            log.debug("Received admin: {}", response != null ? response.getAdminId() : "null");
            return response;
        } catch (HttpClientErrorException ex) {
            log.warn("getAdminById failed for UUID: {} with status: {}", uuid, ex.getStatusCode());
            throw handleHttpClientException(ex);
        }
    }

    public AdminResponseModel addAdmin(AdminRequestModel adminRequestModel) {
        try {
            String url = adminServiceUrl;
            log.debug("Adding admin via URL: {}", url);
            AdminResponseModel response = restTemplate.postForObject(url, adminRequestModel, AdminResponseModel.class);
            log.debug("Added admin: {}", response != null ? response.getAdminId() : "null");
            return response;
        } catch (HttpClientErrorException ex) {
            log.warn("addAdmin failed with status: {}", ex.getStatusCode());
            throw handleHttpClientException(ex);
        }
    }

    public AdminResponseModel updateAdmin(AdminRequestModel adminRequestModel, String uuid) {
        // RestTemplate.put returns void, so we might need to do a GET after PUT
        // or rely on the backend service validating and returning errors.
        // Option 1: Simple PUT (no response body check)
        try {
            String url = adminServiceUrl + "/" + uuid;
            log.debug("Updating admin via URL: {}", url);
            restTemplate.put(url, adminRequestModel);
            log.debug("Update request sent for admin: {}", uuid);
            // We can't easily return the updated model from put.
            // Usually you'd return void or fetch it again if needed immediately.
            // For simplicity here, we return null or handle differently in service/controller.
            return null; // Or potentially fetch again: return getAdminById(uuid);
        } catch (HttpClientErrorException ex) {
            log.warn("updateAdmin failed for UUID: {} with status: {}", uuid, ex.getStatusCode());
            throw handleHttpClientException(ex);
        }
        // Option 2: Use exchange for more control (more complex)
        /*
        try {
             String url = adminServiceUrl + "/" + uuid;
             log.debug("Updating admin via URL: {}", url);
             ResponseEntity<AdminResponseModel> response = restTemplate.exchange(
                url,
                HttpMethod.PUT,
                new HttpEntity<>(adminRequestModel),
                AdminResponseModel.class
             );
             log.debug("Updated admin: {}", response.getBody() != null ? response.getBody().getAdminId() : "null");
             return response.getBody();
        } catch (HttpClientErrorException ex) {
            log.warn("updateAdmin failed for UUID: {} with status: {}", uuid, ex.getStatusCode());
            throw handleHttpClientException(ex);
        }
        */
    }


    public void deleteAdmin(String uuid) {
        try {
            String url = adminServiceUrl + "/" + uuid;
            log.debug("Deleting admin via URL: {}", url);
            restTemplate.delete(url);
            log.debug("Delete request sent for admin: {}", uuid);
        } catch (HttpClientErrorException ex) {
            log.warn("deleteAdmin failed for UUID: {} with status: {}", uuid, ex.getStatusCode());
            throw handleHttpClientException(ex);
        }
    }


    // --- Error Handling ---

    private RuntimeException handleHttpClientException(HttpClientErrorException ex) {
        HttpStatus status = (HttpStatus) ex.getStatusCode(); // Use HttpStatus enum directly
        switch (status) {
            case NOT_FOUND:
                return new NotFoundException(getErrorMessage(ex));
            case UNPROCESSABLE_ENTITY:
                return new InvalidInputException(getErrorMessage(ex));
            default:
                log.warn("Got an unexpected HTTP error: {}, will rethrow it", status);
                log.warn("Error body: {}", ex.getResponseBodyAsString());
                return ex; // Rethrow unexpected errors
        }
    }

    private String getErrorMessage(HttpClientErrorException ex) {
        try {
            // Attempt to parse the error body using HttpErrorInfo DTO
            HttpErrorInfo errorInfo = objectMapper.readValue(ex.getResponseBodyAsString(), HttpErrorInfo.class);
            // Prefer the message from the parsed DTO if available
            if (errorInfo != null && errorInfo.getMessage() != null && !errorInfo.getMessage().isEmpty()) {
                return errorInfo.getMessage();
            }
            // Fallback to the raw response body if parsing fails or message is empty
            return ex.getResponseBodyAsString();
        } catch (IOException ioex) {
            log.warn("Error parsing HttpClientErrorException body: {}", ioex.getMessage());
            // Fallback to the exception's message or the raw response body as string
            return ex.getResponseBodyAsString();
        } catch (Exception e) {
            log.error("Unexpected error parsing error message: {}", e.getMessage());
            return ex.getMessage(); // Generic fallback
        }
    }
}