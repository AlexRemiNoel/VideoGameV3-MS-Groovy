package com.champsoft.profiledashboardmanagement.DataMapper;

import com.champsoft.profiledashboardmanagement.DataAccess.User;
import com.champsoft.profiledashboardmanagement.DataAccess.UserId;
import com.champsoft.profiledashboardmanagement.Presentation.UserResponseModel;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class UserResponseMapperTest {

    private final UserResponseMapper mapper = Mappers.getMapper(UserResponseMapper.class);

    private User createTestUser() {
        User user = new User();
        user.setUserId(new UserId(UUID.randomUUID().toString()));
        user.setUsername("TestUser");
        user.setEmail("testuser@example.com");
        user.setPassword("securePassword");
        user.setBalance(100.0);
        user.setGames(List.of("game1", "game2"));
        return user;
    }

    @Test
    void testUserToUserResponseModel() {
        User user = createTestUser();

        UserResponseModel responseModel = mapper.userToUserResponseModel(user);

        assertNotNull(responseModel);
        assertEquals(user.getUserId().getUuid(), responseModel.getUserId());
        assertEquals(user.getUsername(), responseModel.getUsername());

        // These are not mapped in the interface but can be added to mapper if needed.
        assertEquals(100.0, responseModel.getBalance());
        assertEquals("testuser@example.com", responseModel.getEmail());
        assertEquals(List.of("game1", "game2"),responseModel.getGames());
    }

    @Test
    void testUserListToUserResponseModelList() {
        User user1 = createTestUser();
        User user2 = createTestUser();

        List<User> users = Arrays.asList(user1, user2);
        List<UserResponseModel> responseModels = mapper.userToUserResponseModel(users);

        assertNotNull(responseModels);
        assertEquals(2, responseModels.size());
        assertEquals(user1.getUserId().getUuid(), responseModels.get(0).getUserId());
        assertEquals(user2.getUserId().getUuid(), responseModels.get(1).getUserId());
    }
}
