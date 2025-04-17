package com.champsoft.usermanagement.DataAccess;

import jakarta.transaction.Transactional;
import com.champsoft.usermanagement.DataAccess.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.http.HttpStatus;
import java.util.ArrayList;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.samePropertyValuesAs;
import static org.junit.jupiter.api.Assertions.*;
@DataJpaTest
public class UserRepositoryIntegrationTest {
    private final String NOT_FOUND_USER_ID = "c3540a89-cb47-4c96-888e-ff96708db4d0";
    private final String VALID_USER_ID = "c3540a89-cb47-4c96-888e-ff96708db4d8";
    private final String VALID_USER_USERNAME = "John Doe";
    private final String VALID_USER_PASSWORD = "test123";
    private final Double VALID_BALANCE = 50.0;
    private final String VALID_USER_EMAIL = "john.doe@example.com";
    private final List<String> orderList = new ArrayList<>();
    private final List<String> gameList = new ArrayList<>();

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setup() {
        User user = new User();
        user.setUserId(new UserId(VALID_USER_ID));
        user.setUsername(VALID_USER_USERNAME);
        user.setEmail(VALID_USER_EMAIL);
        user.setPassword(VALID_USER_PASSWORD);
        user.setBalance(VALID_BALANCE);
        user.setOrders(orderList);
        user.setGames(gameList);
        userRepository.save(user);
    }

    @Test
    public void whenUserDoesNotExist_ReturnNull() {
        //arrange
        //act
        User user = userRepository.findUserByUserId_uuid(NOT_FOUND_USER_ID);
        //assert
        assertNull(user);
    }

    @Test
    public void whenUserExist_ReturnUserById() {
        //arrange

        User user1 = new User(
                new UserId(VALID_USER_ID),
                VALID_USER_USERNAME,
                VALID_USER_EMAIL,
                VALID_USER_PASSWORD,
                VALID_BALANCE,
                orderList,
                gameList);
        //act
        User user = userRepository.findUserByUserId_uuid( VALID_USER_ID );

        //assert
        assertNotNull(user);
        assertEquals(user1.getUserId(), user.getUserId());
        assertEquals(user1.getUsername(), user.getUsername());
        assertEquals(user1.getEmail(), user.getEmail());
        assertEquals(user1.getPassword(), user.getPassword());
        assertEquals(user1.getBalance(), user.getBalance());
        assertEquals(user1.getOrders(), user.getOrders());
        assertEquals(user1.getGames(), user.getGames());
    }

    @Test
    public void whenUsersExist_ReturnAllUsers() {
        //arrange
        User user1 = new User(
                new UserId(VALID_USER_ID),
                VALID_USER_USERNAME,
                VALID_USER_EMAIL,
                VALID_USER_PASSWORD,
                VALID_BALANCE,
                orderList,
                gameList);
        userRepository.save(user1);
        User user2 = new User(
                new UserId("e3abc06e-e677-4121-841c-c059e91b8ed2"),
                "Jane Doe",
                "janedoe@example.com",
                "test123",
                VALID_BALANCE,
                orderList,
                gameList);
        userRepository.save(user2);
        Long sizeDB = userRepository.count();
        //act
        List<User> users = userRepository.findAll();
        //assert
        assertEquals(sizeDB, users.size());
    }
}
