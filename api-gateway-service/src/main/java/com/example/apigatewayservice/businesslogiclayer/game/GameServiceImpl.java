package com.example.apigatewayservice.businesslogiclayer.game;// GameGatewayServiceImpl.java (Implementation)

import com.example.apigatewayservice.DomainClientLayer.game.GameServiceClient;
import com.example.apigatewayservice.presentationlayer.game.GameRequestModel;
import com.example.apigatewayservice.presentationlayer.game.GameResponseModel;
import com.example.apigatewayservice.presentationlayer.game.ReviewRequestModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor // Lombok constructor injection
public class GameServiceImpl implements GameService {

    private final GameServiceClient gameServiceClient;

    @Override
    public List<GameResponseModel> getAllGames() {
        log.debug("Calling client for getAllGames");
        return gameServiceClient.getAllGames();
    }

    @Override
    public GameResponseModel getGameById(String uuid) {
        log.debug("Calling client for getGameById with UUID: {}", uuid);
        return gameServiceClient.getGameByGameId(uuid);
    }

    @Override
    public GameResponseModel createGame(GameRequestModel gameRequestModel) {
        log.debug("Calling client for createGame");
        return gameServiceClient.createGame(gameRequestModel);
    }

    @Override
    public GameResponseModel updateGame(GameRequestModel gameRequestModel) {
        log.debug("Calling client for updateGame");
        return gameServiceClient.updateGame(gameRequestModel);
    }

    @Override
    public void deleteGame(String uuid) {
        log.debug("Calling client for deleteGame with UUID: {}", uuid);
        gameServiceClient.deleteGame(uuid);
    }

    @Override
    public GameResponseModel addReview(ReviewRequestModel reviewRequestModel, String gameUuid) {
        log.debug("Calling client for addReview for game UUID: {}", gameUuid);
        return gameServiceClient.addReview(reviewRequestModel, gameUuid);
    }
}