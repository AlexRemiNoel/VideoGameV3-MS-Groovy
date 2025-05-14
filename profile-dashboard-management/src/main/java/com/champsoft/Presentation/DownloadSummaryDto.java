package com.champsoft.Presentation;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DownloadSummaryDto { // A subset of DownloadResponseModel
    private String downloadId;
    private String sourceUrl; // Or perhaps a game title if downloads are tied to games
    private String status;
    private String gameTitle; // If a download is for a specific game
}