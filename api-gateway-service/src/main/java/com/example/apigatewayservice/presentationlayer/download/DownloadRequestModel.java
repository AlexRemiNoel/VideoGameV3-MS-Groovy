package com.example.apigatewayservice.presentationlayer.download;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder

public class DownloadRequestModel {
    String sourceUrl;
}
