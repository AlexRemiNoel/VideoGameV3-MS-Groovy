package com.example.apigatewayservice.businesslogiclayer.user;

import com.example.apigatewayservice.DomainClientLayer.user.UserServiceClient;
import com.example.apigatewayservice.presentationlayer.user.UserRequestModel;
import com.example.apigatewayservice.presentationlayer.user.UserResponseModel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserServiceClient userServiceClient;

    @InjectMocks
    private UserServiceImpl userService;

    private final String VALID_USER_UUID = "user-uuid-123";

    private UserResponseModel buildUserResponseModel(String userId, String username, String email, double balance) {
        // Assumes UserResponseModel has a builder. Adjust if using constructor.
        return UserResponseModel.builder()
                .userId(userId)
                .username(username)
                .email(email)
                .balance(balance)
                .orders(Collections.emptyList())
                .games(Collections.emptyList())
                .build();
    }

    private UserRequestModel buildUserRequestModel(String username, String email, String password, double balance) {
        // Assumes UserRequestModel has a builder. Adjust if using constructor.
        return UserRequestModel.builder()
                .username(username)
                .email(email)
                .password(password)
                .balance(balance)
                .build();
    }

    @Test
    void getAllUsers_callsClient() {
        List<UserResponseModel> expectedResponse = List.of(buildUserResponseModel(VALID_USER_UUID, "user1", "e1@test.com", 10));
        when(userServiceClient.getAllUsers()).thenReturn(expectedResponse);

        List<UserResponseModel> actualResponse = userService.getAllUsers();

        assertEquals(expectedResponse, actualResponse);
        verify(userServiceClient, times(1)).getAllUsers();
    }

    @Test
    void getUserById_callsClient() {
        UserResponseModel expectedResponse = buildUserResponseModel(VALID_USER_UUID, "user1", "e1@test.com", 10);
        when(userServiceClient.getUserById(VALID_USER_UUID)).thenReturn(expectedResponse);

        UserResponseModel actualResponse = userService.getUserById(VALID_USER_UUID);

        assertEquals(expectedResponse, actualResponse);
        verify(userServiceClient, times(1)).getUserById(VALID_USER_UUID);
    }

    @Test
    void addUser_callsClient() {
        UserRequestModel requestModel = buildUserRequestModel("user1", "e1@test.com", "pass", 10);
        UserResponseModel expectedResponse = buildUserResponseModel(VALID_USER_UUID, "user1", "e1@test.com", 10);
        when(userServiceClient.addUser(requestModel)).thenReturn(expectedResponse);

        UserResponseModel actualResponse = userService.addUser(requestModel);

        assertEquals(expectedResponse, actualResponse);
        verify(userServiceClient, times(1)).addUser(requestModel);
    }

    @Test
    void updateUser_callsClient() {
        UserRequestModel requestModel = buildUserRequestModel("user1", "e1@test.com", "pass", 10.00);
        when(userServiceClient.updateUser(any(UserRequestModel.class), eq(VALID_USER_UUID))).thenReturn(null);

        userService.updateUser(requestModel, VALID_USER_UUID);

        verify(userServiceClient).updateUser(any(UserRequestModel.class), eq(VALID_USER_UUID));
    }

    @Test
    void deleteUser_callsClient() {
        doNothing().when(userServiceClient).deleteUser(VALID_USER_UUID);

        userService.deleteUser(VALID_USER_UUID);

        verify(userServiceClient, times(1)).deleteUser(VALID_USER_UUID);
    }

    @Test
    void updateUserBalance_callsClient() {
        double newBalance = 50.0;
        UserResponseModel expectedResponse = buildUserResponseModel(VALID_USER_UUID, "user1", "e1@test.com", newBalance);
        when(userServiceClient.updateUserBalance(VALID_USER_UUID, newBalance)).thenReturn(expectedResponse);

        UserResponseModel actualResponse = userService.updateUserBalance(VALID_USER_UUID, newBalance);

        assertEquals(expectedResponse, actualResponse);
        verify(userServiceClient, times(1)).updateUserBalance(VALID_USER_UUID, newBalance);
    }
} 