package com.example.apigatewayservice.presentationlayer.download;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
public class DownloadResponseModel {
    String id;
    String sourceUrl;
    String status;
}