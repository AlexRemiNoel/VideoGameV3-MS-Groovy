package com.champsoft.gamemanagement.DataAccess;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GameRepository extends JpaRepository<Game, GameId> {
    Game findGameByGameId(GameId gameId);

}
