package com.example.videogamev3.DownloadManagement.DataAccess;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "downloads")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Download {

    @Id
    private DownloadId id;

    @Column(nullable = false)
    private String sourceUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DownloadStatus downloadStatus;
}