package com.example.apigatewayservice.DomainClientLayer.user;

import com.example.apigatewayservice.exception.HttpErrorInfo;
import org.springframework.cloud.gateway.support.NotFoundException;
import org.springframework.http.HttpMethod;
import com.example.apigatewayservice.exception.InvalidInputException;
import com.example.apigatewayservice.presentationlayer.user.UserRequestModel;
import com.example.apigatewayservice.presentationlayer.user.UserResponseModel;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.List;

@Component
@Slf4j
public class UserServiceClient {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final String userServiceUrl;

    public UserServiceClient(
            RestTemplate restTemplate,
            ObjectMapper objectMapper,
            @Value("user-management") String userServiceHost,
            @Value("8080") String userServicePort) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.userServiceUrl = "http://" + userServiceHost + ":" + userServicePort + "/api/v1/user";
        log.info("User Service URL: {}", userServiceUrl);
    }

    public List<UserResponseModel> getAllUsers() {
        try {
            String url = userServiceUrl;
            log.debug("Fetching all users from URL: {}", url);
            List<UserResponseModel> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<UserResponseModel>>() {}
            ).getBody();
            log.debug("Received {} users", response != null ? response.size() : 0);
            return response;
        } catch (HttpClientErrorException ex) {
            log.warn("getAllUsers failed with status: {}", ex.getStatusCode());
            throw handleHttpClientException(ex);
        }
    }

    public UserResponseModel getUserById(String uuid) {
        try {
            String url = userServiceUrl + "/" + uuid;
            log.debug("Fetching user by ID from URL: {}", url);
            UserResponseModel response = restTemplate.getForObject(url, UserResponseModel.class);
            log.debug("Received user: {}", response != null ? response.getUserId() : "null");
            return response;
        } catch (HttpClientErrorException ex) {
            log.warn("getUserById failed for UUID: {} with status: {}", uuid, ex.getStatusCode());
            throw handleHttpClientException(ex);
        }
    }

    public UserResponseModel addUser(UserRequestModel userRequestModel) {
        try {
            String url = userServiceUrl;
            log.debug("Adding user via URL: {}", url);
            UserResponseModel response = restTemplate.postForObject(url, userRequestModel, UserResponseModel.class);
            log.debug("Added user: {}", response != null ? response.getUserId() : "null");
            return response;
        } catch (HttpClientErrorException ex) {
            log.warn("addUser failed with status: {}", ex.getStatusCode());
            throw handleHttpClientException(ex);
        }
    }

    public UserResponseModel updateUser(UserRequestModel userRequestModel, String uuid) {
        try {
            String url = userServiceUrl + "/" + uuid;
            log.debug("Updating user via URL: {}", url);
            restTemplate.put(url, userRequestModel);
            log.debug("Update request sent for user: {}", uuid);
            return null;
        } catch (HttpClientErrorException ex) {
            log.warn("updateUser failed for UUID: {} with status: {}", uuid, ex.getStatusCode());
            throw handleHttpClientException(ex);
        }
    }

    public void deleteUser(String uuid) {
        try {
            String url = userServiceUrl + "/" + uuid;
            log.debug("Deleting user via URL: {}", url);
            restTemplate.delete(url);
            log.debug("Delete request sent for user: {}", uuid);
        } catch (HttpClientErrorException ex) {
            log.warn("deleteUser failed for UUID: {} with status: {}", uuid, ex.getStatusCode());
            throw handleHttpClientException(ex);
        }
    }

    public UserResponseModel updateUserBalance(String userId, double balance) {
        try {

            String url = userServiceUrl + "/uuid/" + userId + "/balance/" + balance;
            log.debug("Updating user balance via URL: {}", url);


            UserResponseModel response = restTemplate.exchange(
                            url,
                            HttpMethod.PUT,
                            null,
                            UserResponseModel.class)
                    .getBody();
            log.debug("Updated balance for user: {}", response != null ? response.getUserId() : "null");
            return response;
        } catch (HttpClientErrorException ex) {
            log.warn("updateUserBalance failed for user: {} with status: {}", userId, ex.getStatusCode());
            throw handleHttpClientException(ex);
        }
    }




    private RuntimeException handleHttpClientException(HttpClientErrorException ex) {
        HttpStatus status = (HttpStatus) ex.getStatusCode();
        switch (status) {
            case NOT_FOUND:
                return new NotFoundException(getErrorMessage(ex));
            case UNPROCESSABLE_ENTITY:
                return new InvalidInputException(getErrorMessage(ex));
            default:
                log.warn("Got an unexpected HTTP error: {}, will rethrow it", status);
                log.warn("Error body: {}", ex.getResponseBodyAsString());
                return ex;
        }
    }

    private String getErrorMessage(HttpClientErrorException ex) {
        try {
            HttpErrorInfo errorInfo = objectMapper.readValue(ex.getResponseBodyAsString(), HttpErrorInfo.class);
            if (errorInfo != null && errorInfo.getMessage() != null && !errorInfo.getMessage().isEmpty()) {
                return errorInfo.getMessage();
            }
            return ex.getResponseBodyAsString();
        } catch (IOException ioex) {
            log.warn("Error parsing HttpClientErrorException body: {}", ioex.getMessage());
            return ex.getResponseBodyAsString();
        } catch (Exception e) {
            log.error("Unexpected error parsing error message: {}", e.getMessage());
            return ex.getMessage();
        }
    }
}