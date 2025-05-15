package com.champsoft.profiledashboardmanagement.Presentation;

import com.champsoft.profiledashboardmanagement.BusinessLogic.UserService;

import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;

@SpringBootTest(classes = UserController.class) // Loads only UserController and its mocked dependencies
public class UserControllerUnitTest {

    @Mock
    private UserService userService;

    @Autowired
    private UserController userController;

    private UserResponseModel responseModel1, responseModel2;
    private UserRequestModel requestModel;
    private final String TEST_UUID_1 = "uuid-1";

    @BeforeEach
    void setUp() {
        responseModel1 = new UserResponseModel(TEST_UUID_1, "user1", "user1@example.com", 100.0, new ArrayList<>());
        responseModel2 = new UserResponseModel("uuid-2", "user2", "user2@example.com", 200.0, new ArrayList<>());
        requestModel = new UserRequestModel("newUser", "new@example.com", "newPass", 50.0);
    }
}