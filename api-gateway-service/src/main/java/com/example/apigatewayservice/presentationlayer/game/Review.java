package com.example.apigatewayservice.presentationlayer.game;


import lombok.*;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder

public class Review {
    private ReviewId reviewId;
    private String comment;
    private String rating;
    private LocalDateTime timestamp;


    public Review(String s, int i) {
        reviewId = new ReviewId(s);
        rating = String.valueOf(i);
    }

}