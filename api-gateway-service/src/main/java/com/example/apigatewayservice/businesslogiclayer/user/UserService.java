package com.example.apigatewayservice.businesslogiclayer.user;

import com.example.apigatewayservice.presentationlayer.user.UserRequestModel;
import com.example.apigatewayservice.presentationlayer.user.UserResponseModel;

import java.util.List;

public interface UserService {
    List<UserResponseModel> getAllUsers();
    UserResponseModel getUserById(String uuid);
    UserResponseModel addUser(UserRequestModel userRequestModel);
    void updateUser(UserRequestModel userRequestModel, String uuid);
    void deleteUser(String uuid);
    UserResponseModel updateUserBalance(String userId, double balance);
}