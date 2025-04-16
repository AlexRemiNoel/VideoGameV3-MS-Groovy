package com.champsoft.gamemanagement.DataMapper;

import com.champsoft.gamemanagement.DataAccess.Review;
import com.champsoft.gamemanagement.DataAccess.ReviewId;
import com.champsoft.gamemanagement.Presentation.DTOS.ReviewRequestModel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.LocalDateTime;
import java.util.UUID;

@Mapper(componentModel = "spring", imports = {LocalDateTime.class, Review.class, UUID.class, ReviewId.class})
public interface ReviewMapper {
    @Mapping(target = "reviewId", expression = "java(new ReviewId(UUID.randomUUID().toString()))")
    @Mapping(target = "timestamp", expression = "java(LocalDateTime.now())")
    Review reviewRequestModelToReview(ReviewRequestModel reviewRequestModel);
}
