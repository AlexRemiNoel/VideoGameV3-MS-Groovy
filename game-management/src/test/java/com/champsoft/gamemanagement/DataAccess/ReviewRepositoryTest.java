package com.champsoft.gamemanagement.DataAccess;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
public class ReviewRepositoryTest {


    @Autowired
    private ReviewRepository reviewRepository;

    private Review game1;
    private Review game2;
    private ReviewId gameId1;
    private ReviewId gameId2;

    @BeforeEach
    void setUp() {
        reviewRepository.deleteAll();

        gameId1 = new ReviewId(UUID.randomUUID().toString());
        game1 = new Review();
        game1.setReviewId(gameId1);
        game1.setGame("Gmae1");
        game1.setRating("2");
        gameId2 = new ReviewId(UUID.randomUUID().toString());
        game2 = new Review();
        game2.setReviewId(gameId2);
        game2.setGame("Gmae2");
        game2.setRating("4");
    }

    @Test
    @DisplayName("Find Download By Existing UUID - Success")
    void whenGameExists_FindByUuid_ShouldReturnGame() {
        reviewRepository.save(game1);
        String existingUuid = game1.getReviewId().toString();

        Review foundGame = reviewRepository.findReviewByReviewId(new ReviewId(existingUuid));

        assertNotNull(foundGame);
        assertEquals(game1.getReviewId(), foundGame.getReviewId());
        assertEquals(game1.getGame(), foundGame.getGame());
        assertEquals(game1.getRating(), foundGame.getRating());
        assertEquals(existingUuid, foundGame.getReviewId().getUuid());
    }

    @Test
    @DisplayName("Find Download By Non-Existent UUID - Returns Null")
    void whenGameDoesNotExist_FindByUuid_ShouldReturnNull() {
        String nonExistentUuid = UUID.randomUUID().toString();

        Review foundGame = reviewRepository.findReviewByReviewId(new ReviewId(nonExistentUuid));

        assertNull(foundGame);
    }

    @Test
    @DisplayName("Save New Download - Success")
    void whenSaveNewGame_ShouldPersistGame() {

        Review savedGame = reviewRepository.save(game1);

        assertNotNull(savedGame);
        assertNotNull(savedGame.getReviewId());
        assertEquals(game1.getReviewId().getUuid(), savedGame.getReviewId().getUuid());

        Review retrievedGame = reviewRepository.findReviewByReviewId(game1.getReviewId());
        assertEquals(game1.getReviewId(), retrievedGame.getReviewId());
    }

    @Test
    @DisplayName("Find All Downloads - Success")
    void whenMultipleGamesExist_FindAll_ShouldReturnAllGames() {
        reviewRepository.save(game1);
        reviewRepository.save(game2);
        long expectedCount = 2;
        List<Review> games = reviewRepository.findAll();

        assertNotNull(games);
        assertEquals(expectedCount, games.size());

    }

    @Test
    @DisplayName("Find All Downloads When None Exist - Returns Empty List")
    void whenNoGamesExist_FindAll_ShouldReturnEmptyList() {
        long expectedCount = 0;

        List<Review> games = reviewRepository.findAll();

        assertNotNull(games);
        assertEquals(expectedCount, games.size());
        assertTrue(games.isEmpty());
    }


    @Test
    @DisplayName("Delete Download By ID - Success")
    void whenGameExists_DeleteById_ShouldRemoveDownload() {
        Review savedDownload = reviewRepository.save(game1);
        ReviewId idToDelete = savedDownload.getReviewId();
        assertTrue(reviewRepository.existsById(idToDelete), "Download should exist before deletion");

        reviewRepository.deleteById(idToDelete);
        assertFalse(reviewRepository.existsById(idToDelete), "Download should not exist after deletion");
        assertNull(reviewRepository.findReviewByReviewId(idToDelete), "Finding by UUID should return null after deletion");
    }


    @Test
    @DisplayName("Update Existing Download - Success")
    void whenUpdateExistingGame_ShouldReflectChanges() {
        Review savedGame = reviewRepository.save(game1);
        ReviewId gameId = savedGame.getReviewId();

        Review GameToUpdate = reviewRepository.findReviewByReviewId(gameId);

        GameToUpdate.setRating("awD");
        GameToUpdate.setGame("Fairy");
        reviewRepository.save(GameToUpdate);
        Review updatedGame = reviewRepository.findReviewByReviewId(gameId);

        assertEquals(gameId, updatedGame.getReviewId());
        assertEquals("awD", updatedGame.getRating());
        assertEquals("Fairy", updatedGame.getGame());
    }

    @Test
    @DisplayName("Check Download Existence By ID - Exists")
    void whenGameExists_ExistsById_ShouldReturnTrue() {
        reviewRepository.save(game1);
        ReviewId existingId = game1.getReviewId();

        boolean exists = reviewRepository.existsById(existingId);

        assertTrue(exists);
    }

    @Test
    @DisplayName("Check Download Existence By ID - Does Not Exist")
    void whenGameDoesNotExist_ExistsById_ShouldReturnFalse() {
        ReviewId nonExistentId = new ReviewId(UUID.randomUUID().toString());

        boolean exists = reviewRepository.existsById(nonExistentId);

        assertFalse(exists);
    }

    @Test
    @DisplayName("Count Downloads - Success")
    void whenMultipleGamesExist_Count_ShouldReturnCorrectNumber() {
        reviewRepository.save(game1);
        reviewRepository.save(game2);
        long expectedCount = 2;

        long actualCount = reviewRepository.count();

        assertEquals(expectedCount, actualCount);
    }
}