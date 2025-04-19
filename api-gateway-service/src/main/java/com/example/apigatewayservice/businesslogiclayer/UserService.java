package com.example.apigatewayservice.businesslogiclayer;

import com.example.apigatewayservice.presentationlayer.UserRequestModel;
import com.example.apigatewayservice.presentationlayer.UserResponseModel;

import java.util.List;

public interface UserService {
    List<UserResponseModel> getAllUsers();
    UserResponseModel getUserById(String uuid);
    UserResponseModel addUser(UserRequestModel userRequestModel);
    void updateUser(UserRequestModel userRequestModel, String uuid); // Changed to void
    void deleteUser(String uuid);
    UserResponseModel updateUserBalance(String userId, double balance);
}