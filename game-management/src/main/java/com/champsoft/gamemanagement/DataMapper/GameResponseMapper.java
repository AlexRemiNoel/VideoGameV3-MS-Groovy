package com.champsoft.gamemanagement.DataMapper;

import com.champsoft.gamemanagement.DataAccess.Game;
import com.champsoft.gamemanagement.DataAccess.GameId;
import com.champsoft.gamemanagement.DataAccess.Genre;
import com.champsoft.gamemanagement.Presentation.DTOS.GameRequestModel;
import com.champsoft.gamemanagement.Presentation.DTOS.GameResponseModel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Mapper(componentModel = "spring",imports = {UUID.class, GameRequestModel.class, GameId.class, LocalDateTime.class, Genre.class})
public interface GameResponseMapper {


    @Mapping(expression = "java(game.getGameId().getUuid())", target = "id")
    @Mapping(expression = "java(game.getReleaseDate().toString())",target = "releaseDate")
    @Mapping(expression = "java(game.getGenre().toString())",target = "genre")
    GameResponseModel gameToGameResponseModel(Game game);
    List<GameResponseModel> gameToGameResponseModel(List<Game> game);
}
