package com.champsoft.profiledashboardmanagement.testing;

import com.champsoft.profiledashboardmanagement.DataAccess.User;
import com.champsoft.profiledashboardmanagement.DataAccess.UserId;
import com.champsoft.profiledashboardmanagement.DataAccess.UserRepository;
import com.champsoft.profiledashboardmanagement.Presentation.UserRequestModel;
import com.champsoft.profiledashboardmanagement.Presentation.UserResponseModel;
import com.champsoft.profiledashboardmanagement.utils.HttpErrorInfo;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient; // Ensure webflux starter is present for tests

import java.util.ArrayList;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("h2") // To use application-h2.yml
public class UserControllerIntegrationTest {

    @Autowired
    private WebTestClient webTestClient; // Auto-configured by Spring Boot

    @Autowired
    private UserRepository userRepository;

    private User user1, user2;
    private final String BASE_URI_USERS = "/api/v1/user";

    @BeforeEach
    void setUp() {
        userRepository.deleteAll(); // Clean slate before each test

        UserId userId1 = new UserId(UUID.randomUUID().toString());
        user1 = new User(userId1, "Integration User1", "intuser1@example.com", "pass1", 100.0, new ArrayList<>());

        UserId userId2 = new UserId(UUID.randomUUID().toString());
        user2 = new User(userId2, "Integration User2", "intuser2@example.com", "pass2", 200.0, new ArrayList<>());

        userRepository.save(user1);
        userRepository.save(user2);
    }

    @AfterEach
    void tearDown() {
        userRepository.deleteAll();
    }

    @Test
    void getUsers_shouldReturnAllUsers() {
        webTestClient.get().uri(BASE_URI_USERS)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBodyList(UserResponseModel.class)
                .hasSize(2)
                .value(users -> {
                    assertTrue(users.stream().anyMatch(u -> u.getUserId().equals(user1.getUserId().getUuid())));
                    assertTrue(users.stream().anyMatch(u -> u.getUserId().equals(user2.getUserId().getUuid())));
                });
    }

