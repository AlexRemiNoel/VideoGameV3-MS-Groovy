package com.example.apigatewayservice.presentationlayer;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor

public class DownloadRequestModel {
    String sourceUrl;
}
