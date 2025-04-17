package com.example.videogamev3.DownloadManagement.DataAccess;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DownloadId {
    @Column(name = "download_id")
    private String uuid;
}
