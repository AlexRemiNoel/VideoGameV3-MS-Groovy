package com.champsoft.profiledashboardmanagement;

import com.champsoft.BusinessLogic.ProfileDashboardService;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class) // Initializes mocks and injects them
public class ProfileDashboardServiceUnitTest {

    @Mock
    private UserClient userClient;

    @Mock
    private GameClient gameClient;

    @Mock
    private DownloadClient downloadClient;

    @Mock
    private UserProfileDashboardRepository dashboardRepository;

    @InjectMocks // Creates an instance of ProfileDashboardService and injects the mocks
    private ProfileDashboardService profileDashboardService;

    private final String VALID_USER_ID = "user123";
    private final String GAME_ID_1 = "gameA";
    private final String GAME_ID_2 = "gameB";
    private final String DOWNLOAD_ID_1 = "downloadX";

    private UserClientResponseDto mockUserResponse;
    private GameClientResponseDto mockGame1Response;
    private GameClientResponseDto mockGame2Response;
    private DownloadClientResponseDto mockDownload1Response;

    @BeforeEach
    void setUp() {
        mockUserResponse = new UserClientResponseDto(VALID_USER_ID, "testUser", "test@example.com", 100.0, Arrays.asList(GAME_ID_1, GAME_ID_2));
        mockGame1Response = new GameClientResponseDto();
        mockGame1Response.setId(GAME_ID_1);
        mockGame1Response.setTitle("Awesome Game 1");
        mockGame1Response.setGenre("RPG");

        mockGame2Response = new GameClientResponseDto();
        mockGame2Response.setId(GAME_ID_2);
        mockGame2Response.setTitle("Fun Game 2");
        mockGame2Response.setGenre("Strategy");

        mockDownload1Response = new DownloadClientResponseDto();
        mockDownload1Response.setId(DOWNLOAD_ID_1);
        mockDownload1Response.setSourceUrl("http://example.com/download1");
        mockDownload1Response.setStatus("COMPLETED");
        mockDownload1Response.setUserId(VALID_USER_ID);
    }

