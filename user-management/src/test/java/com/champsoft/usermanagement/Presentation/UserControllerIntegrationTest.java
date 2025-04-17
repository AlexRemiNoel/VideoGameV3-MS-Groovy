package com.champsoft.usermanagement.Presentation;

import com.champsoft.usermanagement.DataAccess.User;
import com.champsoft.usermanagement.DataAccess.UserId;
import com.champsoft.usermanagement.DataAccess.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT, properties = {"spring.datasource.url=jdbc:h2:mem:user-db"})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@ActiveProfiles("h2")
public class UserControllerIntegrationTest {
    @Autowired
    private WebTestClient WebTestClient;
    @Autowired
    private UserRepository userRepository;

    private final String BASE_URI_USERS = "api/v1/user";

//    private final String NOT_FOUND_USER_ID = "c3540a89-cb47-4c96-888e-ff96708db4d0";
    private final String VALID_USER_ID = "c3540a89-cb47-4c96-888e-ff96708db4d8";
    private final String INVALID_USER_ID = "nonExistentId";
    private final String VALID_USER_USERNAME = "John Doe";
    private final String VALID_USER_PASSWORD = "test123";
    private final Double VALID_BALANCE = 50.0;
    private final Double INVALID_BALANCE = -50.0;
    private final String VALID_USER_EMAIL = "john.doe@example.com";
    private final List<String> orderList = new ArrayList<>();
    private final List<String> gameList = new ArrayList<>();

    @BeforeEach
    void setup() {
        User user = new User();
        user.setUserId(new UserId(VALID_USER_ID));
        user.setUsername(VALID_USER_USERNAME);
        user.setEmail(VALID_USER_EMAIL);
        user.setPassword(VALID_USER_PASSWORD);
        user.setBalance(VALID_BALANCE);
        user.setOrders(orderList);
        user.setGames(gameList);
        userRepository.save(user);
    }

    private UserRequestModel buildUserRequestModel(String username) {
        UserRequestModel userRequestModel = new UserRequestModel();
        userRequestModel.setUsername(username); // Set username
        userRequestModel.setEmail(VALID_USER_EMAIL); // Set user email
        userRequestModel.setPassword(VALID_USER_PASSWORD);
        userRequestModel.setBalance(VALID_BALANCE);
        // Set any other required fields here
        return userRequestModel;
    }

    private UserRequestModel buildInvalidUserRequestModel(String nonExistentUserId) {

        UserRequestModel someUser = new UserRequestModel(
                VALID_USER_ID,
                VALID_USER_USERNAME,
                VALID_USER_PASSWORD,
                VALID_BALANCE
        );
        return someUser;
    }

    @Test
    public void whenDeleteUser_thenDeleteUserSuccessfully() {
        // Act
        WebTestClient.delete().uri(BASE_URI_USERS + "/" + VALID_USER_ID)
                .exchange()
                .expectStatus()
                .isNoContent();
        //Assert

        assertFalse(userRepository.existsByUserId(new UserId((VALID_USER_ID))));
    }

    @Test
    public void whenRemoveNonExistentUser_thenThrowNotFoundException() {
        // Arrange
        String nonExistentUserId = "nonExistentId";
        // Act & Assert
        WebTestClient.delete().uri(BASE_URI_USERS + "/" + nonExistentUserId)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.httpStatus").isEqualTo("NOT_FOUND")
                .jsonPath("$.message").isEqualTo("Unknown userId: " + nonExistentUserId);
    }

    @Test
    public void whenGetUserById_thenReturnUser() {
        // Act & Assert
        WebTestClient.get().uri(BASE_URI_USERS + "/" + VALID_USER_ID)
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isOk()
            .expectBody(UserResponseModel.class)
            .value((User) -> {
                assertNotNull(User);
                assertEquals(VALID_USER_ID, User.getUserId());
                assertEquals(VALID_USER_USERNAME, User.getUsername());
                assertEquals(VALID_USER_EMAIL, User.getEmail());
                assertEquals(VALID_BALANCE, User.getBalance());
        });
    }

