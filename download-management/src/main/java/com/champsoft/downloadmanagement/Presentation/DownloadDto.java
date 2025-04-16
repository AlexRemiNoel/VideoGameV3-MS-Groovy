package com.champsoft.downloadmanagement.Presentation;


import com.champsoft.downloadmanagement.DataAccess.DownloadManager;
import com.champsoft.downloadmanagement.DataAccess.DownloadStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;

import java.util.UUID;

@Data
@AllArgsConstructor
public class DownloadDto {
    UUID id;
    String sourceUrl;
    DownloadStatus status;

    public static DownloadDto fromEntity(DownloadManager entity) {
        return new DownloadDto(
                entity.getId(),
                entity.getSourceUrl(),
                entity.getDownloadStatus()
        );
    }
}