package com.example.apigatewayservice.businesslogiclayer.dashboard;

import com.example.apigatewayservice.presentationlayer.dashboard.UserProfileDashboardResponseDTO_GW;

import java.util.List;

public interface ProfileDashboardService_GW {
    UserProfileDashboardResponseDTO_GW getProfileDashboardByUserId(String userId);
    List<UserProfileDashboardResponseDTO_GW> getAllProfileDashboards();
    UserProfileDashboardResponseDTO_GW createOrRefreshDashboard(String userId);
    UserProfileDashboardResponseDTO_GW updateProfileDashboard(String userId);
    void deleteProfileDashboard(String userId);
}