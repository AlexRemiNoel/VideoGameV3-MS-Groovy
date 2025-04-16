package com.champsoft.downloadmanagement.DataAccess;

public enum DownloadStatus {
    PENDING,
    DOWNLOADING,
    PAUSED,
    COMPLETED,
    CANCELLED,
    FAILED // Added for robustness
}