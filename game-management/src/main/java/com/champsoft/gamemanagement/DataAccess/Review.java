package com.champsoft.gamemanagement.DataAccess;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "reviews")
@ToString(exclude = "game")
public class Review {
    @EmbeddedId
    private ReviewId reviewId;
    private String comment;
    private String rating;
    private LocalDateTime timestamp;
    private String game;

    public Review(String s, int i) {
        reviewId = new ReviewId(s);
        rating = String.valueOf(i);
    }
}
