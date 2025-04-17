package com.champsoft.gamemanagement.DataMapper;

import com.champsoft.gamemanagement.DataAccess.Game;
import com.champsoft.gamemanagement.DataAccess.Genre;
import com.champsoft.gamemanagement.Presentation.DTOS.GameRequestModel;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class DataRequestMapperTest {

    @Autowired
    private GameRequestMapper gameRequestMapper;

//    @Test
//    public void whenRequestModelToEntity_thenGameIdIsGenerated() {
//        // Arrange
//        GameRequestModel requestModel = new GameRequestModel();
//        requestModel.setTitle("Test Game");
//        requestModel.setPrice(25.99);
//        requestModel.setGenre("ACTION");
//
//        // Act
//        Game game = gameRequestMapper.requestModelToEntity(requestModel);
//
//        // Assert
//        assertNotNull(game.getGameId());
//        assertNotNull(game.getGameId().getUuid());
//    }

    @Test
    public void whenRequestModelToEntity_thenTitleAndPriceAreMapped() {
        // Arrange
        GameRequestModel requestModel = new GameRequestModel();
        requestModel.setTitle("Test Game");
        requestModel.setPrice(25.99);
        requestModel.setGenre("ACTION");

        // Act
        Game game = gameRequestMapper.requestModelToEntity(requestModel);

        // Assert
        assertEquals(requestModel.getTitle(), game.getTitle());
        assertEquals(requestModel.getPrice(), game.getPrice());
    }

//    @Test
//    public void whenRequestModelToEntity_thenReleaseDateIsSetToNow() {
//        // Arrange
//        GameRequestModel requestModel = new GameRequestModel();
//        requestModel.setTitle("Test Game");
//        requestModel.setPrice(25.99);
//        requestModel.setGenre("ACTION");
//
//        // Act
//        Game game = gameRequestMapper.requestModelToEntity(requestModel);
//        LocalDateTime now = LocalDateTime.now();
//
//        // Assert
//        assertNotNull(game.getReleaseDate());
//        assertTrue(ChronoUnit.SECONDS.between(game.getReleaseDate(), now) <= 1);
//    }
//
//    @Test
//    public void whenRequestModelToEntity_thenGenreIsMappedCorrectly() {
//        // Arrange
//        GameRequestModel requestModelAction = new GameRequestModel();
//        requestModelAction.setTitle("Action Game");
//        requestModelAction.setPrice(30.00);
//        requestModelAction.setGenre("ACTION");
//
//        GameRequestModel requestModelRPG = new GameRequestModel();
//        requestModelRPG.setTitle("RPG Game");
//        requestModelRPG.setPrice(40.00);
//        requestModelRPG.setGenre("RPG");
//
//        // Act
//        Game gameAction = gameRequestMapper.requestModelToEntity(requestModelAction);
//        Game gameRPG = gameRequestMapper.requestModelToEntity(requestModelRPG);
//
//        // Assert
//        assertEquals(Genre.ACTION, gameAction.getGenre());
//        assertEquals(Genre.RPG, gameRPG.getGenre());
//    }
//
//    @Test
//    public void whenRequestModelToEntity_withAllFields_thenAllAreMappedCorrectly() {
//        // Arrange
//        GameRequestModel requestModel = new GameRequestModel();
//        requestModel.setTitle("Complete Game");
//        requestModel.setPrice(59.99);
//        requestModel.setDescription("A fully featured game.");
//        requestModel.setPublisher("Big Publisher");
//        requestModel.setDeveloper("Indie Dev");
//        requestModel.setGenre("STRATEGY");
//        requestModel.setUserId("user123");
//
//        // Act
//        Game game = gameRequestMapper.requestModelToEntity(requestModel);
//        LocalDateTime now = LocalDateTime.now();
//
//        // Assert
//        assertNotNull(game.getGameId());
//        assertNotNull(game.getGameId().getUuid());
//        assertEquals(requestModel.getTitle(), game.getTitle());
//        assertEquals(requestModel.getPrice(), game.getPrice());
//        assertNotNull(game.getReleaseDate());
//        assertTrue(ChronoUnit.SECONDS.between(game.getReleaseDate(), now) <= 1);
//        assertEquals(requestModel.getDescription(), game.getDescription());
//        assertEquals(requestModel.getPublisher(), game.getPublisher());
//        assertEquals(requestModel.getDeveloper(), game.getDeveloper());
//        assertEquals(Genre.STRATEGY, game.getGenre());
//        assertEquals(requestModel.getUserId(), game.getUserId());
//    }
}