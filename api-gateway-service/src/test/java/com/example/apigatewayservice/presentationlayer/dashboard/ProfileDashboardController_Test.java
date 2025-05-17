package com.example.apigatewayservice.presentationlayer.dashboard;

import com.example.apigatewayservice.businesslogiclayer.dashboard.ProfileDashboardService;
import com.example.apigatewayservice.exception.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
public class ProfileDashboardController_Test {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private ProfileDashboardService dashboardService;

    private final String BASE_URI_PROFILE_DASHBOARDS = "/api/v1/profile-dashboards";
    private final String VALID_USER_ID = "user123";
    private final String VALID_USER_EMAIL = "test@example.com";
    private final String VALID_USERNAME = "testUser";
    private final String NOT_FOUND_USER_ID = "user999";

    private UserProfileDashboardResponseModel buildUserProfileDashboardDTO(String userId, String username, String email) {
        // This assumes UserProfileDashboardResponseDTO_GW has a builder. If not, adjust to use constructor.
        // For example: new UserProfileDashboardResponseDTO_GW(userId, username, email, 100.0, Collections.emptyList(), Collections.emptyList());
        return UserProfileDashboardResponseModel.builder()
                .userId(userId)
                .username(username)
                .email(email)
                .balance(100.0)
                .games(Collections.emptyList()) // Assuming GameSummaryDTO_GW exists
                .downloads(Collections.emptyList()) // Assuming DownloadSummaryDTO_GW exists
                .build();
    }

    @Test
    void getProfileDashboardByUserId_whenUserExists_thenReturnDashboard() {
        // Arrange
        UserProfileDashboardResponseModel expectedDashboard = buildUserProfileDashboardDTO(VALID_USER_ID, VALID_USERNAME, VALID_USER_EMAIL);

        when(dashboardService.getProfileDashboardByUserId(VALID_USER_ID)).thenReturn(expectedDashboard);

        // Act & Assert
        webTestClient.get().uri(BASE_URI_PROFILE_DASHBOARDS + "/" + VALID_USER_ID)
                .accept(MediaType.valueOf("application/hal+json"))
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith("application/hal+json")
                .expectBody(UserProfileDashboardResponseModel.class)
                .value(dashboard -> {
                    assertNotNull(dashboard);
                    assertEquals(expectedDashboard.getUserId(), dashboard.getUserId());
                    assertEquals(expectedDashboard.getUsername(), dashboard.getUsername());
                    assertEquals(expectedDashboard.getEmail(), dashboard.getEmail());
                    assertEquals(expectedDashboard.getBalance(), dashboard.getBalance());
                });

        verify(dashboardService, times(1)).getProfileDashboardByUserId(VALID_USER_ID);
    }

    @Test
    void getProfileDashboardByUserId_whenUserNotFound_thenReturnNotFound() {
        // Arrange
        String errorMessage = "Profile dashboard not found for userId: " + NOT_FOUND_USER_ID;
        when(dashboardService.getProfileDashboardByUserId(NOT_FOUND_USER_ID)).thenThrow(new NotFoundException(errorMessage));

        // Act & Assert
        webTestClient.get().uri(BASE_URI_PROFILE_DASHBOARDS + "/" + NOT_FOUND_USER_ID)
                .accept(MediaType.valueOf("application/hal+json"))
                .exchange()
                .expectStatus().isNotFound()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                .expectBody(HttpErrorInfo.class)
                .value(errorInfo -> {
                    assertNotNull(errorInfo);
                    assertEquals(HttpStatus.NOT_FOUND, errorInfo.getHttpStatus());
                    assertTrue(errorInfo.getMessage().contains(errorMessage));
                    assertEquals(BASE_URI_PROFILE_DASHBOARDS + "/" + NOT_FOUND_USER_ID, errorInfo.getPath());
                });

        verify(dashboardService, times(1)).getProfileDashboardByUserId(NOT_FOUND_USER_ID);
    }

