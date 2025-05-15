package com.champsoft.profiledashboardmanagement.Business;

import com.champsoft.profiledashboardmanagement.BusinessLogic.UserService;
import com.champsoft.profiledashboardmanagement.DataAccess.User;
import com.champsoft.profiledashboardmanagement.DataAccess.UserId;
import com.champsoft.profiledashboardmanagement.DataAccess.UserRepository;
import com.champsoft.profiledashboardmanagement.DataMapper.UserRequestMapper;
import com.champsoft.profiledashboardmanagement.DataMapper.UserResponseMapper;
import com.champsoft.profiledashboardmanagement.Presentation.UserRequestModel;
import com.champsoft.profiledashboardmanagement.Presentation.UserResponseModel;
import com.champsoft.profiledashboardmanagement.utils.exceptions.InvalidUserInputException;
import com.champsoft.profiledashboardmanagement.utils.exceptions.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class) // Use this for pure Mockito tests
// @SpringBootTest(classes = UserService.class) // Or this if you want a minimal Spring context
public class UserServiceUnitTest {

    @Mock // @MockBean if using @SpringBootTest
    private UserRepository userRepository;

    @Mock // @MockBean if using @SpringBootTest
    private UserResponseMapper userResponseMapper;

    @Mock // @MockBean if using @SpringBootTest
    private UserRequestMapper userRequestMapper;

    @InjectMocks // @Autowired if using @SpringBootTest
    private UserService userService;

    private User user1, user2;
    private UserResponseModel responseModel1, responseModel2;
    private UserRequestModel requestModel;
    private final String TEST_UUID_1 = "uuid-1";
    private final String TEST_UUID_2 = "uuid-2";
    private final String NON_EXISTENT_UUID = "uuid-non-existent";

    @BeforeEach
    void setUp() {
        UserId userId1 = new UserId(TEST_UUID_1);
        user1 = new User(userId1, "user1", "user1@example.com", "pass1", 100.0, new ArrayList<>());
        responseModel1 = new UserResponseModel(TEST_UUID_1, "user1", "user1@example.com", 100.0, new ArrayList<>());

        UserId userId2 = new UserId(TEST_UUID_2);
        user2 = new User(userId2, "user2", "user2@example.com", "pass2", 200.0, new ArrayList<>());
        responseModel2 = new UserResponseModel(TEST_UUID_2, "user2", "user2@example.com", 200.0, new ArrayList<>());

        requestModel = new UserRequestModel("newUser", "new@example.com", "newPass", 50.0);
    }

    @Test
    void getAllUsers_whenUsersExist_thenReturnListOfUserResponseModels() {
        // Arrange
        when(userRepository.findAll()).thenReturn(Arrays.asList(user1, user2));
        when(userResponseMapper.userToUserResponseModel(Arrays.asList(user1, user2)))
                .thenReturn(Arrays.asList(responseModel1, responseModel2));

        // Act
        List<UserResponseModel> result = userService.getAllUsers();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(responseModel1.getUsername(), result.get(0).getUsername());
        verify(userRepository, times(1)).findAll();
        verify(userResponseMapper, times(1)).userToUserResponseModel(Arrays.asList(user1, user2));
    }

    @Test
    void getAllUsers_whenNoUsersExist_thenReturnEmptyList() {
        // Arrange
        when(userRepository.findAll()).thenReturn(Collections.emptyList());
        when(userResponseMapper.userToUserResponseModel(Collections.emptyList()))
                .thenReturn(Collections.emptyList());

        // Act
        List<UserResponseModel> result = userService.getAllUsers();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(userRepository, times(1)).findAll();
        verify(userResponseMapper, times(1)).userToUserResponseModel(Collections.emptyList());
    }

