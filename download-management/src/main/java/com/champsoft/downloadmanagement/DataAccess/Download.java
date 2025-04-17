package com.example.videogamev3.DownloadManagement.DataAccess;

import com.example.videogamev3.DownloadManagement.DataAccess.DownloadId;
import jakarta.persistence.*;
import lombok.Data; // Includes @Getter, @Setter, @ToString, @EqualsAndHashCode, @RequiredArgsConstructor
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

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