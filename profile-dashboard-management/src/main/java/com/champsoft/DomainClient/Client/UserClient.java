package com.champsoft.DomainClient.Client;

import com.champsoft.DomainClient.Dtos.UserClientResponseDto;
import com.champsoft.Exceptions.DownstreamServiceUnavailableException;
import com.champsoft.Exceptions.UserNotFoundClientException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
public class UserClient {

    private final RestTemplate restTemplate;
    private final String userServiceUrl;

    public UserClient(RestTemplate restTemplate,
                      @Value("${microservices.user.url}") String userServiceUrl) {
        this.restTemplate = restTemplate;
        this.userServiceUrl = userServiceUrl; // e.g., http://localhost:8081/api/v1/user
    }

    public UserClientResponseDto getUserById(String userId) {
        String url = userServiceUrl + "/" + userId;
        try {
            log.info("Fetching user details for userId: {} from URL: {}", userId, url);
            UserClientResponseDto user = restTemplate.getForObject(url, UserClientResponseDto.class);
            if (user == null) { // Should ideally be handled by 404, but as a safeguard
                log.warn("User not found for userId: {}, service returned null", userId);
                throw new UserNotFoundClientException("User not found with ID: " + userId + " (service returned null)");
            }
            log.info("Successfully fetched user details for userId: {}", userId);
            return user;
        } catch (HttpClientErrorException.NotFound ex) {
            log.warn("User not found for userId: {} (404 from user service)", userId, ex);
            throw new UserNotFoundClientException("User not found with ID: " + userId);
        } catch (ResourceAccessException ex) {
            log.error("Error accessing user service at {}: {}", url, ex.getMessage(), ex);
            throw new DownstreamServiceUnavailableException("User service is unavailable or network issue.", ex);
        } catch (Exception ex) {
            log.error("Unexpected error while fetching user with ID {}: {}", userId, ex.getMessage(), ex);
            throw new DownstreamServiceUnavailableException("Unexpected error communicating with user service.", ex);
        }
    }
}