    @Test
    void getUserById_whenUserExists_thenReturnUserResponseModel() {
        // Arrange
        when(userRepository.findUserByUserId_uuid(TEST_UUID_1)).thenReturn(user1);
        when(userResponseMapper.userToUserResponseModel(user1)).thenReturn(responseModel1);

        // Act
        UserResponseModel result = userService.getUserById(TEST_UUID_1);

        // Assert
        assertNotNull(result);
        assertEquals(responseModel1.getUsername(), result.getUsername());
        verify(userRepository, times(1)).findUserByUserId_uuid(TEST_UUID_1);
        verify(userResponseMapper, times(1)).userToUserResponseModel(user1);
    }

    @Test
    void getUserById_whenUserDoesNotExist_thenThrowNotFoundException() {
        // Arrange
        when(userRepository.findUserByUserId_uuid(NON_EXISTENT_UUID)).thenReturn(null);

        // Act & Assert
        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            userService.getUserById(NON_EXISTENT_UUID);
        });
        assertEquals("Unknown userId: " + NON_EXISTENT_UUID, exception.getMessage());
        verify(userRepository, times(1)).findUserByUserId_uuid(NON_EXISTENT_UUID);
        verify(userResponseMapper, never()).userToUserResponseModel(any(User.class));
    }

    @Test
    void addUser_whenValidRequest_thenReturnUserResponseModel() {
        // Arrange
        User newUser = new User(new UserId("new-uuid"), "newUser", "new@example.com", "newPass", 50.0, new ArrayList<>());
        UserResponseModel newResponseModel = new UserResponseModel("new-uuid", "newUser", "new@example.com", 50.0, new ArrayList<>());

        when(userRequestMapper.userRequestModelToUser(requestModel)).thenReturn(newUser);
        when(userRepository.save(newUser)).thenReturn(newUser); // Assume save returns the saved entity
        when(userResponseMapper.userToUserResponseModel(newUser)).thenReturn(newResponseModel);

        // Act
        UserResponseModel result = userService.addUser(requestModel);

        // Assert
        assertNotNull(result);
        assertEquals(newResponseModel.getUsername(), result.getUsername());
        verify(userRequestMapper, times(1)).userRequestModelToUser(requestModel);
        verify(userRepository, times(1)).save(newUser);
        verify(userResponseMapper, times(1)).userToUserResponseModel(newUser);
    }

    @Test
    void addUser_whenNegativeBalance_thenThrowInvalidUserInputException() {
        // Arrange
        UserRequestModel invalidRequestModel = new UserRequestModel("testUser", "test@example.com", "pass", -10.0);
        User userWithNegativeBalance = new User(new UserId("uuid-neg"), "testUser", "test@example.com", "pass", -10.0, new ArrayList<>());
        when(userRequestMapper.userRequestModelToUser(invalidRequestModel)).thenReturn(userWithNegativeBalance);


        // Act & Assert
        InvalidUserInputException exception = assertThrows(InvalidUserInputException.class, () -> {
            userService.addUser(invalidRequestModel);
        });
        assertEquals("Invalid negative balance: -10.0", exception.getMessage());
        verify(userRequestMapper, times(1)).userRequestModelToUser(invalidRequestModel);
        verify(userRepository, never()).save(any(User.class));
        verify(userResponseMapper, never()).userToUserResponseModel(any(User.class));
    }




    @Test
    void updateUser_whenUserDoesNotExist_thenThrowNotFoundException() {
        // Arrange
        UserRequestModel updateRequest = new UserRequestModel("any", "any@example.com", "any", 0);
        when(userRepository.findUserByUserId_uuid(NON_EXISTENT_UUID)).thenReturn(null);

        // Act & Assert
        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            userService.updateUser(updateRequest, NON_EXISTENT_UUID);
        });
        assertEquals("Unknown userId: " + NON_EXISTENT_UUID, exception.getMessage());
        verify(userRepository, times(1)).findUserByUserId_uuid(NON_EXISTENT_UUID);
        verify(userRepository, never()).save(any(User.class));
        verify(userResponseMapper, never()).userToUserResponseModel(any(User.class));
    }

    @Test
    void deleteUser_whenUserExists_thenDeleteUser() {
        // Arrange
        when(userRepository.findUserByUserId_uuid(TEST_UUID_1)).thenReturn(user1);
        doNothing().when(userRepository).delete(user1);

        // Act
        userService.deleteUser(TEST_UUID_1);

        // Assert
        verify(userRepository, times(1)).findUserByUserId_uuid(TEST_UUID_1);
        verify(userRepository, times(1)).delete(user1);
    }

    @Test
    void deleteUser_whenUserDoesNotExist_thenThrowNotFoundException() {
        // Arrange
        when(userRepository.findUserByUserId_uuid(NON_EXISTENT_UUID)).thenReturn(null);

        // Act & Assert
        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            userService.deleteUser(NON_EXISTENT_UUID);
        });
        assertEquals("Unknown userId: " + NON_EXISTENT_UUID, exception.getMessage());
        verify(userRepository, times(1)).findUserByUserId_uuid(NON_EXISTENT_UUID);
        verify(userRepository, never()).delete(any(User.class));
    }

    @Test
    void updateUserBalance_whenUserExistsAndValidBalance_thenReturnUpdatedUserResponseModel() {
        // Arrange
        double newBalance = 250.0;
        User existingUser = user1; // Original balance 100.0
        User userWithUpdatedBalance = new User(existingUser.getUserId(), existingUser.getUsername(), existingUser.getEmail(), existingUser.getPassword(), newBalance, existingUser.getGames());
        UserResponseModel responseWithUpdatedBalance = new UserResponseModel(TEST_UUID_1, existingUser.getUsername(), existingUser.getEmail(), newBalance, existingUser.getGames());

        when(userRepository.findUserByUserId_uuid(TEST_UUID_1)).thenReturn(existingUser);
        when(userRepository.save(any(User.class))).thenReturn(userWithUpdatedBalance); // Ensure the saved user has the new balance
        when(userResponseMapper.userToUserResponseModel(userWithUpdatedBalance)).thenReturn(responseWithUpdatedBalance);

        // Act
        UserResponseModel result = userService.updateUserBalance(TEST_UUID_1, newBalance);

        // Assert
        assertNotNull(result);
        assertEquals(newBalance, result.getBalance());
        verify(userRepository, times(1)).findUserByUserId_uuid(TEST_UUID_1);
        verify(userRepository, times(1)).save(argThat(savedUser ->
                savedUser.getBalance() == newBalance &&
                        savedUser.getUserId().getUuid().equals(TEST_UUID_1)
        ));
        verify(userResponseMapper, times(1)).userToUserResponseModel(userWithUpdatedBalance);
    }

    @Test
    void updateUserBalance_whenUserDoesNotExist_thenThrowNotFoundException() {
        // Arrange
        double newBalance = 100.0;
        when(userRepository.findUserByUserId_uuid(NON_EXISTENT_UUID)).thenReturn(null);

        // Act & Assert
        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            userService.updateUserBalance(NON_EXISTENT_UUID, newBalance);
        });
        assertEquals("Unknown userId: " + NON_EXISTENT_UUID, exception.getMessage());
        verify(userRepository, times(1)).findUserByUserId_uuid(NON_EXISTENT_UUID);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void updateUserBalance_whenNegativeBalance_thenThrowInvalidUserInputException() {
        // Arrange
        double negativeBalance = -50.0;
        when(userRepository.findUserByUserId_uuid(TEST_UUID_1)).thenReturn(user1); // Assume user1 exists

        // Act & Assert
        InvalidUserInputException exception = assertThrows(InvalidUserInputException.class, () -> {
            userService.updateUserBalance(TEST_UUID_1, negativeBalance);
        });
        assertEquals("Invalid negative balance: " + negativeBalance, exception.getMessage());
        verify(userRepository, times(1)).findUserByUserId_uuid(TEST_UUID_1); // findUserByUuidOrThrow is called
        verify(userRepository, never()).save(any(User.class)); // Save should not be called
    }
}