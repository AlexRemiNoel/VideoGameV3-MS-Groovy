package com.example.apigatewayservice.presentationlayer.game;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReviewRequestModel {
    String comment;
    String rating;
}
