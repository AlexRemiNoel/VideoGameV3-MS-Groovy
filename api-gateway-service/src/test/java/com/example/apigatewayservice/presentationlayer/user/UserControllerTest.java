package com.example.apigatewayservice.presentationlayer.user;

import com.example.apigatewayservice.businesslogiclayer.user.UserService;
import com.example.apigatewayservice.exception.HttpErrorInfo;
import com.example.apigatewayservice.exception.InvalidInputException;
import com.example.apigatewayservice.exception.NotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
public class UserControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private UserService userService;

    private final String BASE_URI_USERS = "/api/v1/user";
    private final String VALID_USER_UUID = "user-uuid-123";
    private final String NOT_FOUND_USER_UUID = "user-uuid-999";
    private final String INVALID_USER_UUID = "invalid-uuid"; // Example of an ID that might cause InvalidInputException

    private UserResponseModel buildUserResponseModel(String userId, String username, String email, double balance) {
        return UserResponseModel.builder()
                .userId(userId)
                .username(username)
                .email(email)
                .balance(balance)
                .orders(Collections.emptyList())
                .games(Collections.emptyList())
                .build();
    }

    // Helper to create UserRequestModel. Add @Builder to your DTO.
    private UserRequestModel buildUserRequestModel(String username, String email, String password, double balance) {
        return UserRequestModel.builder()
                .username(username)
                .email(email)
                .password(password)
                .balance(balance)
                .build();
    }

    @Test
    void getUsers_whenUsersExist_thenReturnUsers() {
        // Arrange
        UserResponseModel user1 = buildUserResponseModel("uuid1", "user1", "user1@test.com", 100.0);
        UserResponseModel user2 = buildUserResponseModel("uuid2", "user2", "user2@test.com", 50.0);
        List<UserResponseModel> expectedUsers = List.of(user1, user2);
        when(userService.getAllUsers()).thenReturn(expectedUsers);

        // Act & Assert
        webTestClient.get().uri(BASE_URI_USERS)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBodyList(UserResponseModel.class)
                .hasSize(2)
                .value(users -> {
                    assertEquals(expectedUsers.get(0).getUserId(), users.get(0).getUserId());
                    assertEquals(expectedUsers.get(1).getUserId(), users.get(1).getUserId());
                });
        verify(userService, times(1)).getAllUsers();
    }

    @Test
    void getUsers_whenNoUsersExist_thenReturnEmptyList() {
        // Arrange
        when(userService.getAllUsers()).thenReturn(Collections.emptyList());

        // Act & Assert
        webTestClient.get().uri(BASE_URI_USERS)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBodyList(UserResponseModel.class)
                .hasSize(0);
        verify(userService, times(1)).getAllUsers();
    }

    @Test
    void getUserById_whenUserExists_thenReturnUser() {
        // Arrange
        UserResponseModel expectedUser = buildUserResponseModel(VALID_USER_UUID, "testUser", "test@example.com", 200.0);
        when(userService.getUserById(VALID_USER_UUID)).thenReturn(expectedUser);

        // Act & Assert
        webTestClient.get().uri(BASE_URI_USERS + "/" + VALID_USER_UUID)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(UserResponseModel.class)
                .value(user -> {
                    assertNotNull(user);
                    assertEquals(expectedUser.getUserId(), user.getUserId());
                    assertEquals(expectedUser.getUsername(), user.getUsername());
                });
        verify(userService, times(1)).getUserById(VALID_USER_UUID);
    }

    @Test
    void getUserById_whenUserNotFound_thenReturnNotFound() {
        // Arrange
        String errorMessage = "User not found with uuid: " + NOT_FOUND_USER_UUID;
        when(userService.getUserById(NOT_FOUND_USER_UUID)).thenThrow(new NotFoundException(errorMessage));

        // Act & Assert
        webTestClient.get().uri(BASE_URI_USERS + "/" + NOT_FOUND_USER_UUID)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(HttpErrorInfo.class)
                .value(errorInfo -> {
                    assertNotNull(errorInfo);
                    assertEquals(HttpStatus.NOT_FOUND, errorInfo.getHttpStatus());
                    assertTrue(errorInfo.getMessage().contains(errorMessage));
                    assertEquals(BASE_URI_USERS + "/" + NOT_FOUND_USER_UUID, errorInfo.getPath());
                });
        verify(userService, times(1)).getUserById(NOT_FOUND_USER_UUID);
    }

    @Test
    void addUser_whenValidRequest_thenReturnCreatedUser() {
        // Arrange
        UserRequestModel requestModel = buildUserRequestModel("newUser", "new@example.com", "password123", 0.0);
        UserResponseModel expectedResponse = buildUserResponseModel(VALID_USER_UUID, "newUser", "new@example.com", 0.0);

        when(userService.addUser(any(UserRequestModel.class))).thenReturn(expectedResponse);

        // Act & Assert
        webTestClient.post().uri(BASE_URI_USERS)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestModel)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(UserResponseModel.class)
                .value(response -> {
                    assertNotNull(response);
                    assertEquals(expectedResponse.getUserId(), response.getUserId());
                    assertEquals(requestModel.getUsername(), response.getUsername());
                });
        verify(userService, times(1)).addUser(any(UserRequestModel.class));
    }

    @Test
    void addUser_whenInvalidInput_thenReturnUnprocessableEntity() {
        // Arrange
        UserRequestModel requestModel = buildUserRequestModel("", "", "", -10.0); // Invalid data
        String errorMessage = "Invalid user data provided";
        when(userService.addUser(any(UserRequestModel.class))).thenThrow(new InvalidInputException(errorMessage));

        // Act & Assert
        webTestClient.post().uri(BASE_URI_USERS)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestModel)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY)
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(HttpErrorInfo.class)
                .value(errorInfo -> {
                    assertNotNull(errorInfo);
                    assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, errorInfo.getHttpStatus());
                    assertTrue(errorInfo.getMessage().contains(errorMessage));
                });
        verify(userService, times(1)).addUser(any(UserRequestModel.class));
    }

    @Test
    void updateUser_whenUserExistsAndValidRequest_thenReturnNoContent() {
        // Arrange
        UserRequestModel requestModel = buildUserRequestModel("updatedUser", "updated@example.com", "newPass", 150.0);
        doNothing().when(userService).updateUser(any(UserRequestModel.class), eq(VALID_USER_UUID));

        // Act & Assert
        webTestClient.put().uri(BASE_URI_USERS + "/" + VALID_USER_UUID)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestModel)
                .exchange()
                .expectStatus().isNoContent();
        verify(userService, times(1)).updateUser(any(UserRequestModel.class), eq(VALID_USER_UUID));
    }

    @Test
    void updateUser_whenUserNotFound_thenReturnNotFound() {
        // Arrange
        UserRequestModel requestModel = buildUserRequestModel("anyUser", "any@example.com", "anyPass", 0.0);
        String errorMessage = "Cannot update. User not found with uuid: " + NOT_FOUND_USER_UUID;
        doThrow(new NotFoundException(errorMessage)).when(userService).updateUser(any(UserRequestModel.class), eq(NOT_FOUND_USER_UUID));

        // Act & Assert
        webTestClient.put().uri(BASE_URI_USERS + "/" + NOT_FOUND_USER_UUID)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestModel)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody(HttpErrorInfo.class)
                .value(errorInfo -> {
                    assertNotNull(errorInfo);
                    assertEquals(HttpStatus.NOT_FOUND, errorInfo.getHttpStatus());
                    assertTrue(errorInfo.getMessage().contains(errorMessage));
                });
        verify(userService, times(1)).updateUser(any(UserRequestModel.class), eq(NOT_FOUND_USER_UUID));
    }

    @Test
    void deleteUser_whenUserExists_thenReturnNoContent() {
        // Arrange
        doNothing().when(userService).deleteUser(VALID_USER_UUID);

        // Act & Assert
        webTestClient.delete().uri(BASE_URI_USERS + "/" + VALID_USER_UUID)
                .exchange()
                .expectStatus().isNoContent();
        verify(userService, times(1)).deleteUser(VALID_USER_UUID);
    }

    @Test
    void deleteUser_whenUserNotFound_thenReturnNotFound() {
        // Arrange
        String errorMessage = "Cannot delete. User not found with uuid: " + NOT_FOUND_USER_UUID;
        doThrow(new NotFoundException(errorMessage)).when(userService).deleteUser(NOT_FOUND_USER_UUID);

        // Act & Assert
        webTestClient.delete().uri(BASE_URI_USERS + "/" + NOT_FOUND_USER_UUID)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody(HttpErrorInfo.class)
                .value(errorInfo -> {
                    assertNotNull(errorInfo);
                    assertEquals(HttpStatus.NOT_FOUND, errorInfo.getHttpStatus());
                    assertTrue(errorInfo.getMessage().contains(errorMessage));
                });
        verify(userService, times(1)).deleteUser(NOT_FOUND_USER_UUID);
    }

    @Test
    void updateUserBalance_whenUserExists_thenReturnUpdatedUser() {
        // Arrange
        double newBalance = 250.75;
        UserResponseModel expectedResponse = buildUserResponseModel(VALID_USER_UUID, "testUser", "test@example.com", newBalance);
        when(userService.updateUserBalance(VALID_USER_UUID, newBalance)).thenReturn(expectedResponse);

        // Act & Assert
        webTestClient.put().uri(BASE_URI_USERS + "/uuid/" + VALID_USER_UUID + "/balance/" + newBalance)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(UserResponseModel.class)
                .value(response -> {
                    assertNotNull(response);
                    assertEquals(VALID_USER_UUID, response.getUserId());
                    assertEquals(newBalance, response.getBalance());
                });
        verify(userService, times(1)).updateUserBalance(VALID_USER_UUID, newBalance);
    }

    @Test
    void updateUserBalance_whenUserNotFound_thenReturnNotFound() {
        // Arrange
        double newBalance = 100.0;
        String errorMessage = "User not found for balance update: " + NOT_FOUND_USER_UUID;
        when(userService.updateUserBalance(NOT_FOUND_USER_UUID, newBalance)).thenThrow(new NotFoundException(errorMessage));

        // Act & Assert
        webTestClient.put().uri(BASE_URI_USERS + "/uuid/" + NOT_FOUND_USER_UUID + "/balance/" + newBalance)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody(HttpErrorInfo.class)
                .value(errorInfo -> {
                    assertNotNull(errorInfo);
                    assertEquals(HttpStatus.NOT_FOUND, errorInfo.getHttpStatus());
                    assertTrue(errorInfo.getMessage().contains(errorMessage));
                });
        verify(userService, times(1)).updateUserBalance(NOT_FOUND_USER_UUID, newBalance);
    }
} 