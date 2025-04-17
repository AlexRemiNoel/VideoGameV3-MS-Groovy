package com.example.videogamev3.DownloadManagement.Presentation;

import com.example.videogamev3.DownloadManagement.DataAccess.DownloadStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DownloadRequestModel {
    String sourceUrl;
}
