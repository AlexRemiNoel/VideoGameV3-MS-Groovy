package com.champsoft.usermanagement.DataAccess;

import com.example.videogamev3.GameManagement.DataAccess.Game;
import com.example.videogamev3.GameManagement.DataAccess.Review;
import com.example.videogamev3.PurchaseManagement.DataAccess.Order;
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
    @JsonManagedReference
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<Order> orders;
    @JsonManagedReference
    @OneToMany(mappedBy="user", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private List<Game> games;
}
