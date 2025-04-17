package com.champsoft.usermanagement.Presentation;

import com.champsoft.usermanagement.DataAccess.User;
import com.champsoft.usermanagement.DataAccess.UserId;
import com.champsoft.usermanagement.DataAccess.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT, properties = {"spring.datasource.url=jdbc:h2:mem:user-db"})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@ActiveProfiles("h2")
public class UserControllerIntegrationTest {
    @Autowired
    private WebTestClient WebTestClient;
    @Autowired
    private UserRepository userRepository;

    private final String BASE_URI_USERS = "api/v1/user";
    private final String INVALID_USER_ID = "c3540a89-cb47-4c96-888e-ff96708db4d";
    private final String NOT_FOUND_USER_ID = "c3540a89-cb47-4c96-888e-ff96708db4d0";
    private final String VALID_USER_ID = "c3540a89-cb47-4c96-888e-ff96708db4d8";
    private final String VALID_USER_USERNAME = "John Doe";
    private final Double VALID_BALANCE = 50.0;
    private final String VALID_USER_EMAIL = "john.doe@example.com";

    @BeforeEach
    void setup() {
        User user = new User();
        user.setUserId(new UserId(VALID_USER_ID));
        user.setUsername("Alick");
        user.setEmail("aucceli0@dot.gov");
        user.setPassword("test123");
        user.setBalance(50);
        userRepository.save(user);
    }

    private UserResponseModel buildUserResponseModel(String username) {
        UserResponseModel userResponseModel = new UserResponseModel();
        userResponseModel.setUserId("valid-user-id"); // Set the user ID
        userResponseModel.setUsername(username); // Set username
        userResponseModel.setBalance(50);
        userResponseModel.setEmail("john.doe@example.com"); // Set user email
        // Set any other required fields here
        return userResponseModel;
    }
    @Test
    public void whenDeleteUser_thenDeleteUserSuccessfully() {
        // Act
        WebTestClient.delete().uri(BASE_URI_USERS + "/" + VALID_USER_ID)
                .exchange()
                .expectStatus()
                .isNoContent();
        //Assert

        assertFalse(userRepository.existsByUserId(new UserId((VALID_USER_ID))));
    }

}
