package com.champsoft.gamemanagement.DataMapper;

import com.champsoft.gamemanagement.DataAccess.Game;
import com.champsoft.gamemanagement.DataAccess.GameId;
import com.champsoft.gamemanagement.DataAccess.Genre;
import com.champsoft.gamemanagement.DataAccess.Review;
import com.champsoft.gamemanagement.Presentation.DTOS.GameResponseModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class GameResponseMapperImplTest {

    private GameResponseMapper gameResponseMapper;

    @BeforeEach
    void setUp() {
        // Instantiate the generated mapper implementation
        gameResponseMapper = new GameResponseMapperImpl();
    }

    @Test
    void gameToGameResponseModel_shouldMapAllFieldsCorrectly() {
        // Arrange
        UUID gameUuid = UUID.randomUUID();
        Game game = new Game();
        game.setGameId(new GameId(gameUuid));
        game.setTitle("Test Game");
        game.setPrice(59.99);
        game.setDescription("A fantastic test game.");
        game.setPublisher("Test Publisher");
        game.setDeveloper("Test Developer");
        LocalDateTime releaseDate = LocalDateTime.now();
        game.setReleaseDate(releaseDate);
        game.setGenre(Genre.ACTION);
        Review rev1 =new Review("1", 5);
        rev1.setComment("Great Game!");
        Review rev2 = new Review("c", 4);
        rev2.setComment("Loved It!");
        List<Review> reviews = new ArrayList<>();
        reviews.add(rev1);
        reviews.add(rev2);

        game.setReviews(reviews);

        // Act
        GameResponseModel responseModel = gameResponseMapper.gameToGameResponseModel(game);

        // Assert
        assertNotNull(responseModel);
        // Corrected assertion: Compare the String representation of the UUID
        assertEquals(gameUuid.toString(), responseModel.getId());
        assertEquals("Test Game", responseModel.getTitle());
        assertEquals(59.99, responseModel.getPrice());
        assertEquals("A fantastic test game.", responseModel.getDescription());
        assertEquals("Test Publisher", responseModel.getPublisher());
        assertEquals("Test Developer", responseModel.getDeveloper());
        assertEquals(releaseDate.toString(), responseModel.getReleaseDate()); // MapStruct uses toString()
        assertEquals(Genre.ACTION.toString(), responseModel.getGenre()); // MapStruct uses toString()

        assertNotNull(responseModel.getReviews());
        assertEquals(2, responseModel.getReviews().size());
        assertEquals(rev1.getComment(), responseModel.getReviews().get(0).getComment());
        assertEquals("5", responseModel.getReviews().get(0).getRating());
        assertEquals(rev2.getComment(), responseModel.getReviews().get(1).getComment());
        assertEquals("4", responseModel.getReviews().get(1).getRating());
    }

    @Test
    void gameToGameResponseModel_shouldReturnNullForNullInput() {
        // Arrange
        Game game = null;

        // Act
        GameResponseModel responseModel = gameResponseMapper.gameToGameResponseModel(game);

        // Assert
        assertNull(responseModel);
    }

    @Test
    void gameToGameResponseModelList_shouldMapAllGamesCorrectly() {
        // Arrange
        List<Game> gameList = new ArrayList<>();

        UUID uuid1 = UUID.randomUUID();
        Game game1 = new Game();
        game1.setGameId(new GameId(uuid1));
        game1.setTitle("Game 1");
        game1.setPrice(10.0);
        game1.setDescription("Desc 1");
        game1.setPublisher("Pub 1");
        game1.setDeveloper("Dev 1");
        game1.setReleaseDate(LocalDateTime.now().minusDays(1));
        game1.setGenre(Genre.ADVENTURE);
        game1.setReviews(List.of(new Review("Review 1", 5)));
        gameList.add(game1);

        UUID uuid2 = UUID.randomUUID();
        Game game2 = new Game();
        game2.setGameId(new GameId(uuid2));
        game2.setTitle("Game 2");
        game2.setPrice(20.0);
        game2.setDescription("Desc 2");
        game2.setPublisher("Pub 2");
        game2.setDeveloper("Dev 2");
        game2.setReleaseDate(LocalDateTime.now());
        game2.setGenre(Genre.RPG);
        Review rev1 = new Review("A",2);
        rev1.setComment("Review A");
        Review rev2 = new Review("B",2);
        rev2.setComment("Review B");
        List<Review> reviews = new ArrayList<>();
        reviews.add(rev1);
        reviews.add(rev2);
        game2.setReviews(reviews);
        gameList.add(game2);

        // Act
        List<GameResponseModel> responseModelList = gameResponseMapper.gameToGameResponseModel(gameList);

        // Assert
        assertNotNull(responseModelList);
        assertEquals(2, responseModelList.size());

        // Verify first game
        GameResponseModel response1 = responseModelList.get(1);
        // Corrected assertion: Compare the String representation of the UUID
        assertEquals(uuid2.toString(), response1.getId());
        assertEquals("Game 2", response1.getTitle());
        assertEquals(20.0, response1.getPrice());
        assertEquals("Desc 2", response1.getDescription());
        assertEquals("Pub 2", response1.getPublisher());
        assertEquals("Dev 2", response1.getDeveloper());
        assertEquals(game2.getReleaseDate().toString(), response1.getReleaseDate());
        assertEquals(Genre.RPG.toString(), response1.getGenre());
        assertNotNull(response1.getReviews());
        assertEquals(2, response1.getReviews().size());
        assertEquals(rev1.getComment(), response1.getReviews().get(0).getComment());
        assertEquals("2", response1.getReviews().get(0).getRating());


        // Verify second game
        GameResponseModel response2 = responseModelList.get(1);
        // Corrected assertion: Compare the String representation of the UUID
        assertEquals(uuid2.toString(), response2.getId());
        assertEquals("Game 2", response2.getTitle());
        assertEquals(20.0, response2.getPrice());
        assertEquals("Desc 2", response2.getDescription());
        assertEquals("Pub 2", response2.getPublisher());
        assertEquals("Dev 2", response2.getDeveloper());
        assertEquals(game2.getReleaseDate().toString(), response2.getReleaseDate());
        assertEquals(Genre.RPG.toString(), response2.getGenre());
        assertNotNull(response2.getReviews());
        assertEquals(2, response2.getReviews().size());
        assertEquals("Review A", response2.getReviews().get(0).getComment());
        assertEquals("2", response2.getReviews().get(0).getRating());
        assertEquals("Review B", response2.getReviews().get(1).getComment());
        assertEquals("2", response2.getReviews().get(1).getRating());
    }

    @Test
    void gameToGameResponseModelList_shouldReturnNullForNullInput() {
        // Arrange
        List<Game> gameList = null;

        // Act
        List<GameResponseModel> responseModelList = gameResponseMapper.gameToGameResponseModel(gameList);

        // Assert
        assertNull(responseModelList);
    }

    @Test
    void gameToGameResponseModelList_shouldReturnEmptyListForEmptyInputList() {
        // Arrange
        List<Game> gameList = new ArrayList<>();

        // Act
        List<GameResponseModel> responseModelList = gameResponseMapper.gameToGameResponseModel(gameList);

        // Assert
        assertNotNull(responseModelList);
        assertTrue(responseModelList.isEmpty());
    }
}