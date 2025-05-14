package com.example.apigatewayservice.DomainClientLayer.dashboard;

import com.example.apigatewayservice.exception.HttpErrorInfo;
import com.example.apigatewayservice.exception.InvalidInputException;
import com.example.apigatewayservice.exception.NotFoundException;
import com.example.apigatewayservice.presentationlayer.dashboard.UserProfileDashboardResponseDTO_GW;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.List;

import static org.springframework.http.HttpStatus.*;

@Slf4j
@Component
public class ProfileDashboardServiceClient {

    private final RestTemplate restTemplate;
    private final ObjectMapper mapper;
    private final String dashboardServiceBaseUrl;

    public ProfileDashboardServiceClient(
            RestTemplate restTemplate,
            ObjectMapper mapper) {
        this.restTemplate = restTemplate;
        this.mapper = mapper;
        this.dashboardServiceBaseUrl = "http://" + "dashboard-management" + ":" + "8080" + "/api/v1/profile-dashboards";
    }

    public UserProfileDashboardResponseDTO_GW getProfileDashboardByUserId(String userId) {
        try {
            String url = dashboardServiceBaseUrl + "/" + userId;
            log.debug("Fetching profile dashboard for userId {} from URL: {}", userId, url);
            UserProfileDashboardResponseDTO_GW response = restTemplate.getForObject(url, UserProfileDashboardResponseDTO_GW.class);
            log.debug("Received profile dashboard: {}", response);
            return response;
        } catch (HttpClientErrorException ex) {
            log.warn("HTTP error from dashboard service (getProfileDashboardByUserId: {}): {}", userId, ex.getStatusCode());
            throw handleHttpClientException(ex);
        }
    }

    public List<UserProfileDashboardResponseDTO_GW> getAllProfileDashboards() {
        try {
            String url = dashboardServiceBaseUrl;
            log.debug("Fetching all profile dashboards from URL: {}", url);
            ResponseEntity<List<UserProfileDashboardResponseDTO_GW>> responseEntity =
                    restTemplate.exchange(url, HttpMethod.GET, null,
                            new ParameterizedTypeReference<List<UserProfileDashboardResponseDTO_GW>>() {});
            log.debug("Received dashboards: {}", responseEntity.getBody());
            return responseEntity.getBody();
        } catch (HttpClientErrorException ex) {
            log.warn("HTTP error from dashboard service (getAllProfileDashboards): {}", ex.getStatusCode());
            throw handleHttpClientException(ex);
        }
    }

    public UserProfileDashboardResponseDTO_GW createOrRefreshDashboard(String userId) {
        try {
            String url = dashboardServiceBaseUrl + "/" + userId;
            log.debug("Creating/refreshing dashboard for userId {} at URL: {}", userId, url);
            // Aggregator's POST doesn't take a body, so request is null
            UserProfileDashboardResponseDTO_GW response = restTemplate.postForObject(url, null, UserProfileDashboardResponseDTO_GW.class);
            log.debug("Created/refreshed dashboard: {}", response);
            return response;
        } catch (HttpClientErrorException ex) {
            log.warn("HTTP error from dashboard service (createOrRefreshDashboard: {}): {}", userId, ex.getStatusCode());
            throw handleHttpClientException(ex);
        }
    }

    public UserProfileDashboardResponseDTO_GW updateProfileDashboard(String userId) {
        try {
            String url = dashboardServiceBaseUrl + "/" + userId;
            log.debug("Updating dashboard for userId {} at URL: {}", userId, url);
            // Aggregator's PUT takes userId in path, no body, but returns the DTO.
            // Use exchange for more control if PUT returns a body.
            RequestEntity<Void> requestEntity = new RequestEntity<>(HttpMethod.PUT, java.net.URI.create(url));
            ResponseEntity<UserProfileDashboardResponseDTO_GW> responseEntity =
                    restTemplate.exchange(requestEntity, UserProfileDashboardResponseDTO_GW.class);
            log.debug("Updated dashboard: {}", responseEntity.getBody());
            return responseEntity.getBody();
        } catch (HttpClientErrorException ex) {
            log.warn("HTTP error from dashboard service (updateProfileDashboard: {}): {}", userId, ex.getStatusCode());
            throw handleHttpClientException(ex);
        }
    }


    public void deleteProfileDashboard(String userId) {
        try {
            String url = dashboardServiceBaseUrl + "/" + userId;
            log.debug("Deleting dashboard for userId {} at URL: {}", userId, url);
            restTemplate.delete(url);
            log.debug("Delete successful for dashboard userId: {}", userId);
        } catch (HttpClientErrorException ex) {
            log.warn("HTTP error from dashboard service (deleteProfileDashboard: {}): {}", userId, ex.getStatusCode());
            throw handleHttpClientException(ex);
        }
    }

    private RuntimeException handleHttpClientException(HttpClientErrorException ex) {
        log.warn("Handling HttpClientErrorException: Status Code: {}, Response Body: {}", ex.getStatusCode(), ex.getResponseBodyAsString());
        HttpStatusCode statusCode = ex.getStatusCode();
        if (statusCode.equals(NOT_FOUND)) {
            return new NotFoundException(getErrorMessage(ex));
        } else if (statusCode.equals(UNPROCESSABLE_ENTITY) || statusCode.equals(BAD_REQUEST)) { // Downstream might use BAD_REQUEST for invalid input
            return new InvalidInputException(getErrorMessage(ex));
        }
        log.warn("Got an unexpected HTTP error: {}, will rethrow it as a generic exception", ex.getStatusCode());
        return new RuntimeException("Unexpected HTTP error: " + ex.getStatusCode() + " - " + getErrorMessage(ex), ex);
    }

    private String getErrorMessage(HttpClientErrorException ex) {
        try {
            if (ex.getResponseBodyAsString() == null || ex.getResponseBodyAsString().isEmpty()) {
                return ex.getStatusText();
            }
            // Assuming downstream service also uses HttpErrorInfo or a compatible structure
            HttpErrorInfo errorInfo = mapper.readValue(ex.getResponseBodyAsString(), HttpErrorInfo.class);
            return errorInfo.getMessage();
        } catch (IOException ioex) {
            log.warn("Error parsing error message from response body: {}", ioex.getMessage());
            return ex.getResponseBodyAsString();
        }
    }
}