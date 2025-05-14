package com.example.videogamev3.DownloadManagement.Presentation;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor

public class DownloadRequestModel {
    String sourceUrl;
    String userId;
}
