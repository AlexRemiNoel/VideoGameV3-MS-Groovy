package com.example.apigatewayservice.businesslogiclayer.dashboard;

import com.example.apigatewayservice.DomainClientLayer.dashboard.ProfileDashboardServiceClient;
import com.example.apigatewayservice.presentationlayer.dashboard.UserProfileDashboardResponseDTO_GW;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProfileDashboardServiceImpl_GWTest {

    @Mock
    private ProfileDashboardServiceClient dashboardServiceClient;

    @InjectMocks
    private ProfileDashboardServiceImpl_GW dashboardService;

    private UserProfileDashboardResponseDTO_GW sampleDashboardDTO;
    private final String VALID_USER_ID = "user123";

    @BeforeEach
    void setUp() {
        // Assuming UserProfileDashboardResponseDTO_GW has a builder or appropriate constructor
        // If it has @Builder:
        sampleDashboardDTO = UserProfileDashboardResponseDTO_GW.builder()
                .userId(VALID_USER_ID)
                .username("testUser")
                .email("test@example.com")
                .balance(100.0)
                .games(Collections.emptyList())
                .downloads(Collections.emptyList())
                .build();
        // If using constructor, e.g.:
        // sampleDashboardDTO = new UserProfileDashboardResponseDTO_GW(VALID_USER_ID, "testUser", "test@example.com", 100.0, Collections.emptyList(), Collections.emptyList());
    }

    @Test
    void getProfileDashboardByUserId_callsClientAndAddsLinks() {
        when(dashboardServiceClient.getProfileDashboardByUserId(VALID_USER_ID)).thenReturn(sampleDashboardDTO);

        UserProfileDashboardResponseDTO_GW result = dashboardService.getProfileDashboardByUserId(VALID_USER_ID);

        assertNotNull(result);
        assertEquals(VALID_USER_ID, result.getUserId()); // Verifying addLinksToDashboard didn't nullify essential fields
        verify(dashboardServiceClient, times(1)).getProfileDashboardByUserId(VALID_USER_ID);
    }

    @Test
    void getAllProfileDashboards_callsClientAndAddsLinksToAll() {
        List<UserProfileDashboardResponseDTO_GW> dtoList = List.of(sampleDashboardDTO);
        when(dashboardServiceClient.getAllProfileDashboards()).thenReturn(dtoList);

        List<UserProfileDashboardResponseDTO_GW> results = dashboardService.getAllProfileDashboards();

        assertNotNull(results);
        assertFalse(results.isEmpty());
        assertEquals(VALID_USER_ID, results.get(0).getUserId());
        verify(dashboardServiceClient, times(1)).getAllProfileDashboards();
    }

    @Test
    void getAllProfileDashboards_whenClientReturnsEmptyList_returnsEmptyList() {
        when(dashboardServiceClient.getAllProfileDashboards()).thenReturn(Collections.emptyList());

        List<UserProfileDashboardResponseDTO_GW> results = dashboardService.getAllProfileDashboards();

        assertNotNull(results);
        assertTrue(results.isEmpty());
        verify(dashboardServiceClient, times(1)).getAllProfileDashboards();
    }

    @Test
    void createOrRefreshDashboard_callsClientAndAddsLinks() {
        when(dashboardServiceClient.createOrRefreshDashboard(VALID_USER_ID)).thenReturn(sampleDashboardDTO);

        UserProfileDashboardResponseDTO_GW result = dashboardService.createOrRefreshDashboard(VALID_USER_ID);

        assertNotNull(result);
        assertEquals(VALID_USER_ID, result.getUserId());
        verify(dashboardServiceClient, times(1)).createOrRefreshDashboard(VALID_USER_ID);
    }

    @Test
    void updateProfileDashboard_callsClientAndAddsLinks() {
        when(dashboardServiceClient.updateProfileDashboard(VALID_USER_ID)).thenReturn(sampleDashboardDTO);

        UserProfileDashboardResponseDTO_GW result = dashboardService.updateProfileDashboard(VALID_USER_ID);

        assertNotNull(result);
        assertEquals(VALID_USER_ID, result.getUserId());
        verify(dashboardServiceClient, times(1)).updateProfileDashboard(VALID_USER_ID);
    }

    @Test
    void deleteProfileDashboard_callsClient() {
        doNothing().when(dashboardServiceClient).deleteProfileDashboard(VALID_USER_ID);

        dashboardService.deleteProfileDashboard(VALID_USER_ID);

        verify(dashboardServiceClient, times(1)).deleteProfileDashboard(VALID_USER_ID);
    }

    @Test
    void addLinksToDashboard_whenDashboardIsNull_returnsNull() {
        UserProfileDashboardResponseDTO_GW result = dashboardService.getProfileDashboardByUserId("someId"); // relies on client returning null
        // To test addLinksToDashboard directly for null, we would need to make it public or use a spy
        // For now, testing through public method assuming client might return null
        when(dashboardServiceClient.getProfileDashboardByUserId("nullId")).thenReturn(null);
        UserProfileDashboardResponseDTO_GW dashboard = dashboardService.getProfileDashboardByUserId("nullId");
        assertNull(dashboard);
    }

    @Test
    void addLinksToDashboard_whenDashboardUserIdIsNull_returnsDashboard() {
        UserProfileDashboardResponseDTO_GW dashboardWithNullId = UserProfileDashboardResponseDTO_GW.builder().userId(null).build();
        when(dashboardServiceClient.getProfileDashboardByUserId("nullUserId")).thenReturn(dashboardWithNullId);
        
        UserProfileDashboardResponseDTO_GW result = dashboardService.getProfileDashboardByUserId("nullUserId");
        
        assertNotNull(result);
        assertNull(result.getUserId());
        verify(dashboardServiceClient, times(1)).getProfileDashboardByUserId("nullUserId");
    }
} 