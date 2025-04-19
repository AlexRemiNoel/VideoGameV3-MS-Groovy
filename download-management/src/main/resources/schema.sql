CREATE TABLE IF NOT EXISTS DownloadId (
    uuid VARCHAR(255) PRIMARY KEY
);

CREATE TABLE downloads (
       download_id      VARCHAR(256) PRIMARY KEY,
       source_url      VARCHAR(2048) NOT NULL,
       download_status VARCHAR(32)   NOT NULL
);