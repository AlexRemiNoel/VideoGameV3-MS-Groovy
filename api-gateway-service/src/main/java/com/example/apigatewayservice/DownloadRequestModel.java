package com.example.apigatewayservice;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor

public class DownloadRequestModel {
    String sourceUrl;
}
