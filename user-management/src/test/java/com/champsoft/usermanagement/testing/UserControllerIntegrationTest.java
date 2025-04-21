package com.champsoft.usermanagement.testing;

import com.champsoft.usermanagement.DataAccess.User;
import com.champsoft.usermanagement.DataAccess.UserId;
import com.champsoft.usermanagement.DataAccess.UserRepository;
import com.champsoft.usermanagement.Presentation.UserRequestModel;
import com.champsoft.usermanagement.Presentation.UserResponseModel;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("h2")
class UserControllerIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private UserRepository userRepository;

    private final String BASE_URI_USER = "/api/v1/user";
    private User user1, user2;
    private String userId1, userId2;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll(); // Clean slate

        userId1 = UUID.randomUUID().toString();
        userId2 = UUID.randomUUID().toString();

        // Initialize lists for orders and games
        List<String> orders1 = new ArrayList<>(List.of("order1", "order2"));
        List<String> games1 = new ArrayList<>(List.of("game1"));
        List<String> orders2 = new ArrayList<>();
        List<String> games2 = new ArrayList<>(List.of("game2", "game3"));

        user1 = new User(new UserId(userId1), "userInteg1", "integ1@test.com", "passInteg1", 150.50, orders1, games1);
        user2 = new User(new UserId(userId2), "userInteg2", "integ2@test.com", "passInteg2", 75.0, orders2, games2);
    }

    @AfterEach
    void tearDown() {
        userRepository.deleteAll();
    }

    private User saveUser(User user) {
        return userRepository.save(user);
    }


    @Test
    void getUsers_whenUsersExist_thenReturnUsers() {
        // Arrange
        saveUser(user1);
        saveUser(user2);

        // Act & Assert
        webTestClient.get().uri(BASE_URI_USER)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.length()").isEqualTo(2)
                .jsonPath("$[0].userId").isEqualTo(user1.getUserId().getUuid())
                .jsonPath("$[0].username").isEqualTo(user1.getUsername())
                .jsonPath("$[0].balance").isEqualTo(user1.getBalance())
                .jsonPath("$[0].orders.length()").isEqualTo(user1.getOrders().size())
                .jsonPath("$[0].games.length()").isEqualTo(user1.getGames().size())
                .jsonPath("$[1].userId").isEqualTo(user2.getUserId().getUuid())
                .jsonPath("$[1].username").isEqualTo(user2.getUsername());
    }

    @Test
    void getUsers_whenNoUsersExist_thenReturnEmptyArray() {
        // Act & Assert
        webTestClient.get().uri(BASE_URI_USER)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.length()").isEqualTo(0);
    }

    @Test
    void getUserById_whenUserExists_thenReturnUser() {
        // Arrange
        saveUser(user1);

        // Act & Assert
        webTestClient.get().uri(BASE_URI_USER + "/" + userId1)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(UserResponseModel.class)
                .value((response) -> {
                    assertNotNull(response);
                    assertEquals(userId1, response.getUserId());
                    assertEquals(user1.getUsername(), response.getUsername());
                    assertEquals(user1.getEmail(), response.getEmail());
                    assertEquals(user1.getBalance(), response.getBalance());
                    assertEquals(user1.getOrders(), response.getOrders());
                    assertEquals(user1.getGames(), response.getGames());
                });
    }

    @Test
    void getUserById_whenUserDoesNotExist_thenReturnNotFound() {
        // Arrange
        String nonExistentId = UUID.randomUUID().toString();

        // Act & Assert
        webTestClient.get().uri(BASE_URI_USER + "/" + nonExistentId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.httpStatus").isEqualTo("NOT_FOUND")
                .jsonPath("$.message").isEqualTo("Unknown userId: " + nonExistentId);
    }

    @Test
    void addUser_whenValidRequest_thenCreateUser() {
        // Arrange
        UserRequestModel requestModel = new UserRequestModel("newUser", "new@test.com", "pass", 10.0);
        long countBefore = userRepository.count();

        // Act & Assert
        webTestClient.post().uri(BASE_URI_USER)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestModel)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(UserResponseModel.class)
                .value((response) -> {
                    assertNotNull(response);
                    assertNotNull(response.getUserId());
                    assertEquals(requestModel.getUsername(), response.getUsername());
                    assertEquals(requestModel.getEmail(), response.getEmail());
                    assertEquals(requestModel.getBalance(), response.getBalance());
                    assertTrue(response.getOrders() == null || response.getOrders().isEmpty()); // Orders/games are not part of request
                    assertTrue(response.getGames() == null || response.getGames().isEmpty());
                });

        // Verify DB state
        long countAfter = userRepository.count();
        assertEquals(countBefore + 1, countAfter);
    }

    @Test
    void addUser_whenInvalidBalance_thenReturnBadRequest() {
        // Arrange
        UserRequestModel requestModel = new UserRequestModel("newUser", "new@test.com", "pass", -10.0);
        long countBefore = userRepository.count();

        // Act & Assert
        webTestClient.post().uri(BASE_URI_USER)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestModel)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isBadRequest() // Assuming InvalidUserInputException maps to 400
                .expectBody()
                .jsonPath("$.httpStatus").isEqualTo("BAD_REQUEST") // Or whatever your handler returns
                .jsonPath("$.message").isEqualTo("Invalid negative balance: -10.0");

        // Verify DB state unchanged
        long countAfter = userRepository.count();
        assertEquals(countBefore, countAfter);
    }

    @Test
    void updateUser_whenUserExists_thenUpdateUser() {
        // Arrange
        saveUser(user1);
        // Note: Balance in request is ignored by the updateUser method
        UserRequestModel updateRequest = new UserRequestModel("updatedName", "updated@mail.com", "newPass", 999.99);

        // Act & Assert
        webTestClient.put().uri(BASE_URI_USER + "/" + userId1)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(updateRequest)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(UserResponseModel.class)
                .value((response) -> {
                    assertNotNull(response);
                    assertEquals(userId1, response.getUserId());
                    assertEquals(updateRequest.getUsername(), response.getUsername());
                    assertEquals(updateRequest.getEmail(), response.getEmail());
                    assertEquals(user1.getBalance(), response.getBalance()); // Balance should be unchanged
                    assertEquals(user1.getOrders(), response.getOrders()); // Orders/Games unchanged
                    assertEquals(user1.getGames(), response.getGames());
                });

        // Verify DB state
        User updatedUser = userRepository.findUserByUserId_uuid(userId1);
        assertNotNull(updatedUser);
        assertEquals(updateRequest.getUsername(), updatedUser.getUsername());
        assertEquals(updateRequest.getEmail(), updatedUser.getEmail());
        assertEquals(updateRequest.getPassword(), updatedUser.getPassword());
        assertEquals(user1.getBalance(), updatedUser.getBalance()); // Verify balance unchanged in DB
    }

    @Test
    void updateUser_whenUserDoesNotExist_thenReturnNotFound() {
        // Arrange
        String nonExistentId = UUID.randomUUID().toString();
        UserRequestModel updateRequest = new UserRequestModel("updatedName", "updated@mail.com", "newPass", 10.0);

        // Act & Assert
        webTestClient.put().uri(BASE_URI_USER + "/" + nonExistentId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(updateRequest)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.httpStatus").isEqualTo("NOT_FOUND")
                .jsonPath("$.message").isEqualTo("Unknown userId: " + nonExistentId);
    }

    @Test
    void deleteUser_whenUserExists_thenDeleteUser() {
        // Arrange
        saveUser(user1);
        assertTrue(userRepository.existsByUserId(user1.getUserId()));

        // Act & Assert
        webTestClient.delete().uri(BASE_URI_USER + "/" + userId1)
                .exchange()
                .expectStatus().isNoContent();

        // Verify DB state
        assertFalse(userRepository.existsByUserId(user1.getUserId()));
    }

    @Test
    void deleteUser_whenUserDoesNotExist_thenReturnNotFound() {
        // Arrange
        String nonExistentId = UUID.randomUUID().toString();
        assertFalse(userRepository.existsByUserId(new UserId(nonExistentId)));

        // Act & Assert
        webTestClient.delete().uri(BASE_URI_USER + "/" + nonExistentId)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.httpStatus").isEqualTo("NOT_FOUND")
                .jsonPath("$.message").isEqualTo("Unknown userId: " + nonExistentId);
    }

    @Test
    void updateUserBalance_whenUserExistsAndValidBalance_thenUpdateBalance() {
        // Arrange
        saveUser(user1);
        double newBalance = 300.75;
        String updateUri = String.format("%s/uuid/%s/balance/%.2f", BASE_URI_USER, userId1, newBalance);


        // Act & Assert
        webTestClient.put().uri(updateUri)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(UserResponseModel.class)
                .value((response) -> {
                    assertNotNull(response);
                    assertEquals(userId1, response.getUserId());
                    assertEquals(user1.getUsername(), response.getUsername());
                    assertEquals(newBalance, response.getBalance(), 0.001); // Compare doubles with tolerance
                    assertEquals(user1.getOrders(), response.getOrders()); // Check other fields remain
                    assertEquals(user1.getGames(), response.getGames());
                });

        // Verify DB state
        User updatedUser = userRepository.findUserByUserId_uuid(userId1);
        assertNotNull(updatedUser);
        assertEquals(newBalance, updatedUser.getBalance(), 0.001);
    }


    @Test
    void updateUserBalance_whenUserDoesNotExist_thenReturnNotFound() {
        // Arrange
        String nonExistentId = UUID.randomUUID().toString();
        double newBalance = 300.75;
        String updateUri = String.format("%s/uuid/%s/balance/%.2f", BASE_URI_USER, nonExistentId, newBalance);

        // Act & Assert
        webTestClient.put().uri(updateUri)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.httpStatus").isEqualTo("NOT_FOUND")
                .jsonPath("$.message").isEqualTo("Unknown userId: " + nonExistentId);
    }


    @Test
    void updateUserBalance_whenInvalidBalance_thenReturnBadRequest() {
        // Arrange
        saveUser(user1);
        double invalidBalance = -50.0;
        String updateUri = String.format("%s/uuid/%s/balance/%.2f", BASE_URI_USER, userId1, invalidBalance);

        // Act & Assert
        webTestClient.put().uri(updateUri)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.httpStatus").isEqualTo("BAD_REQUEST")
                .jsonPath("$.message").isEqualTo("Invalid negative balance: " + invalidBalance);

        // Verify DB balance unchanged
        User userAfter = userRepository.findUserByUserId_uuid(userId1);
        assertNotNull(userAfter);
        assertEquals(user1.getBalance(), userAfter.getBalance(), 0.001);
    }
}