package com.example.videogamev3.DownloadManagement.Presentation;

import com.example.videogamev3.DownloadManagement.DataAccess.Download;
import com.example.videogamev3.DownloadManagement.DataAccess.DownloadStatus;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class DownloadResponseModel {
    String id;
    String sourceUrl;
    String status;
}