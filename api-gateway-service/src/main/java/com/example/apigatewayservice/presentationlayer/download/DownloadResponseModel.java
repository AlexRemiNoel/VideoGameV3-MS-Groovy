package com.example.apigatewayservice.presentationlayer.download;

import lombok.*;

@Data
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
@Builder

public class DownloadResponseModel {
    String id;
    String sourceUrl;
    String status;
}