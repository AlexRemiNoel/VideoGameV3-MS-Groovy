package com.champsoft.usermanagement.DataMapper;

import com.champsoft.usermanagement.DataAccess.User;
import com.champsoft.usermanagement.DataAccess.UserId;
import com.champsoft.usermanagement.Presentation.UserResponseModel;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.util.Arrays;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class UserResponseMapperTest {

    private final UserResponseMapper mapper = Mappers.getMapper(UserResponseMapper.class);

    @Test
    public void testUserToUserResponseModel() {
        // Arrange
        String uuid = UUID.randomUUID().toString();
        UserId userId = new UserId();
        userId.setUuid(uuid);

        User user = new User();
        user.setUserId(userId);
        user.setUsername("TestUser");
        user.setEmail("test@example.com");
        user.setBalance(99.99);
        user.setOrders(Arrays.asList("order1", "order2"));
        user.setGames(Arrays.asList("game1", "game2"));

        // Act
        UserResponseModel responseModel = mapper.userToUserResponseModel(user);

        // Assert
        assertNotNull(responseModel);
        assertEquals(uuid, responseModel.getUserId());
        assertEquals("TestUser", responseModel.getUsername());
        assertEquals("test@example.com", responseModel.getEmail());
        assertEquals(99.99, responseModel.getBalance());
        assertEquals(Arrays.asList("order1", "order2"), responseModel.getOrders());
        assertEquals(Arrays.asList("game1", "game2"), responseModel.getGames());
    }
}