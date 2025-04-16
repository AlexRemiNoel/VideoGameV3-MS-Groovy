package com.champsoft.downloadmanagement.DataAccess;


import jakarta.persistence.*;
import lombok.Data; // Includes @Getter, @Setter, @ToString, @EqualsAndHashCode, @RequiredArgsConstructor
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "downloads")
@Data // Lombok: Generates getters, setters, toString, equals/hashCode
@NoArgsConstructor // JPA Requirement
@AllArgsConstructor // Convenience
public class DownloadManager {

    @Id // Use UUID directly as the primary key
    private UUID id;

    @Column(nullable = false)
    private String sourceUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DownloadStatus downloadStatus;

    // No more domain logic methods (startDownload, pauseDownload, etc.) here.
    // The service layer will handle status changes directly via setters.
}