package com.champsoft.gamemanagement.BusinessLogic;


import com.champsoft.gamemanagement.DataAccess.*;
import com.champsoft.gamemanagement.DataMapper.GameRequestMapper;
import com.champsoft.gamemanagement.DataMapper.GameResponseMapper;
import com.champsoft.gamemanagement.DataMapper.ReviewMapper;
import com.champsoft.gamemanagement.Presentation.DTOS.GameRequestModel;
import com.champsoft.gamemanagement.Presentation.DTOS.GameResponseModel;
import com.champsoft.gamemanagement.Presentation.DTOS.ReviewRequestModel;

import com.champsoft.gamemanagement.utils.exceptions.NotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class GameService {
    private final ReviewRepository reviewRepository;
    private GameResponseMapper gameResponseMapper;
    private GameRepository gameRepository;
    private GameRequestMapper gameRequestMapper;
    private ReviewMapper reviewMapper;


    public GameService(GameResponseMapper gameResponseMapper, GameRepository gameRepository, GameRequestMapper gameRequestMapper, ReviewMapper reviewMapper, ReviewRepository reviewRepository) {
        this.gameRepository =  gameRepository;
        this.gameRequestMapper = gameRequestMapper;
        this.gameResponseMapper = gameResponseMapper;
        this.reviewMapper = reviewMapper;
        this.reviewRepository = reviewRepository;
    }

    public GameResponseModel getGameById(String uuid){
        Game game = gameRepository.findGameByGameId(new GameId(uuid));
        if (game == null) {
            throw new NotFoundException("Game with UUID: " + uuid);
        }
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
        Game game = gameRepository.findGameByGameId(new GameId(gameRequestModel.getUserId()));
        if (game == null) {
            throw new NotFoundException(gameRequestModel.getUserId());
        }
        return gameResponseMapper.gameToGameResponseModel(gameRepository.save(gameRequestMapper.requestModelToEntity(gameRequestModel)));
    }

    public GameResponseModel deleteGame(String uuid){
        Game game = gameRepository.findGameByGameId(new GameId(uuid));
        if (game == null) {
            throw new NotFoundException("Game with UUID: " + uuid);
        }
        gameRepository.delete(game);
        return gameResponseMapper.gameToGameResponseModel(game);
    }

    public GameResponseModel addReview(ReviewRequestModel reviewRequestModel, String gameId){
        Game game = gameRepository.findGameByGameId(new GameId(gameId));
        Review review = reviewMapper.reviewRequestModelToReview(reviewRequestModel);

        review.setReviewId(new ReviewId(UUID.randomUUID().toString()));
        review.setTimestamp(LocalDateTime.now());

        List<Review> reviews = game.getReviews();
        if (reviews == null) {
            reviews = new ArrayList<>();
        }

        reviews.add(review);
        review.setGame(game);

        System.out.println(reviews);
        game.setReviews(reviews);
        reviewRepository.save(review);
        gameRepository.save(game);
        return gameResponseMapper.gameToGameResponseModel(game);
    }

    public void addGameToUser(String uuid, String gameId){
        Game game = gameRepository.findGameByGameId(new GameId(uuid));
        gameRepository.save(game);

    }


    public <T> void updateGame(T eq, T any) {

    }
}
