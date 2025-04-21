package com.champsoft.usermanagement.Business;

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
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.Test;


import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceUnitTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserResponseMapper userResponseMapper;

    @Mock
    private UserRequestMapper userRequestMapper;

    @InjectMocks
    private UserService userService;

    private final String VALID_USER_ID = "c3540a89-cb47-4c96-888e-ff96708db4d8";
    private final String VALID_USER_USERNAME = "John Doe";
    private final String VALID_USER_PASSWORD = "test123";
    private final Double VALID_BALANCE = 50.0;
    private final String VALID_USER_EMAIL = "john.doe@example.com";
    private final List<String> orderList = new ArrayList<>();
    private final List<String> gamesList = new ArrayList<>();

    private User createTestUser() {
        User user = new User();
        user.setUserId(new UserId(VALID_USER_ID));
        user.setUsername(VALID_USER_USERNAME);
        user.setEmail(VALID_USER_EMAIL);
        user.setPassword(VALID_USER_PASSWORD);
        user.setBalance(VALID_BALANCE);
        user.setOrders(orderList);
        user.setGames(gamesList);
        return user;
    }

    private UserResponseModel createTestUserResponseModel() {
        UserResponseModel user = new UserResponseModel();
        user.setUserId(VALID_USER_ID);
        user.setUsername(VALID_USER_USERNAME);
        user.setEmail(VALID_USER_EMAIL);
        user.setBalance(VALID_BALANCE);
        user.setOrders(orderList);
        user.setGames(gamesList);
        return user;
    }

    private UserRequestModel createTestUserRequestModel() {
        UserRequestModel user = new UserRequestModel();
        user.setUsername(VALID_USER_USERNAME);
        user.setEmail(VALID_USER_EMAIL);
        user.setPassword(VALID_USER_PASSWORD);
        user.setBalance(VALID_BALANCE);
        return user;
    }

    @Test
    public void whenGetUserById_existingUuid_thenReturnUserResponseModel() {
        // Arrange
        String uuid = UUID.randomUUID().toString();
        User user = createTestUser();
        UserResponseModel responseModel = createTestUserResponseModel();
        when(userRepository.findUserByUserId_uuid(uuid)).thenReturn(user);
        when(userResponseMapper.userToUserResponseModel(user)).thenReturn(responseModel);
        //Act
        UserResponseModel result = userService.getUserById(uuid);

        // Assert
        assertNotNull(result);
        assertEquals(responseModel, result);
        verify(userRepository, times(1)).findUserByUserId_uuid(uuid);
        verify(userResponseMapper, times(1)).userToUserResponseModel(user);
    }

    @Test
    public void whenGetUserById_nonExistingUuid_thenThrowNotFoundException() {
        // Arrange
        String uuid = UUID.randomUUID().toString();

        when(userRepository.findUserByUserId_uuid(uuid)).thenReturn(null);

        // Act and Assert
        assertThrows(NotFoundException.class, () -> userService.getUserById(uuid));
        verify(userRepository, times(1)).findUserByUserId_uuid(uuid);
        verify(userResponseMapper, never()).userToUserResponseModel((User) any());
    }

    @Test
    public void whenGetAllUsers_usersExist_thenReturnListOfUserResponseModels() {
        // Arrange
        List<User> users = Arrays.asList(createTestUser(), createTestUser());
        List<UserResponseModel> responseModels = Arrays.asList(createTestUserResponseModel(), createTestUserResponseModel());
        when(userRepository.findAll()).thenReturn(users);
        when(userResponseMapper.userToUserResponseModel(users)).thenReturn(responseModels);

        // Act
        List<UserResponseModel> result = userService.getAllUsers();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(userRepository, times(1)).findAll();
        verify(userResponseMapper, times(1)).userToUserResponseModel(users);
    }

    @Test
    public void whenGetAllUsers_noUsersExist_thenReturnEmptyList() {
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
    public void whenAddUser_validRequestModel_thenReturnUserResponseModel() {
        // Arrange
        UserRequestModel requestModel = createTestUserRequestModel();
        User user = createTestUser();
        UserResponseModel responseModel = createTestUserResponseModel();
        when(userRequestMapper.userRequestModelToUser(requestModel)).thenReturn(user);
        when(userRepository.save(user)).thenReturn(user);
        when(userResponseMapper.userToUserResponseModel(user)).thenReturn(responseModel);

        // Act
        UserResponseModel result = userService.addUser(requestModel);

        // Assert
        assertNotNull(result);
        assertEquals(responseModel.getUsername(), result.getUsername());
        verify(userRequestMapper, times(1)).userRequestModelToUser(requestModel);
        verify(userRepository, times(1)).save(user);
        verify(userResponseMapper, times(1)).userToUserResponseModel(user);
    }

    @Test
    public void whenAddUser_invalidBalance_thenReturnInvalidUserInputException() {
        // Arrange
        UserRequestModel requestModel = createTestUserRequestModel();
        requestModel.setBalance(-5.0);

        User mappedUser = new User();
        mappedUser.setBalance(-5.0);


        when(userRequestMapper.userRequestModelToUser(requestModel)).thenReturn(mappedUser);

        // Act & Assert
        assertThrows(InvalidUserInputException.class, () -> userService.addUser(requestModel));

        verify(userRequestMapper).userRequestModelToUser(requestModel);
        verify(userRepository, never()).save(any());
        verify(userResponseMapper, never()).userToUserResponseModel(mappedUser);
    }

    @Test
    public void whenUpdateUser_validRequestModel_thenReturnUserResponseModel() {
        // Arrange
        UserRequestModel requestModel = createTestUserRequestModel();
        User user = createTestUser();
        UserResponseModel responseModel = createTestUserResponseModel();
        String uuid = user.getUserId().getUuid();

        when(userRepository.findUserByUserId_uuid(uuid)).thenReturn(user);
        when(userRepository.save(user)).thenReturn(user);
        when(userResponseMapper.userToUserResponseModel(user)).thenReturn(responseModel);

        // Act
        UserResponseModel result = userService.updateUser(requestModel, uuid);

        // Assert
        assertNotNull(result);
        assertEquals(responseModel.getUsername(), result.getUsername());
        verify(userRepository, times(1)).save(user);
        verify(userResponseMapper, times(1)).userToUserResponseModel(user);
    }


    @Test
    public void whenDeleteUser_existingUuid_thenUserIsDeleted() {
        // Arrange
        String uuid = UUID.randomUUID().toString();
        User user = createTestUser();
        when(userRepository.findUserByUserId_uuid(uuid)).thenReturn(user);

        // Act
        userService.deleteUser(uuid);

        // Assert
        verify(userRepository, times(1)).findUserByUserId_uuid(uuid);
        verify(userRepository, times(1)).delete(user);
    }
}
