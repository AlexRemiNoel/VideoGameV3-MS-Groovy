package com.champsoft.gamemanagement.DataAccess;


import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Setter
@Getter
@Table(name = "games")
@ToString(exclude = "user") // Exclude the user field
public class Game {
    @EmbeddedId
    private GameId gameId;
    private String title;
    private double price;
    private LocalDateTime releaseDate;
    private String description;
    private String publisher;
    private String developer;
    private List<Review> reviews;
    @Enumerated(EnumType.STRING)
    private Genre genre;
    private String user;


//    @ManyToOne
//    @JoinColumns({
//            @JoinColumn(name = "game_user_id", referencedColumnName = "user_id") // Correct JoinColumn
//    })
//    @JsonBackReference
//    private User user;
}