    @Test
    void getAllProfileDashboards_whenDashboardsExist_thenReturnDashboards() {
        // Arrange
        UserProfileDashboardResponseModel dashboard1 = buildUserProfileDashboardDTO("user1", "userOne", "user1@example.com");
        UserProfileDashboardResponseModel dashboard2 = buildUserProfileDashboardDTO("user2", "userTwo", "user2@example.com");
        List<UserProfileDashboardResponseModel> expectedDashboards = List.of(dashboard1, dashboard2);

        when(dashboardService.getAllProfileDashboards()).thenReturn(expectedDashboards);

        // Act & Assert
        webTestClient.get().uri(BASE_URI_PROFILE_DASHBOARDS)
                .accept(MediaType.valueOf("application/hal+json"))
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith("application/hal+json")
                .expectBodyList(UserProfileDashboardResponseModel.class)
                .hasSize(2)
                .value(dashboards -> {
                    assertEquals(expectedDashboards.get(0).getUserId(), dashboards.get(0).getUserId());
                    assertEquals(expectedDashboards.get(1).getUserId(), dashboards.get(1).getUserId());
                });

        verify(dashboardService, times(1)).getAllProfileDashboards();
    }

    @Test
    void getAllProfileDashboards_whenNoDashboardsExist_thenReturnEmptyList() {
        when(dashboardService.getAllProfileDashboards()).thenReturn(Collections.emptyList());

        webTestClient.get().uri(BASE_URI_PROFILE_DASHBOARDS)
                .accept(MediaType.valueOf("application/hal+json"))
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith("application/hal+json")
                .expectBodyList(UserProfileDashboardResponseModel.class)
                .hasSize(0);

        verify(dashboardService, times(1)).getAllProfileDashboards();
    }

    @Test
    void createOrRefreshDashboard_whenValidUserId_thenReturnsCreatedDashboard() {
        // Arrange
        UserProfileDashboardResponseModel expectedDashboard = buildUserProfileDashboardDTO(VALID_USER_ID, VALID_USERNAME, VALID_USER_EMAIL);
        when(dashboardService.createOrRefreshDashboard(VALID_USER_ID)).thenReturn(expectedDashboard);

        // Act & Assert
        webTestClient.post().uri(BASE_URI_PROFILE_DASHBOARDS + "/" + VALID_USER_ID)
                .accept(MediaType.valueOf("application/hal+json"))
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().contentTypeCompatibleWith("application/hal+json")
                .expectBody(UserProfileDashboardResponseModel.class)
                .value(dashboard -> {
                    assertNotNull(dashboard);
                    assertEquals(expectedDashboard.getUserId(), dashboard.getUserId());
                });

        verify(dashboardService, times(1)).createOrRefreshDashboard(VALID_USER_ID);
    }

    @Test
    void updateProfileDashboard_whenUserExists_thenReturnsUpdatedDashboard() {
        // Arrange
        UserProfileDashboardResponseModel updatedDashboard = buildUserProfileDashboardDTO(VALID_USER_ID, "updatedUser", "updated@example.com");
        when(dashboardService.updateProfileDashboard(VALID_USER_ID)).thenReturn(updatedDashboard);

        // Act & Assert
        webTestClient.put().uri(BASE_URI_PROFILE_DASHBOARDS + "/" + VALID_USER_ID)
                .accept(MediaType.valueOf("application/hal+json"))
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith("application/hal+json")
                .expectBody(UserProfileDashboardResponseModel.class)
                .value(dashboard -> {
                    assertNotNull(dashboard);
                    assertEquals(updatedDashboard.getUserId(), dashboard.getUserId());
                    assertEquals(updatedDashboard.getUsername(), dashboard.getUsername());
                });

        verify(dashboardService, times(1)).updateProfileDashboard(VALID_USER_ID);
    }

    @Test
    void updateProfileDashboard_whenUserNotFound_thenReturnsNotFound() {
        // Arrange
        String errorMessage = "Cannot update. Profile dashboard not found for userId: " + NOT_FOUND_USER_ID;
        when(dashboardService.updateProfileDashboard(NOT_FOUND_USER_ID)).thenThrow(new NotFoundException(errorMessage));

        // Act & Assert
        webTestClient.put().uri(BASE_URI_PROFILE_DASHBOARDS + "/" + NOT_FOUND_USER_ID)
                .accept(MediaType.valueOf("application/hal+json"))
                .exchange()
                .expectStatus().isNotFound()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                .expectBody(HttpErrorInfo.class)
                .value(errorInfo -> {
                    assertNotNull(errorInfo);
                    assertEquals(HttpStatus.NOT_FOUND, errorInfo.getHttpStatus());
                    assertTrue(errorInfo.getMessage().contains(errorMessage));
                });

        verify(dashboardService, times(1)).updateProfileDashboard(NOT_FOUND_USER_ID);
    }

