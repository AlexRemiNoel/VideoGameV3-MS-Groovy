package com.champsoft.DataAccess;

import com.champsoft.DomainClient.Client.GameClient;
import com.champsoft.DomainClient.Dtos.DownloadClientResponseDto;
import com.champsoft.Presentation.DownloadSummaryDto;
import com.champsoft.Presentation.GameSummaryDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "user_profile_dashboards")
public class UserProfileDashboardEntity {
    @Id
    private String id;
    @Indexed(unique = true)
    private String userId; // The business key we query by

    private String username;
    private String email;
    private double balance;

    private List<GameSummaryDto> games; // Re-using DTO from presentation for simplicity
    private List<DownloadSummaryDto> downloads; // Re-using DTO from presentation

    private LocalDateTime lastUpdatedAt;
}