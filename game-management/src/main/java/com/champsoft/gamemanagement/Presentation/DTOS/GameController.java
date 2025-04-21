package com.champsoft.gamemanagement.Presentation.DTOS;


import com.champsoft.gamemanagement.BusinessLogic.GameService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("api/v1/game")
public class GameController {

    private GameService gameService;

    public GameController(GameService gameService) {
        this.gameService = gameService;
    }

    @GetMapping("{uuid}")
    public ResponseEntity<GameResponseModel> getGameByGameId_uuid(@PathVariable String uuid) {
        GameResponseModel game = gameService.getGameById(uuid);
        if (game != null) {
            return new ResponseEntity<>(game, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping()
    public ResponseEntity<List<GameResponseModel>> getAllGames() {
        List<GameResponseModel> games = gameService.getAllGames();
        return new ResponseEntity<>(games, HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<GameResponseModel> addGame(@RequestBody GameRequestModel gameRequestModel) {
        GameResponseModel addedGame = gameService.createGame(gameRequestModel);
        if (addedGame == null) {
            return ResponseEntity.badRequest().build();
        }
        return new ResponseEntity<>(addedGame, HttpStatus.CREATED);
    }


    @PutMapping("{uuid}")
    public ResponseEntity<GameResponseModel> updateGame(@RequestBody GameRequestModel gameRequestModel, @PathVariable String uuid) {
        GameResponseModel updatedGame = gameService.updateGame(gameRequestModel);
        if(updatedGame != null){
            return new ResponseEntity<>(updatedGame, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("{uuid}")
    public ResponseEntity<GameResponseModel> deleteGame(@PathVariable String uuid) {
        GameResponseModel deletedGame = gameService.deleteGame(uuid);
        if (deletedGame != null) {
            return new ResponseEntity<>(deletedGame, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping("review/{uuid}")
    public ResponseEntity<GameResponseModel> reviewGame(@PathVariable String uuid, @RequestBody ReviewRequestModel reviewRequestModel) {
        GameResponseModel reviewedGame = gameService.addReview(reviewRequestModel, uuid);
        if(reviewedGame != null){
            return new ResponseEntity<>(reviewedGame, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

//    @PostMapping("/addToLibrary")
//    public ResponseEntity<Void> addToLibrary(@RequestBody Map<String, String> requestBody) {
//        String userUuid = requestBody.get("userUuid");
//        String gameUuid = requestBody.get("gameUuid");
//        gameService.addGameToUser(userUuid, gameUuid);
//        return new ResponseEntity<>(HttpStatus.OK);
//
//    }
}