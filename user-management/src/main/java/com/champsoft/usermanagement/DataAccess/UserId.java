package com.champsoft.usermanagement.DataAccess;


import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserId {
    @Column(name = "user_id") // Map uuid to game_id column
    private String uuid;
}
