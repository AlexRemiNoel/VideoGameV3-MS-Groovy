package com.example.apigatewayservice.DomainClientLayer.game;

import com.example.apigatewayservice.exception.HttpErrorInfo;
import com.example.apigatewayservice.exception.InvalidInputException;
import com.example.apigatewayservice.exception.NotFoundException;
import com.example.apigatewayservice.presentationlayer.game.GameRequestModel;
import com.example.apigatewayservice.presentationlayer.game.GameResponseModel;
import com.example.apigatewayservice.presentationlayer.game.ReviewRequestModel;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.List;

import static org.springframework.http.HttpMethod.PUT;


@Slf4j
@Component
public class GameServiceClient {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final String gameServiceUrl;

    public GameServiceClient(RestTemplate restTemplate,
                             ObjectMapper objectMapper, // Use consistent name
                             @Value("${app.game-service.host}") String gameServiceHost,
                             @Value("${app.game-service.port}") String gameServicePort) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        // Construct the base URL similar to the example
        this.gameServiceUrl = "http://" + gameServiceHost + ":" + gameServicePort + "/api/v1/game";
        log.info("Game Service URL: {}", gameServiceUrl); // Log the constructed URL
    }

    // GET All Games
    public List<GameResponseModel> getAllGames() {
        try {
            String url = gameServiceUrl;
            log.debug("Fetching all games from URL: {}", url);
            List<GameResponseModel> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<GameResponseModel>>() {}
            ).getBody();
            log.debug("Received {} games", response != null ? response.size() : 0);
            return response;
        } catch (HttpClientErrorException ex) {
            log.warn("getAllGames failed with status: {}", ex.getStatusCode());
            throw handleHttpClientException(ex);
        }
    }

    // GET Game by UUID
    public GameResponseModel getGameByGameId(String uuid) {
        try {
            String url = gameServiceUrl + "/" + uuid;
            log.debug("Fetching game by ID from URL: {}", url);
            GameResponseModel response = restTemplate.getForObject(url, GameResponseModel.class);
            log.debug("Received game: {}", response != null ? response.getId() : "null");
            return response;
        } catch (HttpClientErrorException ex) {
            log.warn("getGameByGameId failed for UUID: {} with status: {}", uuid, ex.getStatusCode());
            throw handleHttpClientException(ex);
        }
    }


    public GameResponseModel createGame(GameRequestModel gameRequestModel) {
        try {
            String url = gameServiceUrl;
            log.debug("Adding game via URL: {}", url);
            GameResponseModel response = restTemplate.postForObject(url, gameRequestModel, GameResponseModel.class);
            log.debug("Added game: {}", response != null ? response.getId() : "null");
            return response;
        } catch (HttpClientErrorException ex) {
            log.warn("createGame failed with status: {}", ex.getStatusCode());
            throw handleHttpClientException(ex);
        }
    }


    public GameResponseModel updateGame(GameRequestModel gameRequestModel) {

        try {
            String url = gameServiceUrl;
            log.debug("Updating game via URL: {}", url);


            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<GameRequestModel> requestEntity = new HttpEntity<>(gameRequestModel, headers);

            ResponseEntity<GameResponseModel> responseEntity = restTemplate.exchange(
                    url,
                    PUT,
                    requestEntity,
                    GameResponseModel.class
            );

            log.debug("Update request sent for game (inferred from body), status: {}", responseEntity.getStatusCode());
            return responseEntity.getBody();
        } catch (HttpClientErrorException ex) {
            log.warn("updateGame failed with status: {}", ex.getStatusCode());
            throw handleHttpClientException(ex);
        }
    }

    // DELETE Game
    public void deleteGame(String uuid) {
        try {
            String url = gameServiceUrl + "/" + uuid;
            log.debug("Deleting game via URL: {}", url);
            restTemplate.delete(url); // Using simple delete like the example
            log.debug("Delete request sent for game: {}", uuid);
        } catch (HttpClientErrorException ex) {
            log.warn("deleteGame failed for UUID: {} with status: {}", uuid, ex.getStatusCode());
            throw handleHttpClientException(ex);
        }
    }

    // POST (Add Review)
    public GameResponseModel addReview(ReviewRequestModel reviewRequestModel, String gameUuid) {
        try {
            String url = gameServiceUrl + "/review/" + gameUuid;
            log.debug("Adding review to game {} via URL: {}", gameUuid, url);
            GameResponseModel response = restTemplate.postForObject(url, reviewRequestModel, GameResponseModel.class);
            log.debug("Added review to game: {}", response != null ? response.getId() : "null");
            return response;
        } catch (HttpClientErrorException ex) {
            log.warn("addReview failed for game UUID: {} with status: {}", gameUuid, ex.getStatusCode());
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
            String responseBody = ex.getResponseBodyAsString();
            if (!responseBody.isBlank() && responseBody.trim().startsWith("{") && responseBody.trim().endsWith("}")) {
                HttpErrorInfo errorInfo = objectMapper.readValue(responseBody, HttpErrorInfo.class);
                if (errorInfo != null && errorInfo.getMessage() != null && !errorInfo.getMessage().isBlank()) {
                    log.debug("Parsed error message from response body: {}", errorInfo.getMessage());
                    return errorInfo.getMessage();
                }
            }
            if (responseBody != null && !responseBody.isBlank()) {
                log.debug("Using full response body as error message: {}", responseBody);
                return responseBody;
            }

        } catch (IOException ioex) {
            log.warn("Error parsing HttpClientErrorException body: {}. Falling back.", ioex.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error parsing error message: {}. Falling back.", e.getMessage());
        }

        if (ex.getMessage() != null && !ex.getMessage().isBlank()){
            log.debug("Using exception's default message: {}", ex.getMessage());
            return ex.getMessage();
        }

        log.debug("Using generic error message for status code: {}", ex.getStatusCode());
        return "An error occurred with status code: " + ex.getStatusCode();
    }
}