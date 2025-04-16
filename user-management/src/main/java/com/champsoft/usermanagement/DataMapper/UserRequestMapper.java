package com.champsoft.usermanagement.DataMapper;


import com.champsoft.usermanagement.DataAccess.User;
import com.champsoft.usermanagement.DataAccess.UserId;
import com.champsoft.usermanagement.Presentation.UserRequestModel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;
import java.util.UUID;

@Mapper(componentModel = "spring", imports = {UserId.class, UUID.class})
public interface UserRequestMapper {
    @Mapping(target = "userId", expression = "java(new UserId(UUID.randomUUID().toString()))")
    User userRequestModelToUser(UserRequestModel userRequestModel);
    List<UserRequestModel> userRequestModelToUser(List<UserRequestModel> userRequestModels);
}