package com.champsoft.gamemanagement.DataAccess;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Embeddable
@Data
@AllArgsConstructor
@NoArgsConstructor
public class GameId {
    @Column(name = "game_id") // Map uuid to game_id column
    private String uuid;
    public GameId(UUID id){
        uuid = String.valueOf(id);
    }
}
