package com.champsoft.BusinessLogic;


import com.champsoft.DataAccess.UserProfileDashboardEntity;
import com.champsoft.DataAccess.UserProfileDashboardRepository;
import com.champsoft.DomainClient.Client.DownloadClient;
import com.champsoft.DomainClient.Client.GameClient;
import com.champsoft.DomainClient.Client.UserClient;
import com.champsoft.DomainClient.Dtos.DownloadClientResponseDto;
import com.champsoft.DomainClient.Dtos.GameClientResponseDto;
import com.champsoft.DomainClient.Dtos.UserClientResponseDto;
import com.champsoft.Exceptions.DashboardAggregationFailureException;
import com.champsoft.Exceptions.GameNotFoundClientException;
import com.champsoft.Exceptions.ProfileDashboardNotFoundException;
import com.champsoft.Exceptions.UserNotFoundClientException;
import com.champsoft.Presentation.DownloadSummaryDto;
import com.champsoft.Presentation.GameSummaryDto;
import com.champsoft.Presentation.UserProfileDashboardResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProfileDashboardService {

    private final UserClient userClient;
    private final GameClient gameClient;
    private final DownloadClient downloadClient;
    private final UserProfileDashboardRepository dashboardRepository;

    // --- Main method for GET /api/v1/profile-dashboards/{userId} ---
    public UserProfileDashboardResponseDto getOrCreateProfileDashboard(String userId) {
        // For simplicity in this example, we'll always re-fetch and update/create.
        // A more advanced version might check lastUpdatedAt for staleness.
        log.info("Fetching or creating profile dashboard for userId: {}", userId);
        UserProfileDashboardEntity entity = aggregateAndBuildDashboardEntity(userId);
        
        // Persist the newly aggregated data
        // We use findByUserId to decide if it's an update (if mongoId already exists) or insert
        Optional<UserProfileDashboardEntity> existingEntityOpt = dashboardRepository.findByUserId(userId);
        if (existingEntityOpt.isPresent()) {
            UserProfileDashboardEntity existingEntity = existingEntityOpt.get();
            // Update existing entity fields and preserve its mongoId
            updateExistingEntity(existingEntity, entity); // entity here is the newly aggregated one
            entity = dashboardRepository.save(existingEntity);
            log.info("Updated persisted profile dashboard for userId: {}", userId);
        } else {
            entity = dashboardRepository.save(entity); // Save new entity
            log.info("Created and persisted new profile dashboard for userId: {}", userId);
        }
        return convertToResponseDto(entity);
    }

    private UserProfileDashboardEntity aggregateAndBuildDashboardEntity(String userId) {
        // 1. Fetch User Details
        UserClientResponseDto userDetails;
        try {
            userDetails = userClient.getUserById(userId);
        } catch (UserNotFoundClientException e) {
            log.warn("User not found by client for userId: {}. Cannot build dashboard.", userId);
            throw e; // Re-throw to be caught by GlobalExceptionHandler
        } catch (Exception e) {
            log.error("Failed to fetch user details for userId: {}", userId, e);
            throw new DashboardAggregationFailureException("Failed to retrieve user details for dashboard.", e);
        }

        // 2. Fetch Game Details
        List<GameSummaryDto> gameSummaries = new ArrayList<>();
        if (userDetails.getGames() != null && !userDetails.getGames().isEmpty()) {
            try {
                List<GameClientResponseDto> games = gameClient.getGamesByIds(userDetails.getGames());
                gameSummaries = games.stream()
                        .map(this::convertToGameSummaryDto)
                        .collect(Collectors.toList());
            } catch (GameNotFoundClientException e) {
                // Decide: either throw an error or continue with partial data
                log.warn("A game was not found while fetching details for user {}: {}. Proceeding with available games.", userId, e.getMessage());
                // If strict, re-throw or throw DashboardAggregationFailureException
                // For now, we log and continue, some games might be missing from the list
            } catch (Exception e) {
                log.error("Failed to fetch game details for userId: {}", userId, e);
                // Optionally, allow dashboard with missing games or throw error
                // throw new DashboardAggregationFailureException("Failed to retrieve game details.", e);
            }
        }

        // 3. Fetch Downloads
        List<DownloadSummaryDto> downloadSummaries = new ArrayList<>();
        try {
            // CRITICAL: Assumes DownloadClient.getDownloadsByUserId(userId) is implemented
            // and DownloadService has the endpoint /api/v1/downloads/user/{userId} or similar
            List<DownloadClientResponseDto> userDownloads = downloadClient.getDownloadsByUserId(userId);
            downloadSummaries = userDownloads.stream()
                    .map(this::convertToDownloadSummaryDto)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Failed to fetch download details for userId: {}", userId, e);
            // Optionally, allow dashboard with missing downloads or throw error
            // throw new DashboardAggregationFailureException("Failed to retrieve download details.", e);
        }

        // 4. Assemble the Entity for persistence
        UserProfileDashboardEntity entity = new UserProfileDashboardEntity();
        entity.setUserId(userDetails.getUserId());
        entity.setUsername(userDetails.getUsername());
        entity.setEmail(userDetails.getEmail());
        entity.setBalance(userDetails.getBalance());
        entity.setGames(gameSummaries);
        entity.setDownloads(downloadSummaries);
        entity.setLastUpdatedAt(LocalDateTime.now());
        return entity;
    }
    
    // Helper to update an existing entity with new data, preserving its MongoDB ID
    private void updateExistingEntity(UserProfileDashboardEntity existing, UserProfileDashboardEntity newData) {
        existing.setUsername(newData.getUsername());
        existing.setEmail(newData.getEmail());
        existing.setBalance(newData.getBalance());
        existing.setGames(newData.getGames());
        existing.setDownloads(newData.getDownloads());
        existing.setLastUpdatedAt(newData.getLastUpdatedAt());
    }


    // --- Other CRUD operations for the persisted dashboard (AI5) ---

    public List<UserProfileDashboardResponseDto> getAllPersistedDashboards() {
        return dashboardRepository.findAll().stream()
                .map(this::convertToResponseDto)
                .collect(Collectors.toList());
    }

    public UserProfileDashboardResponseDto getPersistedDashboardByUserId(String userId) {
        return dashboardRepository.findByUserId(userId)
                .map(this::convertToResponseDto)
                .orElseThrow(() -> new ProfileDashboardNotFoundException("No persisted dashboard found for userId: " + userId));
    }

    // POST: To explicitly request creation/refresh. Could take a simple DTO with just userId.
    // For now, it will behave like getOrCreateProfileDashboard.
    public UserProfileDashboardResponseDto createOrRefreshDashboard(String userId) {
        log.info("Explicitly creating or refreshing dashboard for userId: {}", userId);
        // The logic is the same as getOrCreateProfileDashboard for this design
        return getOrCreateProfileDashboard(userId);
    }

    // PUT: Also for explicit refresh.
    public UserProfileDashboardResponseDto updateDashboard(String userId) {
         log.info("Explicitly updating dashboard for userId: {}", userId);
        // The logic is the same as getOrCreateProfileDashboard for this design
        return getOrCreateProfileDashboard(userId);
    }

    public void deletePersistedDashboard(String userId) {
        Optional<UserProfileDashboardEntity> entityOpt = dashboardRepository.findByUserId(userId);
        if (entityOpt.isPresent()) {
            dashboardRepository.deleteByUserId(userId);
            log.info("Deleted persisted dashboard for userId: {}", userId);
        } else {
            log.warn("Attempted to delete non-existent persisted dashboard for userId: {}", userId);
            throw new ProfileDashboardNotFoundException("No persisted dashboard to delete for userId: " + userId);
        }
    }


    // --- DTO Conversion Helpers ---
    private UserProfileDashboardResponseDto convertToResponseDto(UserProfileDashboardEntity entity) {
        if (entity == null) return null;
        UserProfileDashboardResponseDto dto = new UserProfileDashboardResponseDto();
        dto.setUserId(entity.getUserId());
        dto.setUsername(entity.getUsername());
        dto.setEmail(entity.getEmail());
        dto.setBalance(entity.getBalance());
        dto.setGames(entity.getGames()); // Assumes GameSummaryDto is directly usable
        dto.setDownloads(entity.getDownloads()); // Assumes DownloadSummaryDto is directly usable
        // dto.setLastUpdatedAt(entity.getLastUpdatedAt()); // Optionally include in response
        return dto;
    }

    private GameSummaryDto convertToGameSummaryDto(GameClientResponseDto game) {
        if (game == null) return null;
        GameSummaryDto summary = new GameSummaryDto();
        summary.setGameId(game.getId());
        summary.setTitle(game.getTitle());
        summary.setGenre(game.getGenre());
        return summary;
    }

    private DownloadSummaryDto convertToDownloadSummaryDto(DownloadClientResponseDto download) {
        if (download == null) return null;
        DownloadSummaryDto summary = new DownloadSummaryDto();
        summary.setDownloadId(download.getId());
        summary.setSourceUrl(download.getSourceUrl());
        summary.setStatus(download.getStatus());
        // Potentially map gameId from download to a gameTitle if DownloadClientResponseDto has gameId
        // and you want to perform another lookup or if gameTitle is part of DownloadClientResponseDto
        return summary;
    }
}