package com.champsoft.profiledashboardmanagement.DataAccess;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest // Configures H2, focuses on JPA components
@ActiveProfiles("h2") // Ensures our H2 profile (and thus ddl-auto: create-drop) is active
public class UserRepositoryIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    private User user1;
    private final String TEST_UUID_1 = UUID.randomUUID().toString();
    private final String NON_EXISTENT_UUID = UUID.randomUUID().toString();

    @BeforeEach
    void setUp() {
        userRepository.deleteAll(); // Clean before each test

        UserId userId1 = new UserId(TEST_UUID_1);
        user1 = new User(userId1, "Repo User1", "repouser1@example.com", "pass1", 100.0, new ArrayList<>());
        userRepository.save(user1);
    }

    @Test
    void findUserByUserId_uuid_whenUserExists_thenReturnUser() {
        // Act
        User foundUser = userRepository.findUserByUserId_uuid(TEST_UUID_1);

        // Assert
        assertNotNull(foundUser);
        assertEquals(user1.getUserId().getUuid(), foundUser.getUserId().getUuid());
        assertEquals(user1.getUsername(), foundUser.getUsername());
    }

    @Test
    void findUserByUserId_uuid_whenUserDoesNotExist_thenReturnNull() {
        // Act
        User foundUser = userRepository.findUserByUserId_uuid(NON_EXISTENT_UUID);

        // Assert
        assertNull(foundUser);
    }

    @Test
    void existsByUserId_whenUserExists_thenReturnTrue() {
        // Act
        boolean exists = userRepository.existsByUserId(user1.getUserId());

        // Assert
        assertTrue(exists);
    }

    @Test
    void existsByUserId_whenUserDoesNotExist_thenReturnFalse() {
        // Act
        boolean exists = userRepository.existsByUserId(new UserId(NON_EXISTENT_UUID));

        // Assert
        assertFalse(exists);
    }

    @Test
    void saveUser_shouldPersistUser() {
        UserId newUserId = new UserId(UUID.randomUUID().toString());
        User newUser = new User(newUserId, "New Repo User", "newrepo@example.com", "newpass", 50.0, new ArrayList<>());

        User savedUser = userRepository.save(newUser);
        assertNotNull(savedUser);
        assertNotNull(savedUser.getUserId().getUuid()); // ID should be generated/set

        Optional<User> retrievedUser = userRepository.findById(newUserId);
        assertTrue(retrievedUser.isPresent());
        assertEquals("New Repo User", retrievedUser.get().getUsername());
    }

    @Test
    void findAll_shouldReturnAllPersistedUsers() {
        UserId userId2 = new UserId(UUID.randomUUID().toString());
        User user2 = new User(userId2, "Repo User2", "repouser2@example.com", "pass2", 200.0, new ArrayList<>());
        userRepository.save(user2);

        List<User> users = userRepository.findAll();
        assertEquals(2, users.size()); // user1 from setUp, user2 from this test
    }

    @Test
    void deleteUser_shouldRemoveUser() {
        assertTrue(userRepository.existsByUserId(user1.getUserId()));
        userRepository.delete(user1);
        assertFalse(userRepository.existsByUserId(user1.getUserId()));
    }
}