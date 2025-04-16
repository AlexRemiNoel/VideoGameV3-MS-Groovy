package com.champsoft.gamemanagement.Presentation.DTOS;


import com.champsoft.gamemanagement.DataAccess.Game;
import com.champsoft.gamemanagement.DataAccess.Review;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
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
    private List<Game> userGames;
}
