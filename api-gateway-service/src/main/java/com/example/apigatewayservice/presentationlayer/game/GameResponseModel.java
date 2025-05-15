package com.example.apigatewayservice.presentationlayer.game;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GameResponseModel {
    private String id;
    private String title;
    private double price;
    private String releaseDate;
    private String description;
    private String publisher;
    private String developer;
    private String genre;
    private List<Review> reviews;
}
