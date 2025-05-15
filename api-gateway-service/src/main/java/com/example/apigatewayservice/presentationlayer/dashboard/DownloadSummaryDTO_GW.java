package com.example.apigatewayservice.presentationlayer.dashboard;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DownloadSummaryDTO_GW {
    private String downloadId;
    private String sourceUrl;
    private String status;
    private String gameTitle;
}