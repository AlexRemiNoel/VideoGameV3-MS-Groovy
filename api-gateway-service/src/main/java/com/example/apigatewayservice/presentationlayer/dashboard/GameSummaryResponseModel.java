package com.example.apigatewayservice.presentationlayer.dashboard;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
// No need to extend RepresentationModel if these are just embedded data
// and don't have their own independent HATEOAS links from the gateway's perspective.
// If they did, they would also extend RepresentationModel.
@Builder

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GameSummaryResponseModel {
    private String gameId;
    private String title;
    private String genre;
}