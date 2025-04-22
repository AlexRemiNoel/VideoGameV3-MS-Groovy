CREATE TABLE IF NOT EXISTS DownloadId (
    uuid VARCHAR(255) PRIMARY KEY
);

drop table if exists downloads;
CREATE TABLE IF NOT EXISTS downloads (
       download_id      VARCHAR(256) PRIMARY KEY,
       source_url      VARCHAR(2048) NOT NULL,
       download_status VARCHAR(32)   NOT NULL
);