package com.champsoft.BusinessLogic;


import com.champsoft.DataAccess.UserProfileDashboardEntity;
import com.champsoft.DataAccess.UserProfileDashboardRepository;
import com.champsoft.DomainClient.Client.DownloadClient;
import com.champsoft.DomainClient.Client.GameClient;
import com.champsoft.DomainClient.Client.UserClient;
import com.champsoft.DomainClient.Dtos.DownloadClientResponseModel;
import com.champsoft.DomainClient.Dtos.GameClientResponseModel;
import com.champsoft.DomainClient.Dtos.UserClientResponseModel;
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

    public UserProfileDashboardResponseDto getOrCreateProfileDashboard(String userId) {
        log.info("Fetching or creating profile dashboard for userId: {}", userId);
        UserProfileDashboardEntity entity = aggregateAndBuildDashboardEntity(userId);
        

        Optional<UserProfileDashboardEntity> existingEntityOpt = dashboardRepository.findByUserId(userId);
        if (existingEntityOpt.isPresent()) {
            UserProfileDashboardEntity existingEntity = existingEntityOpt.get();
            updateExistingEntity(existingEntity, entity);
            entity = dashboardRepository.save(existingEntity);
            log.info("Updated persisted profile dashboard for userId: {}", userId);
        } else {
            entity = dashboardRepository.save(entity);
            log.info("Created and persisted new profile dashboard for userId: {}", userId);
        }
        return convertToResponseDto(entity);
    }

    private UserProfileDashboardEntity aggregateAndBuildDashboardEntity(String userId) {
        UserClientResponseModel userDetails;
        try {
            userDetails = userClient.getUserById(userId);
        } catch (UserNotFoundClientException e) {
            log.warn("User not found by client for userId: {}. Cannot build dashboard.", userId);
            throw e;
        } catch (Exception e) {
            log.error("Failed to fetch user details for userId: {}", userId, e);
            throw new DashboardAggregationFailureException("Failed to retrieve user details for dashboard.", e);
        }

        List<GameSummaryDto> gameSummaries = new ArrayList<>();
        if (userDetails.getGames() != null && !userDetails.getGames().isEmpty()) {
            try {
                List<GameClientResponseModel> games = gameClient.getGamesByIds(userDetails.getGames());
                gameSummaries = games.stream()
                        .map(this::convertToGameSummaryDto)
                        .collect(Collectors.toList());
            } catch (GameNotFoundClientException e) {
                log.warn("A game was not found while fetching details for user {}: {}. Proceeding with available games.", userId, e.getMessage());

            } catch (Exception e) {
                log.error("Failed to fetch game details for userId: {}", userId, e);

            }
        }
        List<DownloadSummaryDto> downloadSummaries = new ArrayList<>();
        try {
            List<DownloadClientResponseModel> userDownloads = downloadClient.getDownloadsByUserId(userId);
            downloadSummaries = userDownloads.stream()
                    .map(this::convertToDownloadSummaryDto)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Failed to fetch download details for userId: {}", userId, e);
        }

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
    
    private void updateExistingEntity(UserProfileDashboardEntity existing, UserProfileDashboardEntity newData) {
        existing.setUsername(newData.getUsername());
        existing.setEmail(newData.getEmail());
        existing.setBalance(newData.getBalance());
        existing.setGames(newData.getGames());
        existing.setDownloads(newData.getDownloads());
        existing.setLastUpdatedAt(newData.getLastUpdatedAt());
    }


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


    public UserProfileDashboardResponseDto createOrRefreshDashboard(String userId) {
        log.info("Explicitly creating or refreshing dashboard for userId: {}", userId);
        return getOrCreateProfileDashboard(userId);
    }

    public UserProfileDashboardResponseDto updateDashboard(String userId) {
         log.info("Explicitly updating dashboard for userId: {}", userId);
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
        dto.setGames(entity.getGames());
        dto.setDownloads(entity.getDownloads());
        return dto;
    }

    private GameSummaryDto convertToGameSummaryDto(GameClientResponseModel game) {
        if (game == null) return null;
        GameSummaryDto summary = new GameSummaryDto();
        summary.setGameId(game.getId());
        summary.setTitle(game.getTitle());
        summary.setGenre(game.getGenre());
        return summary;
    }

    private DownloadSummaryDto convertToDownloadSummaryDto(DownloadClientResponseModel download) {
        if (download == null) return null;
        DownloadSummaryDto summary = new DownloadSummaryDto();
        summary.setDownloadId(download.getId());
        summary.setSourceUrl(download.getSourceUrl());
        summary.setStatus(download.getStatus());
        return summary;
    }
}