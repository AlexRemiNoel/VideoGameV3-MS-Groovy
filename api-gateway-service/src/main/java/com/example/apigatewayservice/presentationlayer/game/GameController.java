package com.example.apigatewayservice.presentationlayer.game;

import com.example.apigatewayservice.businesslogiclayer.game.GameService;
import com.example.apigatewayservice.exception.InvalidInputException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID; // For basic UUID validation

@Slf4j
@RestController
@RequestMapping("api/v1/games")
@RequiredArgsConstructor
public class GameController {

    private final GameService gameService;


    @GetMapping("{uuid}")
    public ResponseEntity<GameResponseModel> getGameByGameId(@PathVariable String uuid) {
        log.info("Received request to get game by UUID: {}", uuid);

        GameResponseModel game = gameService.getGameById(uuid);
        return ResponseEntity.ok(game);
    }

    @GetMapping
    public ResponseEntity<List<GameResponseModel>> getAllGames() {
        log.info("Received request to get all games");
        List<GameResponseModel> games = gameService.getAllGames();
        return ResponseEntity.ok(games);
    }

    @PostMapping
    public ResponseEntity<GameResponseModel> addGame(@RequestBody GameRequestModel gameRequestModel) {
        log.info("Received request to add a new game");
        GameResponseModel addedGame = gameService.createGame(gameRequestModel);
        return ResponseEntity.status(HttpStatus.CREATED).body(addedGame);
    }

    @PutMapping
    public ResponseEntity<GameResponseModel> updateGame(@RequestBody GameRequestModel gameRequestModel) {
        log.info("Received request to update a game");
        GameResponseModel updatedGame = gameService.updateGame(gameRequestModel);
        return ResponseEntity.ok(updatedGame);
    }

    @DeleteMapping("{uuid}")
    public ResponseEntity<Void> deleteGame(@PathVariable String uuid) {
        log.info("Received request to delete game with UUID: {}", uuid);

        gameService.deleteGame(uuid);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("review/{uuid}")
    public ResponseEntity<GameResponseModel> addReviewToGame(@PathVariable String uuid, @RequestBody ReviewRequestModel reviewRequestModel) {
        log.info("Received request to add review to game UUID: {}", uuid);

        GameResponseModel reviewedGame = gameService.addReview(reviewRequestModel, uuid);
        return ResponseEntity.ok(reviewedGame);
    }
}