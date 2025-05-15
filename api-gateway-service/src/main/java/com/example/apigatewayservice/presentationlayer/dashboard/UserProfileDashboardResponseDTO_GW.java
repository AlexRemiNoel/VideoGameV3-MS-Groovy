package com.example.apigatewayservice.presentationlayer.dashboard;

import lombok.*;

import java.util.List;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false) // Important for RepresentationModel
public class UserProfileDashboardResponseDTO_GW {
    private String userId;
    private String username;
    private String email;
    private double balance;
    private List<GameSummaryDTO_GW> games;
    private List<DownloadSummaryDTO_GW> downloads;
}