    @Test
    void deleteProfileDashboard_whenUserExists_thenReturnsNoContent() {
        // Arrange
        doNothing().when(dashboardService).deleteProfileDashboard(VALID_USER_ID);

        // Act & Assert
        webTestClient.delete().uri(BASE_URI_PROFILE_DASHBOARDS + "/" + VALID_USER_ID)
                .exchange()
                .expectStatus().isNoContent();

        verify(dashboardService, times(1)).deleteProfileDashboard(VALID_USER_ID);
    }

    @Test
    void deleteProfileDashboard_whenUserNotFound_thenReturnsNotFound() {
        // Arrange
        String errorMessage = "Cannot delete. Profile dashboard not found for userId: " + NOT_FOUND_USER_ID;
        doThrow(new NotFoundException(errorMessage)).when(dashboardService).deleteProfileDashboard(NOT_FOUND_USER_ID);

        // Act & Assert
        webTestClient.delete().uri(BASE_URI_PROFILE_DASHBOARDS + "/" + NOT_FOUND_USER_ID)
                .accept(MediaType.valueOf("application/hal+json"))
                .exchange()
                .expectStatus().isNotFound()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                .expectBody(HttpErrorInfo.class)
                .value(errorInfo -> {
                    assertNotNull(errorInfo);
                    assertEquals(HttpStatus.NOT_FOUND, errorInfo.getHttpStatus());
                    assertTrue(errorInfo.getMessage().contains(errorMessage));
                });

        verify(dashboardService, times(1)).deleteProfileDashboard(NOT_FOUND_USER_ID);
    }

    // --- Tests for GlobalControllerExceptionHandler --- 
    @Test
    void whenServiceThrowsDownloadNotFoundException_thenReturnsNotFound() {
        String errorMessage = "Test DownloadNotFoundException";
        when(dashboardService.getProfileDashboardByUserId(VALID_USER_ID)).thenThrow(new DownloadNotFoundException(errorMessage));

        webTestClient.get().uri(BASE_URI_PROFILE_DASHBOARDS + "/" + VALID_USER_ID)
                .accept(MediaType.valueOf("application/hal+json"))
                .exchange()
                .expectStatus().isNotFound()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                .expectBody(HttpErrorInfo.class)
                .value(errorInfo -> {
                    assertNotNull(errorInfo);
                    assertEquals(HttpStatus.NOT_FOUND, errorInfo.getHttpStatus());
                    assertTrue(errorInfo.getMessage().contains(errorMessage));
                    assertEquals(BASE_URI_PROFILE_DASHBOARDS + "/" + VALID_USER_ID, errorInfo.getPath());
                });
    }

    @Test
    void whenServiceThrowsInvalidDownloadDataException_thenReturnsBadRequest() {
        String errorMessage = "Test InvalidDownloadDataException";
        when(dashboardService.getProfileDashboardByUserId(VALID_USER_ID)).thenThrow(new InvalidDownloadDataException(errorMessage));

        webTestClient.get().uri(BASE_URI_PROFILE_DASHBOARDS + "/" + VALID_USER_ID)
                .accept(MediaType.valueOf("application/hal+json"))
                .exchange()
                .expectStatus().isBadRequest() // As per @ResponseStatus(BAD_REQUEST)
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                .expectBody(HttpErrorInfo.class)
                .value(errorInfo -> {
                    assertNotNull(errorInfo);
                    assertEquals(HttpStatus.BAD_REQUEST, errorInfo.getHttpStatus());
                    assertTrue(errorInfo.getMessage().contains(errorMessage));
                });
    }

