package com.champsoft.usermanagement.DataAccess;


import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "games")
public class User {
    @EmbeddedId
    private UserId userId;
    private String username;
    private String email;
    private String password;
    private double balance;


    @ElementCollection(fetch = FetchType.EAGER) // EAGER or LAZY depending on use case
    @CollectionTable(name = "user_game_ids", joinColumns = @JoinColumn(name = "user_id")) // Specify join table and FK column
    @Column(name = "game_id", nullable = false) // Column name for the game IDs in the join table
    private List<String> games;
}
