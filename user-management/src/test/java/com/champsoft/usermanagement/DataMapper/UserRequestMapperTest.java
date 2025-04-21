package com.champsoft.usermanagement.DataMapper;

import com.champsoft.usermanagement.DataAccess.User;
import com.champsoft.usermanagement.Presentation.UserRequestModel;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
public class UserRequestMapperTest {
    @Autowired
    private UserRequestMapper userRequestMapper;

    @Test
    public void testUserRequestModelToUser() {
        // Arrange
        UserRequestModel requestModel = new UserRequestModel(
                "TestUser",
                "test@example.com",
                "securePassword",
                100.0
        );

        // Act
        User user = userRequestMapper.userRequestModelToUser(requestModel);

        // Assert
        assertNotNull(user);
        assertNotNull(user.getUserId());
        assertNotNull(user.getUserId().getUuid());
        assertEquals("TestUser", user.getUsername());
        assertEquals("test@example.com", user.getEmail());
        assertEquals("securePassword", user.getPassword());
        assertEquals(100.0, user.getBalance());
    }

    @Test
    public void testUserRequestModelToUser_ListMapping() {
        // Arrange
        UserRequestModel user1 = new UserRequestModel("User1", "user1@example.com", "pass1", 10.0);
        UserRequestModel user2 = new UserRequestModel("User2", "user2@example.com", "pass2", 20.0);
        List<UserRequestModel> requestModels = Arrays.asList(user1, user2);

        // Act
        List<UserRequestModel> users = userRequestMapper.userRequestModelToUser(requestModels);

        // Assert
        assertNotNull(users);
        assertEquals(2, users.size());

        assertEquals("User1", users.get(0).getUsername());
        assertEquals("user1@example.com", users.get(0).getEmail());
        assertEquals("pass1", users.get(0).getPassword());
        assertEquals(10.0, users.get(0).getBalance());

        assertEquals("User2", users.get(1).getUsername());
        assertEquals("user2@example.com", users.get(1).getEmail());
        assertEquals("pass2", users.get(1).getPassword());
        assertEquals(20.0, users.get(1).getBalance());
    }
}
