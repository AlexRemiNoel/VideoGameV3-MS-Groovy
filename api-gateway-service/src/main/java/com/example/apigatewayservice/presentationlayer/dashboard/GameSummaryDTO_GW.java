package com.example.apigatewayservice.presentationlayer.dashboard;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
// No need to extend RepresentationModel if these are just embedded data
// and don't have their own independent HATEOAS links from the gateway's perspective.
// If they did, they would also extend RepresentationModel.
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GameSummaryDTO_GW {
    private String gameId;
    private String title;
    private String genre;
}