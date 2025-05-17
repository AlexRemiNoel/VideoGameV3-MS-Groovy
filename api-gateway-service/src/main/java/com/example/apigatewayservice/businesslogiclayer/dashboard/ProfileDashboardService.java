package com.example.apigatewayservice.businesslogiclayer.dashboard;

import com.example.apigatewayservice.presentationlayer.dashboard.UserProfileDashboardResponseModel;

import java.util.List;

public interface ProfileDashboardService {
    UserProfileDashboardResponseModel getProfileDashboardByUserId(String userId);
    List<UserProfileDashboardResponseModel> getAllProfileDashboards();
    UserProfileDashboardResponseModel createOrRefreshDashboard(String userId);
    UserProfileDashboardResponseModel updateProfileDashboard(String userId);
    void deleteProfileDashboard(String userId);
}