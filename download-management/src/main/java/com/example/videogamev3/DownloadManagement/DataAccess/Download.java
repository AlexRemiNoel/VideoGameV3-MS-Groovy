package com.example.videogamev3.DownloadManagement.DataAccess;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "downloads")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Download {
    private DownloadId id;
    private String sourceUrl;
    private DownloadStatus downloadStatus;
    private String userId;
}