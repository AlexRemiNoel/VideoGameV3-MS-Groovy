package com.champsoft.usermanagement.DataMapper;


import com.champsoft.usermanagement.DataAccess.User;
import com.champsoft.usermanagement.DataAccess.UserId;
import com.champsoft.usermanagement.Presentation.UserResponseModel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;
import java.util.UUID;

@Mapper(componentModel = "spring", imports = {UserId.class, UUID.class})
public interface UserResponseMapper {
    @Mapping(target = "userId", expression = "java(user.getUserId().getUuid())")
    @Mapping(target = "username", expression = "java(user.getUsername())")
    UserResponseModel userToUserResponseModel(User user);
    List<UserResponseModel> userToUserResponseModel(List<User> users);
}