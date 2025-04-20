package com.champsoft.gamemanagement.DataAccess;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;


@ExtendWith(SpringExtension.class)
@DataJpaTest
public class GameRepositoryTest {


    @Autowired
    private GameRepository gameRepository;

    private Game game1;
    private Game game2;
    private GameId gameId1;
    private GameId gameId2;

    @BeforeEach
    void setUp() {
        gameRepository.deleteAll();

        gameId1 = new GameId(UUID.randomUUID().toString());
        game1 = new Game();
        game1.setGameId(gameId1);
        game1.setTitle("Gmae1");
        game1.setGenre(Genre.ACTION);
        gameId2 = new GameId(UUID.randomUUID().toString());
        game2 = new Game();
        game2.setGameId(gameId2);
        game2.setTitle("Gmae2");
        game2.setGenre(Genre.ACTION);
    }

    @Test
    @DisplayName("Find Download By Existing UUID - Success")
    void whenGameExists_FindByUuid_ShouldReturnGame() {
        gameRepository.save(game1);
        String existingUuid = game1.getGameId().toString();

        Game foundGame = gameRepository.findGameByGameId(new GameId(existingUuid));

        assertNotNull(foundGame);
        assertEquals(game1.getGameId(), foundGame.getGameId());
        assertEquals(game1.getTitle(), foundGame.getTitle());
        assertEquals(game1.getGenre(), foundGame.getGenre());
        assertEquals(existingUuid, foundGame.getGameId().getUuid());
    }

    @Test
    @DisplayName("Find Download By Non-Existent UUID - Returns Null")
    void whenGameDoesNotExist_FindByUuid_ShouldReturnNull() {
        String nonExistentUuid = UUID.randomUUID().toString();

        Game foundGame = gameRepository.findGameByGameId(new GameId(nonExistentUuid));

        assertNull(foundGame);
    }

    @Test
    @DisplayName("Save New Download - Success")
    void whenSaveNewGame_ShouldPersistGame() {

        Game savedGame = gameRepository.save(game1);

        assertNotNull(savedGame);
        assertNotNull(savedGame.getGameId());
        assertEquals(game1.getGameId().getUuid(), savedGame.getGameId().getUuid());

        Game retrievedGame = gameRepository.findGameByGameId(game1.getGameId());
        assertEquals(game1.getTitle(), retrievedGame.getTitle());
    }

    @Test
    @DisplayName("Find All Downloads - Success")
    void whenMultipleGamesExist_FindAll_ShouldReturnAllGames() {
        gameRepository.save(game1);
        gameRepository.save(game2);
        long expectedCount = 2;
        List<Game> games = gameRepository.findAll();

        assertNotNull(games);
        assertEquals(expectedCount, games.size());

    }

    @Test
    @DisplayName("Find All Downloads When None Exist - Returns Empty List")
    void whenNoGamesExist_FindAll_ShouldReturnEmptyList() {
        long expectedCount = 0;

        List<Game> games = gameRepository.findAll();

        assertNotNull(games);
        assertEquals(expectedCount, games.size());
        assertTrue(games.isEmpty());
    }


    @Test
    @DisplayName("Delete Download By ID - Success")
    void whenGameExists_DeleteById_ShouldRemoveDownload() {
        Game savedDownload = gameRepository.save(game1);
        GameId idToDelete = savedDownload.getGameId();
        assertTrue(gameRepository.existsById(idToDelete), "Download should exist before deletion");

        gameRepository.deleteById(idToDelete);
        assertFalse(gameRepository.existsById(idToDelete), "Download should not exist after deletion");
        assertNull(gameRepository.findGameByGameId(idToDelete), "Finding by UUID should return null after deletion");
    }


    @Test
    @DisplayName("Update Existing Download - Success")
    void whenUpdateExistingGame_ShouldReflectChanges() {
        Game savedGame = gameRepository.save(game1);
        GameId gameId = savedGame.getGameId();

        Game GameToUpdate = gameRepository.findGameByGameId(gameId);

        GameToUpdate.setGenre(Genre.ADVENTURE);
        GameToUpdate.setTitle("Game Ready");
        gameRepository.save(GameToUpdate);
        Game updatedGame = gameRepository.findGameByGameId(gameId);

        assertEquals(gameId, updatedGame.getGameId());
        assertEquals(Genre.ADVENTURE, updatedGame.getGenre());
        assertEquals("Game Ready", updatedGame.getTitle());
    }

    @Test
    @DisplayName("Check Download Existence By ID - Exists")
    void whenGameExists_ExistsById_ShouldReturnTrue() {
        gameRepository.save(game1);
        GameId existingId = game1.getGameId();

        boolean exists = gameRepository.existsById(existingId);

        assertTrue(exists);
    }

    @Test
    @DisplayName("Check Download Existence By ID - Does Not Exist")
    void whenGameDoesNotExist_ExistsById_ShouldReturnFalse() {
        GameId nonExistentId = new GameId(UUID.randomUUID().toString());

        boolean exists = gameRepository.existsById(nonExistentId);

        assertFalse(exists);
    }

    @Test
    @DisplayName("Count Downloads - Success")
    void whenMultipleGamesExist_Count_ShouldReturnCorrectNumber() {
        gameRepository.save(game1);
        gameRepository.save(game2);
        long expectedCount = 2;

        long actualCount = gameRepository.count();

        assertEquals(expectedCount, actualCount);
    }
}