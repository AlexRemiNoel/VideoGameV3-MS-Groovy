package com.champsoft.profiledashboardmanagement.testing;

import com.champsoft.profiledashboardmanagement.DataAccess.User;
import com.champsoft.profiledashboardmanagement.DataAccess.UserId;
import com.champsoft.profiledashboardmanagement.DataAccess.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
// Optional: Add ActiveProfiles if your main config might interfere
// @ActiveProfiles("h2")
class UserRepositoryIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    private User user1;
    private User user2;
    private UserId userIdObject1; // Store the UserId object
    private UserId userIdObject2;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll(); // Ensure clean state

        String uuid1 = UUID.randomUUID().toString();
        String uuid2 = UUID.randomUUID().toString();

        userIdObject1 = new UserId(uuid1); // Create the UserId object
        userIdObject2 = new UserId(uuid2); // Create the second UserId object

        List<String> orders1 = new ArrayList<>(List.of("order1", "order2"));
        List<String> games1 = new ArrayList<>(List.of("game1"));
        List<String> orders2 = new ArrayList<>();
        List<String> games2 = new ArrayList<>(List.of("game2", "game3"));

        user1 = new User(userIdObject1, "repoUser1", "repo1@test.com", "pass1", 100.0, games1);
        user2 = new User(userIdObject2, "repoUser2", "repo2@test.com", "pass2", 50.0, games2);
    }

    @Test
    void findUserByUserId_uuid_whenUserExists_thenReturnUser() {
        // Arrange
        userRepository.save(user1);

        // Act
        // Use the custom query method which expects a String uuid
        User foundUser = userRepository.findUserByUserId_uuid(user1.getUserId().getUuid());

        // Assert
        assertNotNull(foundUser);
        assertEquals(user1.getUserId(), foundUser.getUserId());
        assertEquals(user1.getUsername(), foundUser.getUsername());
        assertEquals(user1.getEmail(), foundUser.getEmail());
        assertEquals(user1.getBalance(), foundUser.getBalance());

        assertEquals(user1.getGames(), foundUser.getGames());
    }

    @Test
    void findUserByUserId_uuid_whenUserDoesNotExist_thenReturnNull() {
        // Arrange
        String nonExistentUuid = UUID.randomUUID().toString();

        // Act
        User foundUser = userRepository.findUserByUserId_uuid(nonExistentUuid);

        // Assert
        assertNull(foundUser);
    }

    @Test
    void existsByUserId_whenUserExists_thenReturnTrue() {
        // Arrange
        userRepository.save(user1);

        // Act
        // Use the custom query method which expects a UserId object
        boolean exists = userRepository.existsByUserId(user1.getUserId());

        // Assert
        assertTrue(exists);
    }

    @Test
    void existsByUserId_whenUserDoesNotExist_thenReturnFalse() {
        // Arrange
        UserId nonExistentIdObject = new UserId(UUID.randomUUID().toString());

        // Act
        boolean exists = userRepository.existsByUserId(nonExistentIdObject);

        // Assert
        assertFalse(exists);
    }

    @Test
    void save_whenNewUser_thenPersistUser() {
        // Act
        User savedUser = userRepository.save(user1);
        // *** FIX: Use findById with the correct ID type (UserId) ***
        User foundUser = userRepository.findById(user1.getUserId()).orElse(null);

        // Assert
        assertNotNull(savedUser);
        assertNotNull(savedUser.getUserId());
        assertEquals(user1.getUserId().getUuid(), savedUser.getUserId().getUuid());
        assertEquals(user1.getUsername(), savedUser.getUsername());
        assertThat(savedUser.getGames()).containsExactlyInAnyOrderElementsOf(user1.getGames());

        assertNotNull(foundUser);
        assertEquals(user1.getEmail(), foundUser.getEmail());
        assertEquals(user1.getUserId(), foundUser.getUserId());
        assertThat(foundUser.getGames()).containsExactlyInAnyOrderElementsOf(user1.getGames());
    }

    @Test
    void save_whenUpdateUser_thenUpdateUser() {
        // Arrange
        userRepository.save(user1); // Save initial state
        String updatedEmail = "updatedRepo@mail.com";
        double updatedBalance = 250.75;
        List<String> updatedGames = new ArrayList<>(List.of("game1", "newGame"));
        user1.setEmail(updatedEmail); // Modify existing managed entity
        user1.setBalance(updatedBalance);
        user1.setGames(updatedGames);

        // Act
        User savedUser = userRepository.save(user1); // Save the updated entity
        // Find using the custom method by String UUID for verification
        User foundUser = userRepository.findUserByUserId_uuid(user1.getUserId().getUuid());

        // Assert
        assertNotNull(savedUser);
        assertEquals(updatedEmail, savedUser.getEmail());
        assertEquals(updatedBalance, savedUser.getBalance());
        assertThat(savedUser.getGames()).containsExactlyInAnyOrderElementsOf(updatedGames);
        assertEquals(user1.getUserId(), savedUser.getUserId());

        assertNotNull(foundUser);
        assertEquals(updatedEmail, foundUser.getEmail());
        assertEquals(updatedBalance, foundUser.getBalance());
        assertThat(foundUser.getGames()).containsExactlyInAnyOrderElementsOf(updatedGames);
        assertEquals(user1.getUserId(), foundUser.getUserId());
    }


    @Test
    void findAll_whenNoUsersExist_thenReturnEmptyList() {
        // Act
        List<User> users = userRepository.findAll();

        // Assert
        assertNotNull(users);
        assertTrue(users.isEmpty());
    }

    @Test
    void delete_whenUserExists_thenRemoveUser() {
        // Arrange
        userRepository.save(user1);
        // Use existsById from JpaRepository, expects UserId
        assertTrue(userRepository.existsById(user1.getUserId()));

        // Act
        userRepository.delete(user1);
        userRepository.flush(); // Ensure delete is executed before checking

        // Assert
        // Use existsById from JpaRepository, expects UserId
        assertFalse(userRepository.existsById(user1.getUserId()));
        assertNull(userRepository.findUserByUserId_uuid(user1.getUserId().getUuid()));
    }
}