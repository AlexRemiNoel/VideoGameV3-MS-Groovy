package com.example.apigatewayservice.businesslogiclayer.dashboard;

import com.example.apigatewayservice.DomainClientLayer.dashboard.ProfileDashboardServiceClient;
import com.example.apigatewayservice.presentationlayer.dashboard.UserProfileDashboardResponseModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;


@Slf4j
@Service
@RequiredArgsConstructor
public class ProfileDashboardServiceImpl implements ProfileDashboardService {

    private final ProfileDashboardServiceClient dashboardServiceClient;

    @Override
    public UserProfileDashboardResponseModel getProfileDashboardByUserId(String userId) {
        UserProfileDashboardResponseModel dashboard = dashboardServiceClient.getProfileDashboardByUserId(userId);
        return addLinksToDashboard(dashboard);
    }

    @Override
    public List<UserProfileDashboardResponseModel> getAllProfileDashboards() {
        List<UserProfileDashboardResponseModel> dashboards = dashboardServiceClient.getAllProfileDashboards();
        return dashboards.stream()
                .map(this::addLinksToDashboard)
                .collect(Collectors.toList());
    }

    @Override
    public UserProfileDashboardResponseModel createOrRefreshDashboard(String userId) {
        UserProfileDashboardResponseModel dashboard = dashboardServiceClient.createOrRefreshDashboard(userId);
        return addLinksToDashboard(dashboard);
    }

    @Override
    public UserProfileDashboardResponseModel updateProfileDashboard(String userId) {
        UserProfileDashboardResponseModel dashboard = dashboardServiceClient.updateProfileDashboard(userId);
        return addLinksToDashboard(dashboard);
    }

    @Override
    public void deleteProfileDashboard(String userId) {
        dashboardServiceClient.deleteProfileDashboard(userId);
    }

    private UserProfileDashboardResponseModel addLinksToDashboard(UserProfileDashboardResponseModel dashboard) {
        if (dashboard == null || dashboard.getUserId() == null) {
            return dashboard; // Avoid NPE if dashboard or its ID is null
        }

        return dashboard;
    }
}