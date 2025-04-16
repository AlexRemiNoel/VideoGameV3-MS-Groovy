package com.champsoft.purchasemanagement.DataAccess;


import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "carts")
public class Cart {
    @EmbeddedId
    @Column(name = "cart_id")
    private CartId cartId;

    @ElementCollection(fetch = FetchType.EAGER) // EAGER or LAZY depending on use case
    @CollectionTable(name = "shopping_cart_game_ids", joinColumns = @JoinColumn(name = "cart_id")) // Specify join table and FK column
    @Column(name = "game_id", nullable = false) // Column name for the game IDs in the join table
    private List<String> games;

    @JsonBackReference
    @ManyToOne
    @JoinColumn(name = "cart_user_id", nullable = true)
    private User user;
}