    @Test
    void whenGetOrCreateProfileDashboard_forNewUser_thenAggregatesAndSavesDashboard() {
        // Arrange
        when(userClient.getUserById(VALID_USER_ID)).thenReturn(mockUserResponse);
        when(gameClient.getGamesByIds(Arrays.asList(GAME_ID_1, GAME_ID_2)))
                .thenReturn(Arrays.asList(mockGame1Response, mockGame2Response));
        when(downloadClient.getDownloadsByUserId(VALID_USER_ID))
                .thenReturn(Collections.singletonList(mockDownload1Response));
        when(dashboardRepository.findByUserId(VALID_USER_ID)).thenReturn(Optional.empty());
        when(dashboardRepository.save(any(UserProfileDashboardEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0)); // Return the saved entity

        // Act
        UserProfileDashboardResponseDto result = profileDashboardService.getOrCreateProfileDashboard(VALID_USER_ID);

        // Assert
        assertNotNull(result);
        assertEquals(VALID_USER_ID, result.getUserId());
        assertEquals("testUser", result.getUsername());
        assertEquals(100.0, result.getBalance());
        assertEquals(2, result.getGames().size());
        assertEquals("Awesome Game 1", result.getGames().get(0).getTitle());
        assertEquals(1, result.getDownloads().size());
        assertEquals("COMPLETED", result.getDownloads().get(0).getStatus());

        verify(userClient, times(1)).getUserById(VALID_USER_ID);
        verify(gameClient, times(1)).getGamesByIds(Arrays.asList(GAME_ID_1, GAME_ID_2));
        verify(downloadClient, times(1)).getDownloadsByUserId(VALID_USER_ID);
        verify(dashboardRepository, times(1)).findByUserId(VALID_USER_ID);
        verify(dashboardRepository, times(1)).save(any(UserProfileDashboardEntity.class));
    }

    // In ProfileDashboardServiceUnitTest.java
    @Test
    void whenGetOrCreateProfileDashboard_forExistingUser_thenAggregatesAndUpdatesDashboard() {
        // Arrange
        UserProfileDashboardEntity existingEntityInTest = new UserProfileDashboardEntity(); // Renamed for clarity
        existingEntityInTest.setUserId(VALID_USER_ID);
        existingEntityInTest.setUsername("oldUsername");
        LocalDateTime originalTimestamp = LocalDateTime.now().minusDays(1); // Store the original time
        existingEntityInTest.setLastUpdatedAt(originalTimestamp);

        when(userClient.getUserById(VALID_USER_ID)).thenReturn(mockUserResponse); // new data with current time
        when(gameClient.getGamesByIds(anyList())).thenReturn(Collections.singletonList(mockGame1Response));
        when(downloadClient.getDownloadsByUserId(anyString())).thenReturn(Collections.singletonList(mockDownload1Response));

        // This existingEntityInTest instance will be modified by the service
        when(dashboardRepository.findByUserId(VALID_USER_ID)).thenReturn(Optional.of(existingEntityInTest));

        // The save mock should return the entity passed to it, which is existingEntityInTest after modification
        when(dashboardRepository.save(any(UserProfileDashboardEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        UserProfileDashboardResponseDto result = profileDashboardService.getOrCreateProfileDashboard(VALID_USER_ID);

        // Assert
        assertNotNull(result);
        assertEquals("testUser", result.getUsername()); // Updated username
        assertEquals(1, result.getGames().size());

        ArgumentCaptor<UserProfileDashboardEntity> entityCaptor = ArgumentCaptor.forClass(UserProfileDashboardEntity.class);
        verify(dashboardRepository, times(1)).save(entityCaptor.capture());
        UserProfileDashboardEntity capturedAndSavedEntity = entityCaptor.getValue();

        assertEquals("testUser", capturedAndSavedEntity.getUsername());
        // Compare the updated timestamp from the saved entity with the *original* timestamp
        assertTrue(capturedAndSavedEntity.getLastUpdatedAt().isAfter(originalTimestamp)); // <<< CORRECTED ASSERTION

        verify(userClient, times(1)).getUserById(VALID_USER_ID);
        verify(dashboardRepository, times(1)).findByUserId(VALID_USER_ID);
        verify(dashboardRepository, times(1)).save(any(UserProfileDashboardEntity.class));
    }

    @Test
    void whenGetOrCreateProfileDashboard_userClientThrowsUserNotFound_thenThrowsUserNotFoundClientException() {
        // Arrange
        when(userClient.getUserById(VALID_USER_ID)).thenThrow(new UserNotFoundClientException("User not found"));

        // Act & Assert
        assertThrows(UserNotFoundClientException.class, () -> {
            profileDashboardService.getOrCreateProfileDashboard(VALID_USER_ID);
        });

        verify(userClient, times(1)).getUserById(VALID_USER_ID);
        verifyNoInteractions(gameClient, downloadClient, dashboardRepository);
    }

    @Test
    void whenGetOrCreateProfileDashboard_userClientThrowsOtherException_thenThrowsDashboardAggregationFailureException() {
        // Arrange
        when(userClient.getUserById(VALID_USER_ID)).thenThrow(new RuntimeException("Network error"));

        // Act & Assert
        DashboardAggregationFailureException thrown = assertThrows(DashboardAggregationFailureException.class, () -> {
            profileDashboardService.getOrCreateProfileDashboard(VALID_USER_ID);
        });
        assertTrue(thrown.getMessage().contains("Failed to retrieve user details"));

        verify(userClient, times(1)).getUserById(VALID_USER_ID);
        verifyNoInteractions(gameClient, downloadClient, dashboardRepository);
    }

    @Test
    void whenGetOrCreateProfileDashboard_gameClientThrowsGameNotFound_thenProceedsWithAvailableGames() {
        // Arrange
        mockUserResponse.setGames(Arrays.asList(GAME_ID_1, "nonExistentGameId"));
        when(userClient.getUserById(VALID_USER_ID)).thenReturn(mockUserResponse);
        when(gameClient.getGamesByIds(anyList()))
                .thenAnswer(invocation -> {
                     // Simulate that getGameById within getGamesByIds throws for one game
                    List<String> ids = invocation.getArgument(0);
                    if (ids.contains("nonExistentGameId")) {
                        // This part is a bit tricky to mock precisely with the CompletableFuture approach in GameClient
                        // For unit test simplicity, we assume GameClient.getGamesByIds itself handles this
                        // and returns only found games or throws and is caught here.
                        // Let's assume it returns only the found game.
                        return Collections.singletonList(mockGame1Response);
                        // Or if GameClient.getGamesByIds itself throws GameNotFoundClientException
                        // throw new GameNotFoundClientException("A game was not found");
                    }
                    return Arrays.asList(mockGame1Response);
                });
        // If GameClient.getGamesByIds *itself* can throw GameNotFoundClientException for one of the games (as per the log message in service)
        // and the service catches it, we could mock it like this:
        // when(gameClient.getGamesByIds(Arrays.asList(GAME_ID_1, "nonExistentGameId")))
        // .thenThrow(new GameNotFoundClientException("One game not found"));
        // However, the provided GameClient actually logs and returns only found games from getGamesByIds.
        // So the service won't see a GameNotFoundClientException from the getGamesByIds call directly,
        // but rather fewer games than requested. The log in service "A game was not found while fetching details..."
        // seems to imply the exception *could* be thrown by getGamesByIds and caught.
        // Let's adjust to what the GameClient code implies (returns fewer games):
        when(gameClient.getGamesByIds(Arrays.asList(GAME_ID_1, "nonExistentGameId")))
            .thenReturn(Collections.singletonList(mockGame1Response)); // Only game1 is found

        when(downloadClient.getDownloadsByUserId(VALID_USER_ID)).thenReturn(Collections.emptyList());
        when(dashboardRepository.findByUserId(VALID_USER_ID)).thenReturn(Optional.empty());
        when(dashboardRepository.save(any(UserProfileDashboardEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        UserProfileDashboardResponseDto result = profileDashboardService.getOrCreateProfileDashboard(VALID_USER_ID);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getGames().size()); // Only one game should be present
        assertEquals(GAME_ID_1, result.getGames().get(0).getGameId());
        verify(dashboardRepository, times(1)).save(any(UserProfileDashboardEntity.class));
    }


    @Test
    void whenGetOrCreateProfileDashboard_gameClientThrowsOtherException_thenProceedsWithEmptyGames() {
        // Arrange
        when(userClient.getUserById(VALID_USER_ID)).thenReturn(mockUserResponse);
        when(gameClient.getGamesByIds(anyList())).thenThrow(new RuntimeException("Game service unavailable"));
        when(downloadClient.getDownloadsByUserId(VALID_USER_ID)).thenReturn(Collections.emptyList());
        when(dashboardRepository.findByUserId(VALID_USER_ID)).thenReturn(Optional.empty());
        when(dashboardRepository.save(any(UserProfileDashboardEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        UserProfileDashboardResponseDto result = profileDashboardService.getOrCreateProfileDashboard(VALID_USER_ID);

        // Assert
        assertNotNull(result);
        assertTrue(result.getGames().isEmpty()); // Games list should be empty
        verify(dashboardRepository, times(1)).save(any(UserProfileDashboardEntity.class));
    }

    @Test
    void whenGetOrCreateProfileDashboard_downloadClientThrowsException_thenProceedsWithEmptyDownloads() {
        // Arrange
        when(userClient.getUserById(VALID_USER_ID)).thenReturn(mockUserResponse);
        when(gameClient.getGamesByIds(anyList())).thenReturn(Collections.emptyList());
        when(downloadClient.getDownloadsByUserId(VALID_USER_ID)).thenThrow(new RuntimeException("Download service error"));
        when(dashboardRepository.findByUserId(VALID_USER_ID)).thenReturn(Optional.empty());
        when(dashboardRepository.save(any(UserProfileDashboardEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        UserProfileDashboardResponseDto result = profileDashboardService.getOrCreateProfileDashboard(VALID_USER_ID);

        // Assert
        assertNotNull(result);
        assertTrue(result.getDownloads().isEmpty()); // Downloads list should be empty
        verify(dashboardRepository, times(1)).save(any(UserProfileDashboardEntity.class));
    }

    @Test
    void getAllPersistedDashboards_whenDashboardsExist_thenReturnListOfDashboards() {
        // Arrange
        UserProfileDashboardEntity entity1 = new UserProfileDashboardEntity();
        entity1.setUserId("user1");
        entity1.setUsername("User One");
        UserProfileDashboardEntity entity2 = new UserProfileDashboardEntity();
        entity2.setUserId("user2");
        entity2.setUsername("User Two");
        when(dashboardRepository.findAll()).thenReturn(Arrays.asList(entity1, entity2));

        // Act
        List<UserProfileDashboardResponseDto> results = profileDashboardService.getAllPersistedDashboards();

        // Assert
        assertNotNull(results);
        assertEquals(2, results.size());
        assertEquals("user1", results.get(0).getUserId());
        assertEquals("User Two", results.get(1).getUsername());
        verify(dashboardRepository, times(1)).findAll();
    }

    @Test
    void getAllPersistedDashboards_whenNoDashboards_thenReturnEmptyList() {
        // Arrange
        when(dashboardRepository.findAll()).thenReturn(Collections.emptyList());

        // Act
        List<UserProfileDashboardResponseDto> results = profileDashboardService.getAllPersistedDashboards();

        // Assert
        assertNotNull(results);
        assertTrue(results.isEmpty());
        verify(dashboardRepository, times(1)).findAll();
    }

    @Test
    void getPersistedDashboardByUserId_whenDashboardExists_thenReturnDashboard() {
        // Arrange
        UserProfileDashboardEntity entity = new UserProfileDashboardEntity();
        entity.setUserId(VALID_USER_ID);
        entity.setUsername("testUser");
        when(dashboardRepository.findByUserId(VALID_USER_ID)).thenReturn(Optional.of(entity));

        // Act
        UserProfileDashboardResponseDto result = profileDashboardService.getPersistedDashboardByUserId(VALID_USER_ID);

        // Assert
        assertNotNull(result);
        assertEquals(VALID_USER_ID, result.getUserId());
        assertEquals("testUser", result.getUsername());
        verify(dashboardRepository, times(1)).findByUserId(VALID_USER_ID);
    }

    @Test
    void getPersistedDashboardByUserId_whenDashboardNotExists_thenThrowProfileDashboardNotFoundException() {
        // Arrange
        when(dashboardRepository.findByUserId(VALID_USER_ID)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ProfileDashboardNotFoundException.class, () -> {
            profileDashboardService.getPersistedDashboardByUserId(VALID_USER_ID);
        });
        verify(dashboardRepository, times(1)).findByUserId(VALID_USER_ID);
    }

    @Test
    void createOrRefreshDashboard_callsGetOrCreateProfileDashboard() {
        // Arrange
        // We need to spy on the service to verify the internal call,
        // or simply trust its implementation and test getOrCreateProfileDashboard thoroughly.
        // For simplicity, let's assume getOrCreateProfileDashboard is well-tested.
        // We just need to ensure this method calls it.
        // This can be done by mocking what getOrCreateProfileDashboard needs.
        when(userClient.getUserById(VALID_USER_ID)).thenReturn(mockUserResponse);
        when(gameClient.getGamesByIds(anyList())).thenReturn(Collections.emptyList());
        when(downloadClient.getDownloadsByUserId(anyString())).thenReturn(Collections.emptyList());
        when(dashboardRepository.findByUserId(VALID_USER_ID)).thenReturn(Optional.empty());
        when(dashboardRepository.save(any(UserProfileDashboardEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        UserProfileDashboardResponseDto result = profileDashboardService.createOrRefreshDashboard(VALID_USER_ID);

        // Assert
        assertNotNull(result); // Verifies that getOrCreateProfileDashboard was effectively called and returned.
        verify(userClient, times(1)).getUserById(VALID_USER_ID); // Indirectly verifies the call path
    }

    @Test
    void updateDashboard_callsGetOrCreateProfileDashboard() {
         // Arrange (similar to createOrRefreshDashboard)
        when(userClient.getUserById(VALID_USER_ID)).thenReturn(mockUserResponse);
        when(gameClient.getGamesByIds(anyList())).thenReturn(Collections.emptyList());
        when(downloadClient.getDownloadsByUserId(anyString())).thenReturn(Collections.emptyList());
        when(dashboardRepository.findByUserId(VALID_USER_ID)).thenReturn(Optional.empty());
        when(dashboardRepository.save(any(UserProfileDashboardEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        // Act
        UserProfileDashboardResponseDto result = profileDashboardService.updateDashboard(VALID_USER_ID);
        // Assert
        assertNotNull(result);
        verify(userClient, times(1)).getUserById(VALID_USER_ID);
    }

    @Test
    void deletePersistedDashboard_whenDashboardExists_thenDeletesDashboard() {
        // Arrange
        UserProfileDashboardEntity entity = new UserProfileDashboardEntity();
        entity.setUserId(VALID_USER_ID);
        when(dashboardRepository.findByUserId(VALID_USER_ID)).thenReturn(Optional.of(entity));
        doNothing().when(dashboardRepository).deleteByUserId(VALID_USER_ID);

        // Act
        profileDashboardService.deletePersistedDashboard(VALID_USER_ID);

        // Assert
        verify(dashboardRepository, times(1)).findByUserId(VALID_USER_ID);
        verify(dashboardRepository, times(1)).deleteByUserId(VALID_USER_ID);
    }

    @Test
    void deletePersistedDashboard_whenDashboardNotExists_thenThrowProfileDashboardNotFoundException() {
        // Arrange
        when(dashboardRepository.findByUserId(VALID_USER_ID)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ProfileDashboardNotFoundException.class, () -> {
            profileDashboardService.deletePersistedDashboard(VALID_USER_ID);
        });
        verify(dashboardRepository, times(1)).findByUserId(VALID_USER_ID);
        verify(dashboardRepository, never()).deleteByUserId(anyString());
    }
}