CREATE TABLE IF NOT EXISTS DownloadId (
    uuid VARCHAR(255) PRIMARY KEY
);

CREATE TABLE downloads (
       download_id              UUID PRIMARY KEY,          -- Corresponds to @Id private UUID id;
       source_url      VARCHAR(2048) NOT NULL,    -- Corresponds to private String sourceUrl; VARCHAR length can be adjusted
       download_status VARCHAR(32)   NOT NULL     -- Corresponds to @Enumerated(EnumType.STRING) private DownloadStatus downloadStatus;
    -- VARCHAR length accommodates longest status string (e.g., "DOWNLOADING") plus buffer.
);