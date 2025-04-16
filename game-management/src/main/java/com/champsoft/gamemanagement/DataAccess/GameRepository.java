package com.champsoft.gamemanagement.DataAccess;

import org.springframework.data.jpa.repository.JpaRepository;

public interface GameRepository extends JpaRepository<Game, String> {
    Game findGameByGameId_uuid(String gameIdUuid);
}
