package com.example.apigatewayservice.businesslogiclayer;

import com.example.apigatewayservice.DomainClientLayer.UserServiceClient;
import com.example.apigatewayservice.presentationlayer.UserRequestModel;
import com.example.apigatewayservice.presentationlayer.UserResponseModel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserServiceClient userServiceClient;

    @Override
    public List<UserResponseModel> getAllUsers() {
        return userServiceClient.getAllUsers();
    }

    @Override
    public UserResponseModel getUserById(String uuid) {
        return userServiceClient.getUserById(uuid);
    }

    @Override
    public UserResponseModel addUser(UserRequestModel userRequestModel) {
        return userServiceClient.addUser(userRequestModel);
    }

    @Override
    public void updateUser(UserRequestModel userRequestModel, String uuid) {
        userServiceClient.updateUser(userRequestModel, uuid);
    }

    @Override
    public void deleteUser(String uuid) {
        userServiceClient.deleteUser(uuid);
    }

    @Override
    public UserResponseModel updateUserBalance(String userId, double balance) {
        return userServiceClient.updateUserBalance(userId, balance);
    }
}