    @Test
    void whenServiceThrowsDuplicateDownloadIDException_thenReturnsConflict() {
        String errorMessage = "Test DuplicateDownloadIDException";
        when(dashboardService.getProfileDashboardByUserId(VALID_USER_ID)).thenThrow(new DuplicateDownloadIDException(errorMessage));

        webTestClient.get().uri(BASE_URI_PROFILE_DASHBOARDS + "/" + VALID_USER_ID)
                .accept(MediaType.valueOf("application/hal+json"))
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.CONFLICT) // As per @ResponseStatus(CONFLICT)
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                .expectBody(HttpErrorInfo.class)
                .value(errorInfo -> {
                    assertNotNull(errorInfo);
                    assertEquals(HttpStatus.CONFLICT, errorInfo.getHttpStatus());
                    assertTrue(errorInfo.getMessage().contains(errorMessage));
                });
    }

    @Test
    void whenServiceThrowsGenericRuntimeException_thenReturnsInternalServerError() {
        String errorMessage = "Test Generic RuntimeException";
        // Use a direct RuntimeException, not a subclass already handled specifically
        when(dashboardService.getProfileDashboardByUserId(VALID_USER_ID)).thenThrow(new RuntimeException(errorMessage)); 

        webTestClient.get().uri(BASE_URI_PROFILE_DASHBOARDS + "/" + VALID_USER_ID)
                .accept(MediaType.valueOf("application/hal+json"))
                .exchange()
                .expectStatus().is5xxServerError()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                .expectBody(HttpErrorInfo.class)
                .value(errorInfo -> {
                    assertNotNull(errorInfo);
                    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, errorInfo.getHttpStatus());
                    assertEquals("An unexpected internal error occurred.", errorInfo.getMessage());
                });
    }

    @Test
    void whenServiceThrowsGenericException_thenReturnsInternalServerError() {
        String errorMessage = "Test Generic Exception";
        // Use a direct RuntimeException because service method doesn't throw Exception
        when(dashboardService.getProfileDashboardByUserId(VALID_USER_ID)).thenThrow(new RuntimeException(errorMessage)); 

        webTestClient.get().uri(BASE_URI_PROFILE_DASHBOARDS + "/" + VALID_USER_ID)
                .accept(MediaType.valueOf("application/hal+json"))
                .exchange()
                .expectStatus().is5xxServerError()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                .expectBody(HttpErrorInfo.class)
                .value(errorInfo -> {
                    assertNotNull(errorInfo);
                    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, errorInfo.getHttpStatus());
                    // Message for RuntimeException from GlobalExceptionHandler
                    assertEquals("An unexpected internal error occurred.", errorInfo.getMessage()); 
                });
    }

    // --- GET All Dashboards ---
//    @Test
//    void getAllProfileDashboards_thenReturnDashboards() {
//        List<UserProfileDashboardResponseDTO_GW> expectedDashboards = List.of(sampleDashboardResponse);
//        when(dashboardService.getAllProfileDashboards()).thenReturn(expectedDashboards);
//
//        webTestClient.get().uri(BASE_URI_PROFILE_DASHBOARDS)
//                .accept(MediaType.valueOf("application/hal+json"))
//                .exchange()
//                .expectStatus().isOk()
//                .expectHeader().contentTypeCompatibleWith("application/hal+json")
//                .expectBodyList(UserProfileDashboardResponseDTO_GW.class)
//                .hasSize(1);
//        verify(dashboardService, times(1)).getAllProfileDashboards();
//    }
//
//    @Test
//    void getAllProfileDashboards_withPageAndSizeParams_thenReturnDashboards() {
//        // This test is primarily to ensure Jacoco sees the endpoint called with these optional params,
//        // even if the controller doesn't currently use them to alter its logic.
//        List<UserProfileDashboardResponseDTO_GW> expectedDashboards = List.of(sampleDashboardResponse);
//        when(dashboardService.getAllProfileDashboards()).thenReturn(expectedDashboards); // Service method doesn't take page/size
//
//        webTestClient.get().uri(uriBuilder -> uriBuilder.path(BASE_URI_PROFILE_DASHBOARDS)
//                        .queryParam("page", 1)
//                        .queryParam("size", 10)
//                        .build())
//                .accept(MediaType.valueOf("application/hal+json"))
//                .exchange()
//                .expectStatus().isOk()
//                .expectHeader().contentTypeCompatibleWith("application/hal+json")
//                .expectBodyList(UserProfileDashboardResponseDTO_GW.class)
//                .hasSize(1);
//        verify(dashboardService, times(1)).getAllProfileDashboards(); // Called once, same as without params
//    }
} 