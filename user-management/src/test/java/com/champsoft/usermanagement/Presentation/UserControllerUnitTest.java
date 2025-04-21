package com.champsoft.usermanagement.Presentation;



import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.champsoft.usermanagement.BusinessLogic.UserService;
import com.champsoft.usermanagement.utils.exceptions.InvalidUserInputException;
import com.champsoft.usermanagement.utils.exceptions.NotFoundException;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;


import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserControllerUnitTest {
    private final String VALID_USER_ID = "c3540a89-cb47-4c96-888e-ff96708db4d8";
    private final String NOT_FOUND_USER_ID = "c3540a89-cb47-4c96-888eff96708db4d0";
    private final String INVALID_USER_ID = "c3540a89-cb47-4c96-888e-ff96708db4d";
    @Mock
    UserService userService;
    @InjectMocks
    UserController userController;
    @Test
    public void whenNoUsersExist_ThenReturnEmptyList() {
        //arrange
        when(userService.getAllUsers()).thenReturn(Collections.emptyList());
        //act
        ResponseEntity<List<UserResponseModel>> userResponseEntity = userController.getUsers();
        //assert
        assertNotNull(userResponseEntity);
        assertEquals(userResponseEntity.getStatusCode(), HttpStatus.OK);
        assertArrayEquals(userResponseEntity.getBody().toArray(),
                new ArrayList<UserResponseModel>().toArray());
        verify(userService, times(1)).getAllUsers();
    }

    @Test
    public void whenUserExists_ThenReturnUser()
    {
        //arrange
        UserResponseModel userResponseModel = buildUserResponseModel();
        when(userService.getUserById(VALID_USER_ID)).thenReturn(userResponseModel);
        //act
        ResponseEntity<UserResponseModel> userResponseEntity =
                userController.getUserById(VALID_USER_ID);

        //assert
        assertNotNull(userResponseEntity);
        assertEquals(userResponseEntity.getStatusCode(), HttpStatus.OK);
        assertNotNull(userResponseEntity.getBody());
        assertEquals(userResponseEntity.getBody(), userResponseModel);
        verify(userService, times(1)).getUserById(VALID_USER_ID);
    }

    @Test
    public void whenUserIdInvalidOnGet_ThenThrowInvalidInputException() {
        //arrange
        when(userService.getUserById(INVALID_USER_ID)).thenThrow(new InvalidUserInputException("Invalid user id: " + INVALID_USER_ID));
        //act and assert

        InvalidUserInputException exception = assertThrowsExactly(InvalidUserInputException.class, () -> {
            userController.getUserById(INVALID_USER_ID);
        });
        // Assert exception message
        // the correct exception message should be : "Invalid user id: " + INVALID_USER_ID
        assertEquals("Invalid user id: " + INVALID_USER_ID, exception.getMessage());
        verify(userService, times(0)).getAllUsers();
    }

    @Test
    public void whenUserNotFoundOnGet_ThenThrowNotFoundException() {
        //arrange
        when(userService.getUserById(NOT_FOUND_USER_ID))
                .thenThrow(new NotFoundException("Unknown userId: " + NOT_FOUND_USER_ID));
        //act
        NotFoundException exception = assertThrowsExactly(NotFoundException.class, () -> {
            userController.getUserById(NOT_FOUND_USER_ID);
        });
        //assert
        assertEquals( "Unknown userId: " + NOT_FOUND_USER_ID, exception.getMessage());
        verify(userService, times( 1)).getUserById(NOT_FOUND_USER_ID);
    }

    @Test
    public void whenUserValid_ThenReturnNewUser() {
        //arrange
        UserRequestModel userRequestModel = buildUserRequestModel();
        UserResponseModel userResponseModel = buildUserResponseModel();
        when(userService.addUser(userRequestModel)).thenReturn(userResponseModel);
        //act
        ResponseEntity<UserResponseModel> userResponseEntity =
                userController.addUser(userRequestModel);

        //assert
        assertNotNull(userResponseEntity);
        assertEquals(userResponseEntity.getStatusCode(), HttpStatus.CREATED);
        assertNotNull(userResponseEntity.getBody());
        assertEquals(userResponseEntity.getBody(), userResponseModel);
        verify(userService, times(  1)).addUser(userRequestModel);
    }

    @Test
    public void whenUserExists_ThenReturnUpdatedUser() {
        //arrange
        UserRequestModel userRequestModel = buildUserRequestModel( "Betty");
        UserResponseModel userResponseModel = buildUserResponseModel("Betty");
        when(userService.updateUser(userRequestModel, VALID_USER_ID)).thenReturn(userResponseModel);
        //act
        ResponseEntity<UserResponseModel> userResponseEntity =
                userController.updateUser(userRequestModel, VALID_USER_ID);
        //assert
        assertNotNull(userResponseEntity);
        assertEquals(userResponseEntity.getStatusCode(), HttpStatus.OK);
        assertNotNull(userResponseEntity.getBody());
        assertEquals(userResponseEntity.getBody(), userResponseModel);
        verify(userService, times( 1)).updateUser(userRequestModel, VALID_USER_ID);
    }

    @Test
    public void whenUserDoesNotExistOnUpdate_ThenThrowNotFoundException() {
        //arrange
        UserRequestModel userRequestModel = buildUserRequestModel( "Betty");
        when(userService.updateUser(userRequestModel, NOT_FOUND_USER_ID)).thenThrow(new NotFoundException("Unknown userId: " + NOT_FOUND_USER_ID));
        //act
        NotFoundException exception = assertThrowsExactly(NotFoundException.class, () -> {
                userController.updateUser(userRequestModel, NOT_FOUND_USER_ID);
        });
        //assert
        assertEquals("Unknown userId: "+ NOT_FOUND_USER_ID, exception.getMessage());
        verify(userService, times( 1)).updateUser(userRequestModel, NOT_FOUND_USER_ID);
    }

    @Test
    public void whenUserExists_ThenDeleteUser() {
        //arrange
        doNothing().when(userService).deleteUser(VALID_USER_ID);

        //act
        ResponseEntity<Void> responseEntity = userController.deleteUser(VALID_USER_ID);

        //assert
        assertNotNull(responseEntity);
        assertEquals(responseEntity.getStatusCode(), HttpStatus.NO_CONTENT);
        verify(userService, times(1)).deleteUser(VALID_USER_ID);
    }

    @Test
    public void whenUserDoesNotExistOnDelete_ThenThrowNotFoundException() {
        //arrange
        doThrow(new NotFoundException("Unknown userId: " + NOT_FOUND_USER_ID))
            .when(userService).deleteUser(NOT_FOUND_USER_ID);

        //act
        NotFoundException exception = assertThrowsExactly(NotFoundException.class, () -> {
            userController.deleteUser(NOT_FOUND_USER_ID);
        });

        //assert
        assertEquals("Unknown userId: "+ NOT_FOUND_USER_ID, exception.getMessage());
        verify(userService, times( 1)).deleteUser(NOT_FOUND_USER_ID);
    }


    public UserResponseModel buildUserResponseModel() {
        return buildUserResponseModel("John Doe");
    }

    public UserResponseModel buildUserResponseModel(String username) {
        UserResponseModel userResponseModel = new UserResponseModel();
        userResponseModel.setUserId("valid-user-id"); // Set the user ID
        userResponseModel.setUsername(username); // Set user name
        userResponseModel.setBalance(50);
        userResponseModel.setEmail("john.doe@example.com"); // Set user email
        // Set any other required fields here
        return userResponseModel;
    }

    public UserRequestModel buildUserRequestModel(){
        return buildUserRequestModel("John Doe");
    }

    public UserRequestModel buildUserRequestModel(String username){
        UserRequestModel userRequestModel = new UserRequestModel();
        userRequestModel.setUsername(username);
        userRequestModel.setPassword("test123");
        userRequestModel.setEmail("john.doe@example.com");
        return userRequestModel;
    }
}
