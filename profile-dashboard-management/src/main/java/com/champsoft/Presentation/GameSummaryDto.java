package com.champsoft.Presentation;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GameSummaryDto { // A subset of GameResponseModel
    private String gameId;
    private String title;
    private String genre;
    // Add other essential fields for a dashboard view
}