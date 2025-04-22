package com.champsoft.usermanagement.Presentation;
import com.champsoft.usermanagement.DataAccess.User;
import com.champsoft.usermanagement.DataAccess.UserId;
import com.champsoft.usermanagement.DataAccess.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class UserRepositoryIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    private User user1;
    private final String USER1_UUID = UUID.randomUUID().toString();
    private final String USER2_UUID = UUID.randomUUID().toString();
    private final String NOT_FOUND_UUID = UUID.randomUUID().toString();

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();

        user1 = new User(new UserId(USER1_UUID), "userOne", "one@email.com", "passOne", 100.0, List.of("order1", "order2"), List.of("game1"));
        User user2 = new User(new UserId(USER2_UUID), "userTwo", "two@email.com", "passTwo", 200.0, new ArrayList<>(), new ArrayList<>());

        userRepository.save(user1);
        userRepository.save(user2);
        userRepository.flush();
    }

    // --- findUserByUserId_uuid ---
    @Test
    void findUserByUserId_uuid_whenExists_thenReturnUser() {
        // Act
        User foundUser = userRepository.findUserByUserId_uuid(USER1_UUID);

        // Assert
        assertNotNull(foundUser);
        assertEquals(USER1_UUID, foundUser.getUserId().getUuid());
        assertEquals("userOne", foundUser.getUsername());
        assertEquals(100.0, foundUser.getBalance());
        assertNotNull(foundUser.getOrders());
        assertEquals(2, foundUser.getOrders().size());
        assertNotNull(foundUser.getGames());
        assertEquals(1, foundUser.getGames().size());
    }

    @Test
    void findUserByUserId_uuid_whenNotExists_thenReturnNull() {
        // Act
        User foundUser = userRepository.findUserByUserId_uuid(NOT_FOUND_UUID);

        // Assert
        assertNull(foundUser);
    }

    // --- existsByUserId ---
    @Test
    void existsByUserId_whenExists_thenReturnTrue() {
        // Arrange
        UserId existingId = new UserId(USER1_UUID);

        // Act
        boolean exists = userRepository.existsByUserId(existingId);

        // Assert
        assertTrue(exists);
    }

    @Test
    void existsByUserId_whenNotExists_thenReturnFalse() {
        // Arrange
        UserId nonExistingId = new UserId(NOT_FOUND_UUID);

        // Act
        boolean exists = userRepository.existsByUserId(nonExistingId);

        // Assert
        assertFalse(exists);
    }

    // --- findAll ---
    @Test
    void findAll_shouldReturnAllUsers() {
        // Act
        List<User> users = userRepository.findAll();

        // Assert
        assertNotNull(users);
        assertEquals(2, users.size());
    }

    // --- save ---
    @Test
    void save_shouldPersistNewUserWithCollections() {
        // Arrange
        String newUuid = UUID.randomUUID().toString();
        User newUser = new User(new UserId(newUuid), "newUser", "new@email.com", "newPass", 50.0, List.of("newOrder"), List.of("newGame1", "newGame2"));

        // Act
        User savedUser = userRepository.save(newUser);
        userRepository.flush();
        User foundUser = userRepository.findUserByUserId_uuid(newUuid);

        // Assert
        assertNotNull(savedUser);
        assertEquals(newUuid, savedUser.getUserId().getUuid());
        assertNotNull(foundUser);
        assertEquals(newUuid, foundUser.getUserId().getUuid());
        assertEquals("newUser", foundUser.getUsername());
        assertEquals(50.0, foundUser.getBalance());
        assertNotNull(foundUser.getOrders());
        assertEquals(1, foundUser.getOrders().size());
        assertEquals("newOrder", foundUser.getOrders().get(0));
        assertNotNull(foundUser.getGames());
        assertEquals(2, foundUser.getGames().size());
        assertTrue(foundUser.getGames().containsAll(List.of("newGame1", "newGame2")));
        assertEquals(3, userRepository.count());
    }

    // --- delete ---
    @Test
    void delete_shouldRemoveUser() {
        // Arrange
        long countBefore = userRepository.count();
        assertTrue(userRepository.existsByUserId(new UserId(USER1_UUID)));

        // Act
        userRepository.delete(user1);
        userRepository.flush();

        // Assert
        long countAfter = userRepository.count();
        assertEquals(countBefore - 1, countAfter);
        assertFalse(userRepository.existsByUserId(new UserId(USER1_UUID)));
        assertNull(userRepository.findUserByUserId_uuid(USER1_UUID));
    }

    // --- Update balance implicitly tested via save ---
    @Test
    void updateBalance_viaSave_shouldPersistChange() {
        // Arrange
        User userToUpdate = userRepository.findUserByUserId_uuid(USER1_UUID);
        assertNotNull(userToUpdate);
        double originalBalance = userToUpdate.getBalance();
        double newBalance = originalBalance + 50.0;

        // Act
        userToUpdate.setBalance(newBalance);
        userRepository.save(userToUpdate);
        userRepository.flush(); // Force changes to DB within the transaction

        User foundUser = userRepository.findUserByUserId_uuid(USER1_UUID);

        // Assert
        assertNotNull(foundUser);
        assertEquals(newBalance, foundUser.getBalance());
    }
}