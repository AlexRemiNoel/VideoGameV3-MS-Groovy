package com.example.videogamev3.DownloadManagement.Presentation;

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
    String userId;
}