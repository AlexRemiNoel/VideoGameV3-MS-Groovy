package com.champsoft.usermanagement.testing;

import com.champsoft.usermanagement.BusinessLogic.UserService;
import com.champsoft.usermanagement.DataAccess.User;
import com.champsoft.usermanagement.DataAccess.UserId;
import com.champsoft.usermanagement.DataAccess.UserRepository;
import com.champsoft.usermanagement.DataMapper.UserRequestMapper;
import com.champsoft.usermanagement.DataMapper.UserResponseMapper;
import com.champsoft.usermanagement.Presentation.UserRequestModel;
import com.champsoft.usermanagement.Presentation.UserResponseModel;
import com.champsoft.usermanagement.utils.exceptions.InvalidUserInputException;
import com.champsoft.usermanagement.utils.exceptions.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceUnitTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserResponseMapper userResponseMapper;

    @Mock
    private UserRequestMapper userRequestMapper;

    @InjectMocks
    private UserService userService;

    private User user1;
    private User user2;
    private UserRequestModel userRequestModel;
    private UserResponseModel userResponseModel1;
    private UserResponseModel userResponseModel2;
    private String userId1;
    private String userId2;
    private String nonExistentUserId;


    @BeforeEach
    void setUp() {
        userId1 = UUID.randomUUID().toString();
        userId2 = UUID.randomUUID().toString();
        nonExistentUserId = UUID.randomUUID().toString();

        user1 = new User(new UserId(userId1), "user1", "user1@test.com", "pass1", 100.0, new ArrayList<>(), new ArrayList<>());
        user2 = new User(new UserId(userId2), "user2", "user2@test.com", "pass2", 50.0, new ArrayList<>(), new ArrayList<>());
        userRequestModel = new UserRequestModel("newUser", "new@test.com", "newPass", 25.0);
        userResponseModel1 = new UserResponseModel(userId1, "user1", "user1@test.com", 100.0, new ArrayList<>(), new ArrayList<>());
        userResponseModel2 = new UserResponseModel(userId2, "user2", "user2@test.com", 50.0, new ArrayList<>(), new ArrayList<>());
    }

    @Test
    void getAllUsers_whenUsersExist_thenReturnUserList() {
        // Arrange
        List<User> users = Arrays.asList(user1, user2);
        List<UserResponseModel> responseModels = Arrays.asList(userResponseModel1, userResponseModel2);
        when(userRepository.findAll()).thenReturn(users);
        when(userResponseMapper.userToUserResponseModel(users)).thenReturn(responseModels);

        // Act
        List<UserResponseModel> result = userService.getAllUsers();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(responseModels, result);
        verify(userRepository, times(1)).findAll();
        verify(userResponseMapper, times(1)).userToUserResponseModel(users);
    }

    @Test
    void getAllUsers_whenNoUsersExist_thenReturnEmptyList() {
        // Arrange
        when(userRepository.findAll()).thenReturn(Collections.emptyList());
        when(userResponseMapper.userToUserResponseModel(Collections.emptyList())).thenReturn(Collections.emptyList());

        // Act
        List<UserResponseModel> result = userService.getAllUsers();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(userRepository, times(1)).findAll();
        verify(userResponseMapper, times(1)).userToUserResponseModel(Collections.emptyList());
    }

    @Test
    void getUserById_whenUserExists_thenReturnUser() {
        // Arrange
        when(userRepository.findUserByUserId_uuid(userId1)).thenReturn(user1);
        when(userResponseMapper.userToUserResponseModel(user1)).thenReturn(userResponseModel1);

        // Act
        UserResponseModel result = userService.getUserById(userId1);

        // Assert
        assertNotNull(result);
        assertEquals(userResponseModel1, result);
        verify(userRepository, times(1)).findUserByUserId_uuid(userId1);
        verify(userResponseMapper, times(1)).userToUserResponseModel(user1);
    }

    @Test
    void getUserById_whenUserDoesNotExist_thenThrowNotFoundException() {
        // Arrange
        when(userRepository.findUserByUserId_uuid(nonExistentUserId)).thenReturn(null);

        // Act & Assert
        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            userService.getUserById(nonExistentUserId);
        });
        assertEquals("Unknown userId: " + nonExistentUserId, exception.getMessage());
        verify(userRepository, times(1)).findUserByUserId_uuid(nonExistentUserId);
        verify(userResponseMapper, never()).userToUserResponseModel(any(User.class));
    }

    @Test
    void addUser_whenValidRequest_thenReturnAddedUser() {
        // Arrange
        User newUser = new User(new UserId(UUID.randomUUID().toString()), "newUser", "new@test.com", "newPass", 25.0, new ArrayList<>(), new ArrayList<>());
        UserResponseModel newUserResponse = new UserResponseModel(newUser.getUserId().getUuid(), newUser.getUsername(), newUser.getEmail(), newUser.getBalance(), newUser.getOrders(), newUser.getGames());

        when(userRequestMapper.userRequestModelToUser(userRequestModel)).thenReturn(newUser);
        when(userRepository.save(newUser)).thenReturn(newUser);
        when(userResponseMapper.userToUserResponseModel(newUser)).thenReturn(newUserResponse);

        // Act
        UserResponseModel result = userService.addUser(userRequestModel);

        // Assert
        assertNotNull(result);
        assertEquals(newUserResponse, result);
        verify(userRequestMapper, times(1)).userRequestModelToUser(userRequestModel);
        verify(userRepository, times(1)).save(newUser);
        verify(userResponseMapper, times(1)).userToUserResponseModel(newUser);
    }

    @Test
    void addUser_whenNegativeBalance_thenThrowInvalidUserInputException() {
        // Arrange
        UserRequestModel invalidRequest = new UserRequestModel("invalidUser", "invalid@test.com", "pass", -10.0);
        User invalidUser = new User(new UserId(UUID.randomUUID().toString()), "invalidUser", "invalid@test.com", "pass", -10.0, new ArrayList<>(), new ArrayList<>());
        when(userRequestMapper.userRequestModelToUser(invalidRequest)).thenReturn(invalidUser);

        // Act & Assert
        InvalidUserInputException exception = assertThrows(InvalidUserInputException.class, () -> {
            userService.addUser(invalidRequest);
        });
        assertEquals("Invalid negative balance: -10.0", exception.getMessage());
        verify(userRequestMapper, times(1)).userRequestModelToUser(invalidRequest);
        verify(userRepository, never()).save(any(User.class));
        verify(userResponseMapper, never()).userToUserResponseModel(any(User.class));
    }


    @Test
    void updateUser_whenUserExists_thenReturnUpdatedUser() {
        // Arrange
        UserRequestModel updateRequest = new UserRequestModel("updatedUser", "updated@test.com", "updatedPass", 150.0); // Balance update ignored here
        User updatedUser = new User(new UserId(userId1), "updatedUser", "updated@test.com", "updatedPass", 100.0, new ArrayList<>(), new ArrayList<>()); // Balance unchanged by this method
        UserResponseModel updatedResponse = new UserResponseModel(userId1, "updatedUser", "updated@test.com", 100.0, new ArrayList<>(), new ArrayList<>());

        when(userRepository.findUserByUserId_uuid(userId1)).thenReturn(user1);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(userResponseMapper.userToUserResponseModel(any(User.class))).thenReturn(updatedResponse);

        // Act
        UserResponseModel result = userService.updateUser(updateRequest, userId1);

        // Assert
        assertNotNull(result);
        assertEquals(updatedResponse, result);
        assertEquals("updatedUser", user1.getUsername()); // Verify original object was modified
        assertEquals("updated@test.com", user1.getEmail());
        assertEquals("updatedPass", user1.getPassword());
        assertEquals(100.0, user1.getBalance()); // Balance should NOT be updated by this method
        verify(userRepository, times(1)).findUserByUserId_uuid(userId1);
        verify(userRepository, times(1)).save(user1);
        verify(userResponseMapper, times(1)).userToUserResponseModel(user1);
    }

    @Test
    void updateUser_whenUserDoesNotExist_thenThrowNotFoundException() {
        // Arrange
        UserRequestModel updateRequest = new UserRequestModel("updatedUser", "updated@test.com", "updatedPass", 150.0);
        when(userRepository.findUserByUserId_uuid(nonExistentUserId)).thenReturn(null);

        // Act & Assert
        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            userService.updateUser(updateRequest, nonExistentUserId);
        });
        assertEquals("Unknown userId: " + nonExistentUserId, exception.getMessage());
        verify(userRepository, times(1)).findUserByUserId_uuid(nonExistentUserId);
        verify(userRepository, never()).save(any(User.class));
        verify(userResponseMapper, never()).userToUserResponseModel(any(User.class));
    }

    @Test
    void deleteUser_whenUserExists_thenDeleteSuccessfully() {
        // Arrange
        when(userRepository.findUserByUserId_uuid(userId1)).thenReturn(user1);
        doNothing().when(userRepository).delete(user1);

        // Act
        assertDoesNotThrow(() -> {
            userService.deleteUser(userId1);
        });

        // Assert
        verify(userRepository, times(1)).findUserByUserId_uuid(userId1);
        verify(userRepository, times(1)).delete(user1);
    }

    @Test
    void deleteUser_whenUserDoesNotExist_thenThrowNotFoundException() {
        // Arrange
        when(userRepository.findUserByUserId_uuid(nonExistentUserId)).thenReturn(null);

        // Act & Assert
        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            userService.deleteUser(nonExistentUserId);
        });
        assertEquals("Unknown userId: " + nonExistentUserId, exception.getMessage());
        verify(userRepository, times(1)).findUserByUserId_uuid(nonExistentUserId);
        verify(userRepository, never()).delete(any(User.class));
    }

    @Test
    void updateUserBalance_whenUserExistsAndBalanceValid_thenReturnUpdatedUser() {
        // Arrange
        double newBalance = 200.50;
        User userWithUpdatedBalance = new User(new UserId(userId1), "user1", "user1@test.com", "pass1", newBalance, new ArrayList<>(), new ArrayList<>());
        UserResponseModel updatedResponse = new UserResponseModel(userId1, "user1", "user1@test.com", newBalance, new ArrayList<>(), new ArrayList<>());

        when(userRepository.findUserByUserId_uuid(userId1)).thenReturn(user1);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0)); // Mock save to return updated user
        when(userResponseMapper.userToUserResponseModel(any(User.class))).thenReturn(updatedResponse); // Map the updated user


        // Act
        UserResponseModel result = userService.updateUserBalance(userId1, newBalance);

        // Assert
        assertNotNull(result);
        assertEquals(updatedResponse, result);
        assertEquals(newBalance, user1.getBalance()); // Check if balance was set on the original object before save
        verify(userRepository, times(1)).findUserByUserId_uuid(userId1);
        verify(userRepository, times(1)).save(user1); // Verify save was called with the updated user1
        verify(userResponseMapper, times(1)).userToUserResponseModel(user1);
    }

    @Test
    void updateUserBalance_whenUserExistsAndBalanceInvalid_thenThrowInvalidUserInputException() {
        // Arrange
        double invalidBalance = -50.0;
        when(userRepository.findUserByUserId_uuid(userId1)).thenReturn(user1);

        // Act & Assert
        InvalidUserInputException exception = assertThrows(InvalidUserInputException.class, () -> {
            userService.updateUserBalance(userId1, invalidBalance);
        });
        assertEquals("Invalid negative balance: " + invalidBalance, exception.getMessage());
        verify(userRepository, times(1)).findUserByUserId_uuid(userId1);
        verify(userRepository, never()).save(any(User.class));
        verify(userResponseMapper, never()).userToUserResponseModel(any(User.class));
    }

    @Test
    void updateUserBalance_whenUserDoesNotExist_thenThrowNotFoundException() {
        // Arrange
        double newBalance = 200.50;
        when(userRepository.findUserByUserId_uuid(nonExistentUserId)).thenReturn(null);

        // Act & Assert
        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            userService.updateUserBalance(nonExistentUserId, newBalance);
        });
        assertEquals("Unknown userId: " + nonExistentUserId, exception.getMessage());
        verify(userRepository, times(1)).findUserByUserId_uuid(nonExistentUserId);
        verify(userRepository, never()).save(any(User.class));
        verify(userResponseMapper, never()).userToUserResponseModel(any(User.class));
    }
}