    @Test
    void getUserById_whenUserExists_shouldReturnUser() {
        String existingUserId = user1.getUserId().getUuid();
        webTestClient.get().uri(BASE_URI_USERS + "/" + existingUserId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(UserResponseModel.class)
                .value(userResponse -> {
                    assertEquals(existingUserId, userResponse.getUserId());
                    assertEquals(user1.getUsername(), userResponse.getUsername());
                });
    }

    @Test
    void getUserById_whenUserDoesNotExist_shouldReturnNotFound() {
        String nonExistentUserId = UUID.randomUUID().toString();
        webTestClient.get().uri(BASE_URI_USERS + "/" + nonExistentUserId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody(HttpErrorInfo.class) // Assuming HttpErrorInfo is your error DTO
                .value(errorInfo -> {
                    assertEquals(404, errorInfo.getHttpStatus().value());
                    assertTrue(errorInfo.getMessage().contains("Unknown userId: " + nonExistentUserId));
                });
    }

    @Test
    void addUser_whenValidRequest_shouldCreateUser() {
        UserRequestModel newUserRequest = new UserRequestModel("New Int User", "newint@example.com", "newpass", 75.0);
        long countBefore = userRepository.count();

        webTestClient.post().uri(BASE_URI_USERS)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(newUserRequest)
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(UserResponseModel.class)
                .value(createdUser -> {
                    assertNotNull(createdUser.getUserId());
                    assertEquals(newUserRequest.getUsername(), createdUser.getUsername());
                    assertEquals(newUserRequest.getEmail(), createdUser.getEmail());
                    assertEquals(newUserRequest.getBalance(), createdUser.getBalance());
                });
        assertEquals(countBefore + 1, userRepository.count());
    }

    @Test
    void addUser_whenNegativeBalance_shouldReturnUnprocessableEntity() {
        UserRequestModel invalidUserRequest = new UserRequestModel("Invalid User", "invalid@example.com", "pass", -50.0);

        webTestClient.post().uri(BASE_URI_USERS)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(invalidUserRequest)
                .exchange()
                .expectStatus().isEqualTo(422) // UNPROCESSABLE_ENTITY
                .expectBody(HttpErrorInfo.class)
                .value(errorInfo -> {
                    assertEquals(422, errorInfo.getHttpStatus().value());
                    assertTrue(errorInfo.getMessage().contains("Invalid negative balance: -50.0"));
                });
    }

    @Test
    void updateUser_whenUserExistsAndValidRequest_shouldUpdateUser() {
        String existingUserId = user1.getUserId().getUuid();
        UserRequestModel updateUserRequest = new UserRequestModel("Updated Name", "updated@example.com", "updatedPass", user1.getBalance()); // Keep balance same or update if intended

        webTestClient.put().uri(BASE_URI_USERS + "/" + existingUserId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(updateUserRequest)
                .exchange()
                .expectStatus().isOk()
                .expectBody(UserResponseModel.class)
                .value(updatedUser -> {
                    assertEquals(existingUserId, updatedUser.getUserId());
                    assertEquals(updateUserRequest.getUsername(), updatedUser.getUsername());
                    assertEquals(updateUserRequest.getEmail(), updatedUser.getEmail());
                });

        User dbUser = userRepository.findUserByUserId_uuid(existingUserId);
        assertNotNull(dbUser);
        assertEquals("Updated Name", dbUser.getUsername());
    }

    @Test
    void updateUser_whenUserDoesNotExist_shouldReturnNotFound() {
        String nonExistentUserId = UUID.randomUUID().toString();
        UserRequestModel updateUserRequest = new UserRequestModel("Any Name", "any@example.com", "anyPass", 0);

        webTestClient.put().uri(BASE_URI_USERS + "/" + nonExistentUserId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(updateUserRequest)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody(HttpErrorInfo.class)
                .value(errorInfo -> assertTrue(errorInfo.getMessage().contains("Unknown userId: " + nonExistentUserId)));
    }

    @Test
    void deleteUser_whenUserExists_shouldDeleteUser() {
        String existingUserId = user1.getUserId().getUuid();
        assertTrue(userRepository.existsByUserId(user1.getUserId()));

        webTestClient.delete().uri(BASE_URI_USERS + "/" + existingUserId)
                .exchange()
                .expectStatus().isNoContent();

        assertFalse(userRepository.existsByUserId(user1.getUserId()));
    }

    @Test
    void deleteUser_whenUserDoesNotExist_shouldReturnNotFound() {
        String nonExistentUserId = UUID.randomUUID().toString();
        webTestClient.delete().uri(BASE_URI_USERS + "/" + nonExistentUserId)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody(HttpErrorInfo.class)
                .value(errorInfo -> assertTrue(errorInfo.getMessage().contains("Unknown userId: " + nonExistentUserId)));
    }

    @Test
    void updateUserBalance_whenUserExistsAndValidBalance_shouldUpdateBalance() {
        String existingUserId = user1.getUserId().getUuid();
        double newBalance = 300.0;

        webTestClient.put().uri(BASE_URI_USERS + "/uuid/" + existingUserId + "/balance/" + newBalance)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(UserResponseModel.class)
                .value(userResponse -> {
                    assertEquals(existingUserId, userResponse.getUserId());
                    assertEquals(newBalance, userResponse.getBalance());
                });

        User dbUser = userRepository.findUserByUserId_uuid(existingUserId);
        assertNotNull(dbUser);
        assertEquals(newBalance, dbUser.getBalance());
    }

    @Test
    void updateUserBalance_whenUserDoesNotExist_shouldReturnNotFound() {
        String nonExistentUserId = UUID.randomUUID().toString();
        double newBalance = 100.0;

        webTestClient.put().uri(BASE_URI_USERS + "/uuid/" + nonExistentUserId + "/balance/" + newBalance)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody(HttpErrorInfo.class)
                .value(errorInfo -> assertTrue(errorInfo.getMessage().contains("Unknown userId: " + nonExistentUserId)));
    }

    @Test
    void updateUserBalance_whenNegativeBalance_shouldReturnUnprocessableEntity() {
        String existingUserId = user1.getUserId().getUuid();
        double negativeBalance = -100.0;

        webTestClient.put().uri(BASE_URI_USERS + "/uuid/" + existingUserId + "/balance/" + negativeBalance)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(422) // UNPROCESSABLE_ENTITY
                .expectBody(HttpErrorInfo.class)
                .value(errorInfo -> assertTrue(errorInfo.getMessage().contains("Invalid negative balance: " + negativeBalance)));
    }
}
