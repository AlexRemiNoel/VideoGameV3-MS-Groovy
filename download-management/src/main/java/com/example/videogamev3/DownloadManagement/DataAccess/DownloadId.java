package com.example.videogamev3.DownloadManagement.DataAccess;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
public class DownloadId {
    private String uuid;
    public DownloadId() {
        this.uuid = UUID.randomUUID().toString();
    }
}


