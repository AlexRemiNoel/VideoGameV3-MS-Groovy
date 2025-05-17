package com.example.apigatewayservice.presentationlayer.dashboard;

import lombok.*;

import java.util.List;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false) // Important for RepresentationModel
public class UserProfileDashboardResponseModel {
    private String userId;
    private String username;
    private String email;
    private double balance;
    private List<GameSummaryResponseModel> games;
    private List<DownloadSummaryResponseModel> downloads;
}