package com.example.apigatewayservice.businesslogiclayer.game;

import com.example.apigatewayservice.DomainClientLayer.game.GameServiceClient;
import com.example.apigatewayservice.presentationlayer.game.GameRequestModel;
import com.example.apigatewayservice.presentationlayer.game.GameResponseModel;
import com.example.apigatewayservice.presentationlayer.game.ReviewRequestModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

public interface GameService {
    List<GameResponseModel> getAllGames();
    GameResponseModel getGameById(String uuid);
    GameResponseModel createGame(GameRequestModel gameRequestModel);
    GameResponseModel updateGame(GameRequestModel gameRequestModel);
    void deleteGame(String uuid); // Changed to void based on client implementation
    GameResponseModel addReview(ReviewRequestModel reviewRequestModel, String gameUuid);
}


