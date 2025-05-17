package com.champsoft.profiledashboardmanagement;

import com.champsoft.BusinessLogic.ProfileDashboardService;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProfileDashboardServiceTest {

    @Mock
    private UserClient userClient;
    @Mock
    private GameClient gameClient;
    @Mock
    private DownloadClient downloadClient;
    @Mock
    private UserProfileDashboardRepository dashboardRepository;

    @InjectMocks
    private ProfileDashboardService profileDashboardService;

    @Captor
    private ArgumentCaptor<UserProfileDashboardEntity> entityArgumentCaptor;

    private String testUserId;
    private UserClientResponseModel userDetailsDto;
    private GameClientResponseModel game1Dto, game2Dto;
    private DownloadClientResponseModel download1Dto, download2Dto;

    @BeforeEach
    void setUp() {
        testUserId = "user123";

        userDetailsDto = new UserClientResponseModel(
                testUserId, "testUser", "test@example.com", 100.0, Arrays.asList("gameId1", "gameId2")
        );

        game1Dto = new GameClientResponseModel();
        game1Dto.setId("gameId1");
        game1Dto.setTitle("Awesome Game 1");
        game1Dto.setGenre("Adventure");

        game2Dto = new GameClientResponseModel();
        game2Dto.setId("gameId2");
        game2Dto.setTitle("Epic Quest 2");
        game2Dto.setGenre("RPG");

        download1Dto = new DownloadClientResponseModel();
        download1Dto.setId("downloadId1");
        download1Dto.setSourceUrl("http://example.com/download1");
        download1Dto.setStatus("COMPLETED");
        download1Dto.setUserId(testUserId);


        download2Dto = new DownloadClientResponseModel();
        download2Dto.setId("downloadId2");
        download2Dto.setSourceUrl("http://example.com/download2");
        download2Dto.setStatus("PENDING");
        download2Dto.setUserId(testUserId);
    }

    private UserProfileDashboardEntity buildPersistedEntity(String id, String userId, LocalDateTime lastUpdatedAt) {
        UserProfileDashboardEntity entity = new UserProfileDashboardEntity();
        entity.setId(id);
        entity.setUserId(userId);
        entity.setUsername(userDetailsDto.getUsername() + (id.equals("existingDBId") ? "_updated" : "")); // slight diff for update
        entity.setEmail(userDetailsDto.getEmail());
        entity.setBalance(userDetailsDto.getBalance());
        entity.setGames(Arrays.asList(
                new GameSummaryDto("gameId1", "Awesome Game 1", "Adventure"),
                new GameSummaryDto("gameId2", "Epic Quest 2", "RPG")
        ));
        entity.setDownloads(Arrays.asList(
                new DownloadSummaryDto("downloadId1", "http://example.com/download1", "COMPLETED", null),
                new DownloadSummaryDto("downloadId2", "http://example.com/download2", "PENDING", null)
        ));
        entity.setLastUpdatedAt(lastUpdatedAt != null ? lastUpdatedAt : LocalDateTime.now().minusHours(1)); // ensure different if not specified
        return entity;
    }


    @Nested
    @DisplayName("getOrCreateProfileDashboard Tests")
    class GetOrCreateProfileDashboardTests {

        @Test
        @DisplayName("Should create and persist new dashboard when none exists")
        void whenNoExistingDashboard_thenCreatesAndPersistsNew() {
            // Arrange
            when(userClient.getUserById(testUserId)).thenReturn(userDetailsDto);
            when(gameClient.getGamesByIds(userDetailsDto.getGames())).thenReturn(Arrays.asList(game1Dto, game2Dto));
            when(downloadClient.getDownloadsByUserId(testUserId)).thenReturn(Arrays.asList(download1Dto, download2Dto));
            when(dashboardRepository.findByUserId(testUserId)).thenReturn(Optional.empty());
            when(dashboardRepository.save(any(UserProfileDashboardEntity.class)))
                    .thenAnswer(invocation -> {
                        UserProfileDashboardEntity entityToSave = invocation.getArgument(0);
                        entityToSave.setId("newDBId"); // Simulate ID generation on save
                        return entityToSave;
                    });

            // Act
            UserProfileDashboardResponseDto responseDto = profileDashboardService.getOrCreateProfileDashboard(testUserId);

            // Assert
            assertNotNull(responseDto);
            assertEquals(testUserId, responseDto.getUserId());
            assertEquals(userDetailsDto.getUsername(), responseDto.getUsername());
            assertEquals(userDetailsDto.getEmail(), responseDto.getEmail());
            assertEquals(userDetailsDto.getBalance(), responseDto.getBalance());
            assertEquals(2, responseDto.getGames().size());
            assertEquals("Awesome Game 1", responseDto.getGames().get(0).getTitle());
            assertEquals(2, responseDto.getDownloads().size());
            assertEquals("http://example.com/download1", responseDto.getDownloads().get(0).getSourceUrl());

            verify(dashboardRepository).save(entityArgumentCaptor.capture());
            UserProfileDashboardEntity savedEntity = entityArgumentCaptor.getValue();
            assertNull(savedEntity.getId()); // ID should be null before save, then generated by mock
            assertNotNull(savedEntity.getLastUpdatedAt());
            assertEquals(testUserId, savedEntity.getUserId());
        }

        @Test
        @DisplayName("Should update and persist existing dashboard")
        void whenDashboardExists_thenUpdatesAndPersists() {
            // Arrange
            UserProfileDashboardEntity existingEntity = buildPersistedEntity("existingDBId", testUserId, LocalDateTime.now().minusDays(1));
            LocalDateTime initialLastUpdatedAt = existingEntity.getLastUpdatedAt();

            // New user details (e.g., username changed)
            UserClientResponseModel updatedUserDetailsDto = new UserClientResponseModel(
                    testUserId, "testUser_UPDATED", "test_updated@example.com", 150.0, Arrays.asList("gameId1") // Only one game now
            );
            GameClientResponseModel onlyGameDto = new GameClientResponseModel();
            onlyGameDto.setId("gameId1");
            onlyGameDto.setTitle("Awesome Game 1");
            onlyGameDto.setGenre("Adventure");

            when(userClient.getUserById(testUserId)).thenReturn(updatedUserDetailsDto);
            when(gameClient.getGamesByIds(updatedUserDetailsDto.getGames())).thenReturn(Collections.singletonList(onlyGameDto));
            when(downloadClient.getDownloadsByUserId(testUserId)).thenReturn(Collections.singletonList(download1Dto)); // Only one download
            when(dashboardRepository.findByUserId(testUserId)).thenReturn(Optional.of(existingEntity));
            when(dashboardRepository.save(any(UserProfileDashboardEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));


            // Act
            UserProfileDashboardResponseDto responseDto = profileDashboardService.getOrCreateProfileDashboard(testUserId);

            // Assert
            assertNotNull(responseDto);
            assertEquals(testUserId, responseDto.getUserId());
            assertEquals("testUser_UPDATED", responseDto.getUsername());
            assertEquals("test_updated@example.com", responseDto.getEmail());
            assertEquals(150.0, responseDto.getBalance());
            assertEquals(1, responseDto.getGames().size());
            assertEquals("Awesome Game 1", responseDto.getGames().get(0).getTitle());
            assertEquals(1, responseDto.getDownloads().size());

            verify(dashboardRepository).save(entityArgumentCaptor.capture());
            UserProfileDashboardEntity savedEntity = entityArgumentCaptor.getValue();
            assertEquals("existingDBId", savedEntity.getId()); // Should be the same ID
            assertEquals("testUser_UPDATED", savedEntity.getUsername());
            assertTrue(savedEntity.getLastUpdatedAt().isAfter(initialLastUpdatedAt)); // lastUpdatedAt should be updated
        }

        @Test
        @DisplayName("Should throw UserNotFoundClientException if user client cannot find user")
        void whenUserClientThrowsUserNotFound_thenPropagatesException() {
            // Arrange
            when(userClient.getUserById(testUserId)).thenThrow(new UserNotFoundClientException("User " + testUserId + " not found by client"));

            // Act & Assert
            UserNotFoundClientException exception = assertThrows(UserNotFoundClientException.class, () -> {
                profileDashboardService.getOrCreateProfileDashboard(testUserId);
            });
            assertEquals("User " + testUserId + " not found by client", exception.getMessage());

            verify(gameClient, never()).getGamesByIds(anyList());
            verify(downloadClient, never()).getDownloadsByUserId(anyString());
            verify(dashboardRepository, never()).findByUserId(anyString());
            verify(dashboardRepository, never()).save(any(UserProfileDashboardEntity.class));
        }

        @Test
        @DisplayName("Should throw DashboardAggregationFailureException if user client fails with generic error")
        void whenUserClientThrowsGenericError_thenThrowsDashboardAggregationFailure() {
            // Arrange
            when(userClient.getUserById(testUserId)).thenThrow(new RuntimeException("User client network error"));

            // Act & Assert
            DashboardAggregationFailureException exception = assertThrows(DashboardAggregationFailureException.class, () -> {
                profileDashboardService.getOrCreateProfileDashboard(testUserId);
            });
            assertTrue(exception.getMessage().contains("Failed to retrieve user details for dashboard."));
            assertTrue(exception.getCause() instanceof RuntimeException);
            assertEquals("User client network error", exception.getCause().getMessage());
        }

        @Test
        @DisplayName("Should proceed with empty games if GameClient throws GameNotFoundClientException")
        void whenGameClientThrowsGameNotFound_thenProceedsWithEmptyGames() {
            // Arrange
            userDetailsDto.setGames(Arrays.asList("gameId1", "nonExistentGameId"));
            when(userClient.getUserById(testUserId)).thenReturn(userDetailsDto);
            when(gameClient.getGamesByIds(userDetailsDto.getGames())).thenThrow(new GameNotFoundClientException("Game nonExistentGameId not found"));
            when(downloadClient.getDownloadsByUserId(testUserId)).thenReturn(Collections.emptyList());
            when(dashboardRepository.findByUserId(testUserId)).thenReturn(Optional.empty());
            when(dashboardRepository.save(any(UserProfileDashboardEntity.class))).thenAnswer(invocation -> {
                UserProfileDashboardEntity entityToSave = invocation.getArgument(0);
                entityToSave.setId("newDBId");
                return entityToSave;
            });

            // Act
            UserProfileDashboardResponseDto responseDto = profileDashboardService.getOrCreateProfileDashboard(testUserId);

            // Assert
            assertNotNull(responseDto);
            assertEquals(0, responseDto.getGames().size()); // Games list should be empty
            assertEquals(0, responseDto.getDownloads().size());
            verify(dashboardRepository).save(any(UserProfileDashboardEntity.class));
        }

        @Test
        @DisplayName("Should proceed with empty games if GameClient fails with generic error")
        void whenGameClientThrowsGenericError_thenProceedsWithEmptyGames() {
            // Arrange
            when(userClient.getUserById(testUserId)).thenReturn(userDetailsDto); // userDetailsDto has game IDs
            when(gameClient.getGamesByIds(anyList())).thenThrow(new RuntimeException("Game client network error"));
            when(downloadClient.getDownloadsByUserId(testUserId)).thenReturn(Collections.emptyList());
            when(dashboardRepository.findByUserId(testUserId)).thenReturn(Optional.empty());
            when(dashboardRepository.save(any(UserProfileDashboardEntity.class))).thenAnswer(invocation -> {
                UserProfileDashboardEntity entityToSave = invocation.getArgument(0);
                entityToSave.setId("newDBId");
                return entityToSave;
            });

            // Act
            UserProfileDashboardResponseDto responseDto = profileDashboardService.getOrCreateProfileDashboard(testUserId);

            // Assert
            assertNotNull(responseDto);
            assertEquals(0, responseDto.getGames().size());
            verify(dashboardRepository).save(any(UserProfileDashboardEntity.class));
        }


        @Test
        @DisplayName("Should proceed with empty downloads if DownloadClient fails with generic error")
        void whenDownloadClientThrowsGenericError_thenProceedsWithEmptyDownloads() {
            // Arrange
            when(userClient.getUserById(testUserId)).thenReturn(userDetailsDto);
            when(gameClient.getGamesByIds(anyList())).thenReturn(Arrays.asList(game1Dto, game2Dto));
            when(downloadClient.getDownloadsByUserId(testUserId)).thenThrow(new RuntimeException("Download client network error"));
            when(dashboardRepository.findByUserId(testUserId)).thenReturn(Optional.empty());
            when(dashboardRepository.save(any(UserProfileDashboardEntity.class))).thenAnswer(invocation -> {
                UserProfileDashboardEntity entityToSave = invocation.getArgument(0);
                entityToSave.setId("newDBId");
                return entityToSave;
            });

            // Act
            UserProfileDashboardResponseDto responseDto = profileDashboardService.getOrCreateProfileDashboard(testUserId);

            // Assert
            assertNotNull(responseDto);
            assertEquals(2, responseDto.getGames().size()); // Games should still be there
            assertEquals(0, responseDto.getDownloads().size()); // Downloads should be empty
            verify(dashboardRepository).save(any(UserProfileDashboardEntity.class));
        }
        
        @Test
        @DisplayName("Should not call game client if user has no game IDs")
        void whenUserHasNoGameIds_thenGameClientNotCalled() {
            // Arrange
            userDetailsDto.setGames(null); // or Collections.emptyList()
            when(userClient.getUserById(testUserId)).thenReturn(userDetailsDto);
            // No need to mock gameClient.getGamesByIds as it shouldn't be called
            when(downloadClient.getDownloadsByUserId(testUserId)).thenReturn(Collections.emptyList());
            when(dashboardRepository.findByUserId(testUserId)).thenReturn(Optional.empty());
            when(dashboardRepository.save(any(UserProfileDashboardEntity.class)))
                    .thenAnswer(invocation -> {
                        UserProfileDashboardEntity entityToSave = invocation.getArgument(0);
                        entityToSave.setId("newDBId");
                        return entityToSave;
                    });

            // Act
            UserProfileDashboardResponseDto responseDto = profileDashboardService.getOrCreateProfileDashboard(testUserId);

            // Assert
            assertNotNull(responseDto);
            assertTrue(responseDto.getGames().isEmpty());
            verify(gameClient, never()).getGamesByIds(anyList());
            verify(dashboardRepository).save(any(UserProfileDashboardEntity.class));
        }
    }

    @Nested
    @DisplayName("getAllPersistedDashboards Tests")
    class GetAllPersistedDashboardsTests {
        @Test
        @DisplayName("Should return list of DTOs when dashboards exist")
        void whenDashboardsExist_returnsListOfDtos() {
            // Arrange
            UserProfileDashboardEntity entity1 = buildPersistedEntity("db1", "user1", null);
            UserProfileDashboardEntity entity2 = buildPersistedEntity("db2", "user2", null);
            when(dashboardRepository.findAll()).thenReturn(Arrays.asList(entity1, entity2));

            // Act
            List<UserProfileDashboardResponseDto> response = profileDashboardService.getAllPersistedDashboards();

            // Assert
            assertNotNull(response);
            assertEquals(2, response.size());
            assertEquals("user1", response.get(0).getUserId());
            assertEquals("user2", response.get(1).getUserId());
        }

        @Test
        @DisplayName("Should return empty list when no dashboards exist")
        void whenNoDashboardsExist_returnsEmptyList() {
            // Arrange
            when(dashboardRepository.findAll()).thenReturn(Collections.emptyList());

            // Act
            List<UserProfileDashboardResponseDto> response = profileDashboardService.getAllPersistedDashboards();

            // Assert
            assertNotNull(response);
            assertTrue(response.isEmpty());
        }
    }

    @Nested
    @DisplayName("getPersistedDashboardByUserId Tests")
    class GetPersistedDashboardByUserIdTests {
        @Test
        @DisplayName("Should return DTO when dashboard exists for user")
        void whenDashboardExistsForUser_returnsDto() {
            // Arrange
            UserProfileDashboardEntity entity = buildPersistedEntity("db1", testUserId, null);
            when(dashboardRepository.findByUserId(testUserId)).thenReturn(Optional.of(entity));

            // Act
            UserProfileDashboardResponseDto response = profileDashboardService.getPersistedDashboardByUserId(testUserId);

            // Assert
            assertNotNull(response);
            assertEquals(testUserId, response.getUserId());
            assertEquals(entity.getUsername(), response.getUsername());
        }

        @Test
        @DisplayName("Should throw ProfileDashboardNotFoundException when no dashboard for user")
        void whenNoDashboardForUser_throwsProfileDashboardNotFoundException() {
            // Arrange
            when(dashboardRepository.findByUserId(testUserId)).thenReturn(Optional.empty());

            // Act & Assert
            ProfileDashboardNotFoundException exception = assertThrows(ProfileDashboardNotFoundException.class, () -> {
                profileDashboardService.getPersistedDashboardByUserId(testUserId);
            });
            assertEquals("No persisted dashboard found for userId: " + testUserId, exception.getMessage());
        }
    }

    @Nested
    @DisplayName("createOrRefreshDashboard and updateDashboard Tests")
    class CreateOrRefreshAndUpdateDashboardTests {

        // These methods delegate to getOrCreateProfileDashboard.
        // We can test the delegation by mocking getOrCreateProfileDashboard if we make it package-private or use a spy.
        // For simplicity here, we'll assume its full execution and rely on the tests for getOrCreateProfileDashboard.
        // A more focused test would use a spy.

        @Test
        @DisplayName("createOrRefreshDashboard should call getOrCreateProfileDashboard")
        void createOrRefreshDashboard_callsGetOrCreate() {
            // Arrange: Setup mocks for getOrCreateProfileDashboard to succeed
            when(userClient.getUserById(testUserId)).thenReturn(userDetailsDto);
            when(gameClient.getGamesByIds(userDetailsDto.getGames())).thenReturn(Arrays.asList(game1Dto, game2Dto));
            when(downloadClient.getDownloadsByUserId(testUserId)).thenReturn(Arrays.asList(download1Dto, download2Dto));
            when(dashboardRepository.findByUserId(testUserId)).thenReturn(Optional.empty());
            when(dashboardRepository.save(any(UserProfileDashboardEntity.class)))
                    .thenAnswer(invocation -> {
                        UserProfileDashboardEntity entityToSave = invocation.getArgument(0);
                        entityToSave.setId("newDBId");
                        return entityToSave;
                    });

            // Act
            UserProfileDashboardResponseDto response = profileDashboardService.createOrRefreshDashboard(testUserId);

            // Assert
            assertNotNull(response);
            assertEquals(testUserId, response.getUserId());
            // Further assertions are covered by getOrCreateProfileDashboard tests
            verify(userClient).getUserById(testUserId); // verify at least one interaction from getOrCreate
        }

        @Test
        @DisplayName("updateDashboard should call getOrCreateProfileDashboard")
        void updateDashboard_callsGetOrCreate() {
             // Arrange: Setup mocks for getOrCreateProfileDashboard to succeed
            when(userClient.getUserById(testUserId)).thenReturn(userDetailsDto);
            when(gameClient.getGamesByIds(userDetailsDto.getGames())).thenReturn(Arrays.asList(game1Dto, game2Dto));
            when(downloadClient.getDownloadsByUserId(testUserId)).thenReturn(Arrays.asList(download1Dto, download2Dto));
            when(dashboardRepository.findByUserId(testUserId)).thenReturn(Optional.empty());
            when(dashboardRepository.save(any(UserProfileDashboardEntity.class)))
                    .thenAnswer(invocation -> {
                        UserProfileDashboardEntity entityToSave = invocation.getArgument(0);
                        entityToSave.setId("newDBId");
                        return entityToSave;
                    });

            // Act
            UserProfileDashboardResponseDto response = profileDashboardService.updateDashboard(testUserId);

            // Assert
            assertNotNull(response);
            assertEquals(testUserId, response.getUserId());
            verify(userClient).getUserById(testUserId); // verify at least one interaction from getOrCreate
        }
    }

    @Nested
    @DisplayName("deletePersistedDashboard Tests")
    class DeletePersistedDashboardTests {
        @Test
        @DisplayName("Should delete dashboard when it exists")
        void whenDashboardExists_deletesSuccessfully() {
            // Arrange
            UserProfileDashboardEntity entity = buildPersistedEntity("db1", testUserId, null);
            when(dashboardRepository.findByUserId(testUserId)).thenReturn(Optional.of(entity));
            doNothing().when(dashboardRepository).deleteByUserId(testUserId);

            // Act
            assertDoesNotThrow(() -> profileDashboardService.deletePersistedDashboard(testUserId));

            // Assert
            verify(dashboardRepository).findByUserId(testUserId);
            verify(dashboardRepository).deleteByUserId(testUserId);
        }

        @Test
        @DisplayName("Should throw ProfileDashboardNotFoundException when trying to delete non-existent dashboard")
        void whenDeletingNonExistentDashboard_throwsProfileDashboardNotFoundException() {
            // Arrange
            when(dashboardRepository.findByUserId(testUserId)).thenReturn(Optional.empty());

            // Act & Assert
            ProfileDashboardNotFoundException exception = assertThrows(ProfileDashboardNotFoundException.class, () -> {
                profileDashboardService.deletePersistedDashboard(testUserId);
            });
            assertEquals("No persisted dashboard to delete for userId: " + testUserId, exception.getMessage());
            verify(dashboardRepository).findByUserId(testUserId);
            verify(dashboardRepository, never()).deleteByUserId(anyString());
        }
    }
}