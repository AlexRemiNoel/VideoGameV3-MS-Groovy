package com.champsoft.DomainClient.Dtos;

import lombok.Data;

@Data
public class DownloadClientResponseDto { // Mirrors DownloadResponseModel + additions
    private String id;
    private String sourceUrl;
    private String status;
    private String userId;
}