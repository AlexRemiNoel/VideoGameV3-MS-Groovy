package com.champsoft.gamemanagement.BusinessLogic;


import com.champsoft.gamemanagement.DataAccess.Game;
import com.champsoft.gamemanagement.DataAccess.GameRepository;
import com.champsoft.gamemanagement.DataAccess.Review;
import com.champsoft.gamemanagement.DataMapper.GameRequestMapper;
import com.champsoft.gamemanagement.DataMapper.GameResponseMapper;
import com.champsoft.gamemanagement.DataMapper.ReviewMapper;
import com.champsoft.gamemanagement.Presentation.DTOS.GameRequestModel;
import com.champsoft.gamemanagement.Presentation.DTOS.GameResponseModel;
import com.champsoft.gamemanagement.Presentation.DTOS.ReviewRequestModel;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class GameService {
    private GameResponseMapper gameResponseMapper;
    private GameRepository gameRepository;
    private GameRequestMapper gameRequestMapper;
    private ReviewMapper reviewMapper;


    public GameService(GameResponseMapper gameResponseMapper, GameRepository gameRepository, GameRequestMapper gameRequestMapper, ReviewMapper reviewMapper) {
        this.gameRepository =  gameRepository;
        this.gameRequestMapper = gameRequestMapper;
        this.gameResponseMapper = gameResponseMapper;
        this.reviewMapper = reviewMapper;

    }


    public GameResponseModel getGameById(String uuid){
        Game game = gameRepository.findGameByGameId_uuid(uuid);
        GameResponseModel responseModel = gameResponseMapper.gameToGameResponseModel(game);
        return responseModel;
    }

    public List<GameResponseModel> getAllGames(){
        List<Game> games = gameRepository.findAll();
        return gameResponseMapper.gameToGameResponseModel(games);
    }

    public GameResponseModel createGame(GameRequestModel gameRequestModel){
        if (gameRequestModel.getPrice()<0){
            return null;
        }
        return gameResponseMapper.gameToGameResponseModel(gameRepository.save(gameRequestMapper.requestModelToEntity(gameRequestModel)));
    }



    public GameResponseModel updateGame(GameRequestModel gameRequestModel){
        return gameResponseMapper.gameToGameResponseModel(gameRepository.save(gameRequestMapper.requestModelToEntity(gameRequestModel)));
    }

    public GameResponseModel deleteGame(String uuid){
        Game game = gameRepository.findGameByGameId_uuid(uuid);
        gameRepository.delete(game);
        return gameResponseMapper.gameToGameResponseModel(game);
    }

    public GameResponseModel addReview(ReviewRequestModel reviewRequestModel, String gameId){
        Game game = gameRepository.findGameByGameId_uuid(gameId);
        Review review = reviewMapper.reviewRequestModelToReview(reviewRequestModel);

        List<Review> reviews = game.getReviews();
        if (reviews == null) {
            reviews = new ArrayList<>();
        }

        reviews.add(review);

        game.setReviews(reviews);

        gameRepository.save(game);
        return gameResponseMapper.gameToGameResponseModel(game);
    }

    public void addGameToUser(String uuid, String gameId){
        Game game = gameRepository.findGameByGameId_uuid(gameId);
        gameRepository.save(game);

    }


}
