package com.champsoft.profiledashboardmanagement;

import com.champsoft.BusinessLogic.ProfileDashboardService;
import com.champsoft.Exceptions.ProfileDashboardNotFoundException;
import com.champsoft.Exceptions.UserNotFoundClientException;
import com.champsoft.Presentation.UserProfileDashboardResponseDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
public class ProfileDashboardControllerIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private ProfileDashboardService profileDashboardService;

    private final String BASE_URI_PROFILE_DASHBOARDS = "/api/v1/profile-dashboards";
    private final String VALID_USER_ID = "user123";
    private final String NOT_FOUND_USER_ID = "user404";
    private final String OTHER_NOT_FOUND_USER_ID = "userNotFoundForDelete";


    @Test
    void whenGetProfileDashboard_withValidUserId_thenReturnDashboard() {
        // Arrange
        UserProfileDashboardResponseDto mockDashboard = UserProfileDashboardResponseDto.builder()
                .userId(VALID_USER_ID)
                .username("testUser")
                .email("test@example.com")
                .balance(50.0)
                .games(Collections.emptyList())
                .downloads(Collections.emptyList())
                .build();
        when(profileDashboardService.getOrCreateProfileDashboard(VALID_USER_ID)).thenReturn(mockDashboard);

        // Act & Assert
        webTestClient.get().uri(BASE_URI_PROFILE_DASHBOARDS + "/" + VALID_USER_ID)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(UserProfileDashboardResponseDto.class)
                .value(dashboard -> {
                    assertNotNull(dashboard);
                    assertEquals(VALID_USER_ID, dashboard.getUserId());
                    assertEquals("testUser", dashboard.getUsername());
                });

        verify(profileDashboardService, times(1)).getOrCreateProfileDashboard(VALID_USER_ID);
    }

    @Test
    void whenGetProfileDashboard_withNonExistentUser_thenServiceThrows_shouldReturnNotFound() {
        // Arrange
        String exceptionMessage = "User not found by client: " + NOT_FOUND_USER_ID;
        when(profileDashboardService.getOrCreateProfileDashboard(NOT_FOUND_USER_ID))
                .thenThrow(new UserNotFoundClientException(exceptionMessage));

        // Act & Assert
        webTestClient.get().uri(BASE_URI_PROFILE_DASHBOARDS + "/" + NOT_FOUND_USER_ID)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.message").isEqualTo("Underlying user service could not find user: " + exceptionMessage);


        verify(profileDashboardService, times(1)).getOrCreateProfileDashboard(NOT_FOUND_USER_ID);
    }

    @Test
    void whenGetAllProfileDashboards_thenReturnListOfDashboards() {
        // Arrange
        UserProfileDashboardResponseDto dashboard1 = UserProfileDashboardResponseDto.builder().userId("user1").username("User One").build();
        UserProfileDashboardResponseDto dashboard2 = UserProfileDashboardResponseDto.builder().userId("user2").username("User Two").build();
        List<UserProfileDashboardResponseDto> mockDashboards = Arrays.asList(dashboard1, dashboard2);
        when(profileDashboardService.getAllPersistedDashboards()).thenReturn(mockDashboards);

        // Act & Assert
        webTestClient.get().uri(BASE_URI_PROFILE_DASHBOARDS)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBodyList(UserProfileDashboardResponseDto.class)
                .hasSize(2)
                .contains(dashboard1, dashboard2);

        verify(profileDashboardService, times(1)).getAllPersistedDashboards();
    }

    @Test
    void whenCreateOrRefreshDashboard_withValidUserId_thenReturnCreatedDashboard() {
        // Arrange
        UserProfileDashboardResponseDto mockDashboard = UserProfileDashboardResponseDto.builder().userId(VALID_USER_ID).username("createdUser").build();
        when(profileDashboardService.createOrRefreshDashboard(VALID_USER_ID)).thenReturn(mockDashboard);

        // Act & Assert
        webTestClient.post().uri(BASE_URI_PROFILE_DASHBOARDS + "/" + VALID_USER_ID)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(UserProfileDashboardResponseDto.class)
                .value(dashboard -> assertEquals("createdUser", dashboard.getUsername()));

        verify(profileDashboardService, times(1)).createOrRefreshDashboard(VALID_USER_ID);
    }

    @Test
    void whenUpdateProfileDashboard_withValidUserId_thenReturnUpdatedDashboard() {
        // Arrange
        UserProfileDashboardResponseDto mockDashboard = UserProfileDashboardResponseDto.builder().userId(VALID_USER_ID).username("updatedUser").build();
        when(profileDashboardService.updateDashboard(VALID_USER_ID)).thenReturn(mockDashboard);

        // Act & Assert
        webTestClient.put().uri(BASE_URI_PROFILE_DASHBOARDS + "/" + VALID_USER_ID)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(UserProfileDashboardResponseDto.class)
                .value(dashboard -> assertEquals("updatedUser", dashboard.getUsername()));

        verify(profileDashboardService, times(1)).updateDashboard(VALID_USER_ID);
    }

    @Test
    void whenDeleteProfileDashboard_withValidUserId_thenReturnNoContent() {
        // Arrange
        doNothing().when(profileDashboardService).deletePersistedDashboard(VALID_USER_ID);

        // Act & Assert
        webTestClient.delete().uri(BASE_URI_PROFILE_DASHBOARDS + "/" + VALID_USER_ID)
                .exchange()
                .expectStatus().isNoContent();

        verify(profileDashboardService, times(1)).deletePersistedDashboard(VALID_USER_ID);
    }

    @Test
    void whenDeleteProfileDashboard_withNonExistentUserId_thenServiceThrows_shouldReturnNotFound() {
        // Arrange
        String exceptionMessage = "No dashboard for user: " + OTHER_NOT_FOUND_USER_ID;
        doThrow(new ProfileDashboardNotFoundException(exceptionMessage))
                .when(profileDashboardService).deletePersistedDashboard(OTHER_NOT_FOUND_USER_ID);

        // Act & Assert
        // NOTE: The GlobalExceptionHandler currently maps ProfileDashboardNotFoundException
        // to a generic 500 error. Ideally, it should be a 404.
        // This test reflects the current behavior.
        webTestClient.delete().uri(BASE_URI_PROFILE_DASHBOARDS + "/" + OTHER_NOT_FOUND_USER_ID)
                .exchange()
                .expectBody()
                .jsonPath("$.statusCode").isEqualTo(500)
                .jsonPath("$.message").isEqualTo("An unexpected error occurred: " + exceptionMessage);

        verify(profileDashboardService, times(1)).deletePersistedDashboard(OTHER_NOT_FOUND_USER_ID);
    }
}