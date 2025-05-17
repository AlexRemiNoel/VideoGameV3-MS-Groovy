// In ProfileDashboardAggregator: client/
package com.champsoft.DomainClient.Client;


import com.champsoft.DomainClient.Dtos.GameClientResponseModel;
import com.champsoft.Exceptions.DownstreamServiceUnavailableException;
import com.champsoft.Exceptions.GameNotFoundClientException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Slf4j
@Component
public class GameClient {

    private final RestTemplate restTemplate;
    private final String gameServiceUrl;

    public GameClient(RestTemplate restTemplate,
                      @Value("${microservices.game.url}") String gameServiceUrl) {
        this.restTemplate = restTemplate;
        this.gameServiceUrl = gameServiceUrl;
    }

    public GameClientResponseModel getGameById(String gameId) {
        String url = gameServiceUrl + "/" + gameId;
        try {
            log.info("Fetching game details for gameId: {} from URL: {}", gameId, url);
            GameClientResponseModel game = restTemplate.getForObject(url, GameClientResponseModel.class);
             if (game == null) {
                log.warn("Game not found for gameId: {}, service returned null", gameId);
                throw new GameNotFoundClientException("Game not found with ID: " + gameId + " (service returned null)");
            }
            log.info("Successfully fetched game details for gameId: {}", gameId);
            return game;
        } catch (HttpClientErrorException.NotFound ex) {
            log.warn("Game not found for gameId: {} (404 from game service)", gameId, ex);
            throw new GameNotFoundClientException("Game not found with ID: " + gameId);
        } catch (ResourceAccessException ex) {
            log.error("Error accessing game service at {}: {}", url, ex.getMessage(), ex);
            throw new DownstreamServiceUnavailableException("Game service is unavailable or network issue.", ex);
        } catch (Exception ex) {
            log.error("Unexpected error while fetching game with ID {}: {}", gameId, ex.getMessage(), ex);
            throw new DownstreamServiceUnavailableException("Unexpected error communicating with game service.", ex);
        }
    }

    /**
     * Fetches multiple games by their IDs.
     * NOTE: The current GameService does not support a batch GET. This implementation
     * makes N individual calls. For better performance, these calls are parallelized.
     * Ideally, the GameService would expose an endpoint like /api/v1/game?ids=id1,id2,id3
     */
    public List<GameClientResponseModel> getGamesByIds(List<String> gameIds) {
        if (gameIds == null || gameIds.isEmpty()) {
            return Collections.emptyList();
        }

        log.info("Fetching details for {} game IDs.", gameIds.size());

        // Parallel execution of individual getGameById calls
        List<CompletableFuture<GameClientResponseModel>> futures = gameIds.stream()
                .map(gameId -> CompletableFuture.supplyAsync(() -> {
                    try {
                        return getGameById(gameId);
                    } catch (GameNotFoundClientException e) {
                        log.warn("Game with ID {} not found while fetching batch, skipping.", gameId);
                        return null; // Or handle differently, e.g., collect errors
                    }
                }))
                .collect(Collectors.toList());

        // Wait for all futures to complete and collect results
        return futures.stream()
                .map(CompletableFuture::join)
                .filter(Objects::nonNull) // Filter out nulls if a game was not found
                .collect(Collectors.toList());

        /*
        // --- Alternative: If GameService supported batch GET ---
        // String url = UriComponentsBuilder.fromHttpUrl(gameServiceUrl)
        // .queryParam("ids", String.join(",", gameIds)).toUriString();
        // try {
        //     log.info("Fetching games by IDs: {} from URL: {}", gameIds, url);
        //     ResponseEntity<List<GameClientResponseDto>> response = restTemplate.exchange(
        //             url,
        //             HttpMethod.GET,
        //             null,
        //             new ParameterizedTypeReference<List<GameClientResponseDto>>() {});
        //     return response.getBody();
        // } catch (ResourceAccessException ex) {
        //     log.error("Error accessing game service for batch get: {}", ex.getMessage(), ex);
        //     throw new DownstreamServiceUnavailableException("Game service is unavailable for batch get.", ex);
        // } catch (Exception ex) {
        //     log.error("Unexpected error fetching games by IDs: {}", ex.getMessage(), ex);
        //     throw new DownstreamServiceUnavailableException("Unexpected error with game service during batch get.", ex);
        // }
        */
    }
}