    @Test
    public void whenValidUser_thenCreateUser(){
        //arrange
        long sizeDB = userRepository.count();
        UserRequestModel UserToCreate = buildUserRequestModel(VALID_USER_USERNAME);
        WebTestClient.post()
                .uri(BASE_URI_USERS)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(UserToCreate)
                .exchange()
                .expectStatus().isCreated()
                //.expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(UserResponseModel.class)
                .value((userResponseModel) -> {
                    assertNotNull(UserToCreate);
                    assertEquals(UserToCreate.getUsername(),userResponseModel.getUsername());
                    assertEquals(UserToCreate.getEmail(),userResponseModel.getEmail());
                    assertEquals(UserToCreate.getBalance(), userResponseModel.getBalance());
                });
        long sizeDBAfter = userRepository.count();
        assertEquals(sizeDB + 1, sizeDBAfter);
    }

    @Test
    public void whenUpdateNonExistentUser_thenThrowNotFoundException() {
        // Arrange
        UserRequestModel updatedUser = buildInvalidUserRequestModel(INVALID_USER_ID);
        // Act & Assert
        WebTestClient.put()
                .uri(BASE_URI_USERS + "/" + INVALID_USER_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(updatedUser)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.httpStatus").isEqualTo("NOT_FOUND")
                .jsonPath("$.message").isEqualTo("Unknown userId: " + INVALID_USER_ID);
    }

    @Test
    public void whenUpdateUser_thenReturnUpdatedUser() {
        // Arrange
        UserRequestModel UserToUpdate = buildUserRequestModel(VALID_USER_USERNAME);
        // Act & Assert
        WebTestClient.put()
                .uri(BASE_URI_USERS + "/" + VALID_USER_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(UserToUpdate)
                .exchange()
                .expectStatus().isOk()
                .expectBody(UserResponseModel.class)
                .value((updatedUser) -> {
                    assertNotNull(updatedUser);
                    assertEquals(UserToUpdate.getUsername(), updatedUser.getUsername());
                    assertEquals(UserToUpdate.getEmail(), updatedUser.getEmail());
                    assertEquals(UserToUpdate.getBalance(), updatedUser.getBalance());
                });
    }

    @Test
    public void whenUpdateUserBalance_thenThrowNotFoundException() {
        // Arrange
        String nonExistentUserId = "nonExistentId";
        // Act & Assert
        WebTestClient.put()
                .uri(BASE_URI_USERS + "/uuid/" + nonExistentUserId + "/balance/" + VALID_BALANCE)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.httpStatus").isEqualTo("NOT_FOUND")
                .jsonPath("$.message").isEqualTo("Unknown userId: " + nonExistentUserId);
    }

    @Test
    public void whenUpdateUserBalance_thenThrowInvalidInputException() {
        // Arrange
        // Act & Assert
        WebTestClient.put()
                .uri(BASE_URI_USERS + "/uuid/" + VALID_USER_ID + "/balance/" + INVALID_BALANCE)
                .exchange()
//                .expectStatus().isUnauthorized()
                .expectBody()
                .jsonPath("$.httpStatus").isEqualTo("UNPROCESSABLE_ENTITY")
                .jsonPath("$.message").isEqualTo("Invalid negative balance: " + INVALID_BALANCE);
    }

    @Test
    public void whenUpdateUserBalance_thenReturnUpdatedUserBalance() {
        // Arrange
        UserRequestModel UserToUpdate = buildUserRequestModel(VALID_USER_USERNAME);
        // Act & Assert
        WebTestClient.put()
                .uri(BASE_URI_USERS + "/uuid/" + VALID_USER_ID + "/balance/" + VALID_BALANCE)
                .exchange()
                .expectStatus().isOk()
                .expectBody(UserResponseModel.class)
                .value((updatedUser) -> {
                    assertNotNull(updatedUser);
                    assertEquals(UserToUpdate.getUsername(), updatedUser.getUsername());
                    assertEquals(UserToUpdate.getEmail(), updatedUser.getEmail());
                    assertEquals(UserToUpdate.getBalance(), updatedUser.getBalance());
                });
    }
}
