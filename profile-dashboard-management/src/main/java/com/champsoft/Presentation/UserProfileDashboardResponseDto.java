package com.champsoft.Presentation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileDashboardResponseDto {
    private String userId;
    private String username;
    private String email;
    private double balance;
    private List<GameSummaryDto> games;
    private List<DownloadSummaryDto> downloads;
}
