package com.champsoft.gamemanagement.DataAccess;

import com.champsoft.gamemanagement.DataAccess.Game;
import com.champsoft.gamemanagement.DataAccess.GameId;
import com.champsoft.gamemanagement.DataAccess.GameRepository;
import com.champsoft.gamemanagement.DataAccess.Genre;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;


@ExtendWith(SpringExtension.class)
@DataJpaTest
public class GameRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private GameRepository gameRepository;

    private Game createTestGame() {
        GameId gameId = new GameId(UUID.randomUUID().toString());
        Game game = new Game();
        game.setGameId(gameId);
        game.setTitle("Test Game");
        game.setPrice(29.99);
        game.setReleaseDate(LocalDateTime.now());
        game.setDescription("A test game description.");
        game.setPublisher("Test Publisher");
        game.setDeveloper("Test Developer");
        game.setGenre(Genre.ACTION);
        game.setUserId("testUser");
        return game;
    }

    @Test
    public void whenSaveGame_thenGameIsPersisted() {
        Game game = createTestGame();
        Game savedGame = gameRepository.save(game);
        Optional<Game> foundGame = gameRepository.findById(savedGame.getGameId());
        assertTrue(foundGame.isPresent());
        assertEquals(game.getTitle(), foundGame.get().getTitle());
    }

    @Test
    public void whenFindGameByExistingId_thenGameIsReturned() {
        Game game = createTestGame();
        entityManager.persist(game);
        entityManager.flush();

        Optional<Game> foundGame = gameRepository.findById(game.getGameId());
        assertTrue(foundGame.isPresent());
        assertEquals(game.getTitle(), foundGame.get().getTitle());
    }

    @Test
    public void whenFindGameByNonExistingId_thenEmptyOptionalIsReturned() {
        Optional<Game> foundGame = gameRepository.findById(new GameId(UUID.randomUUID().toString())); // Use GameId object
        assertFalse(foundGame.isPresent());
    }

    @Test
    public void whenFindAllGames_thenAllGamesAreReturned() {
        Game game1 = createTestGame();
        Game game2 = createTestGame();
        entityManager.persist(game1);
        entityManager.persist(game2);
        entityManager.flush();

        List<Game> allGames = gameRepository.findAll();
        assertEquals(2, allGames.size());
        assertTrue(allGames.stream().anyMatch(game -> game.getTitle().equals(game1.getTitle())));
        assertTrue(allGames.stream().anyMatch(game -> game.getTitle().equals(game2.getTitle())));
    }

    @Test
    public void whenDeleteGameById_thenGameIsNoLongerPresent() {
        Game game = createTestGame();
        entityManager.persist(game);
        entityManager.flush();

        gameRepository.deleteById(game.getGameId());
        Optional<Game> foundGame = gameRepository.findById(game.getGameId());
        assertFalse(foundGame.isPresent());
    }

    @Test
    public void whenFindGameByExistingUuid_thenGameIsReturned() {
        Game game = createTestGame();
        entityManager.persist(game);
        entityManager.flush();

        Game foundGame = gameRepository.findGameByGameId_uuid(new GameId(game.getGameId().getUuid()));
        assertNotNull(foundGame);
        assertEquals(game.getTitle(), foundGame.getTitle());
    }

    @Test
    public void whenFindGameByNonExistingUuid_thenNullIsReturned() {
        Game foundGame = gameRepository.findGameByGameId_uuid(new GameId(UUID.randomUUID().toString()));
        assertNull(foundGame);
    }
}