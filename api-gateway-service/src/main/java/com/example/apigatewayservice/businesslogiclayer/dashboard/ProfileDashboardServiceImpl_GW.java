package com.example.apigatewayservice.businesslogiclayer.dashboard;

import com.example.apigatewayservice.DomainClientLayer.dashboard.ProfileDashboardServiceClient;
import com.example.apigatewayservice.presentationlayer.dashboard.UserProfileDashboardResponseDTO_GW;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;


@Slf4j
@Service
@RequiredArgsConstructor
public class ProfileDashboardServiceImpl_GW implements ProfileDashboardService_GW {

    private final ProfileDashboardServiceClient dashboardServiceClient;

    @Override
    public UserProfileDashboardResponseDTO_GW getProfileDashboardByUserId(String userId) {
        UserProfileDashboardResponseDTO_GW dashboard = dashboardServiceClient.getProfileDashboardByUserId(userId);
        return addLinksToDashboard(dashboard);
    }

    @Override
    public List<UserProfileDashboardResponseDTO_GW> getAllProfileDashboards() {
        List<UserProfileDashboardResponseDTO_GW> dashboards = dashboardServiceClient.getAllProfileDashboards();
        return dashboards.stream()
                .map(this::addLinksToDashboard)
                .collect(Collectors.toList());
    }

    @Override
    public UserProfileDashboardResponseDTO_GW createOrRefreshDashboard(String userId) {
        UserProfileDashboardResponseDTO_GW dashboard = dashboardServiceClient.createOrRefreshDashboard(userId);
        return addLinksToDashboard(dashboard);
    }

    @Override
    public UserProfileDashboardResponseDTO_GW updateProfileDashboard(String userId) {
        UserProfileDashboardResponseDTO_GW dashboard = dashboardServiceClient.updateProfileDashboard(userId);
        return addLinksToDashboard(dashboard);
    }

    @Override
    public void deleteProfileDashboard(String userId) {
        dashboardServiceClient.deleteProfileDashboard(userId);
    }

    private UserProfileDashboardResponseDTO_GW addLinksToDashboard(UserProfileDashboardResponseDTO_GW dashboard) {
        if (dashboard == null || dashboard.getUserId() == null) {
            return dashboard; // Avoid NPE if dashboard or its ID is null
        }

        return dashboard;
    }
}