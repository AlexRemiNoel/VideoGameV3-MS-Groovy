package com.champsoft.usermanagement.Presentation;

import com.champsoft.usermanagement.DataAccess.Admin;
import com.champsoft.usermanagement.DataAccess.AdminId;
import com.champsoft.usermanagement.DataAccess.AdminRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT, properties = {"spring.datasource.url=jdbc:h2:mem:admin-db"})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@ActiveProfiles("h2")
public class AdminControllerIntegrationTest {
    @Autowired
    private WebTestClient WebTestClient;
    @Autowired
    private AdminRepository adminRepository;

    private final String BASE_URI_ADMINS = "api/v1/admin";

    private final String VALID_ADMIN_ID = "c3540a89-cb47-4c96-888e-ff96708db4d8";
    private final String INVALID_ADMIN_ID = "nonExistentId";
    private final String VALID_ADMIN_USERNAME = "John Doe";
    private final String VALID_ADMIN_PASSWORD = "test123";

    @BeforeEach
    void setup() {
        Admin admin = new Admin();
        admin.setAdminId(new AdminId(VALID_ADMIN_ID));
        admin.setUsername(VALID_ADMIN_USERNAME);
        admin.setPassword(VALID_ADMIN_PASSWORD);
        adminRepository.save(admin);
    }

    private AdminRequestModel buildAdminRequestModel(String adminname) {
        AdminRequestModel adminRequestModel = new AdminRequestModel();
        adminRequestModel.setUsername(adminname); // Set adminname
        adminRequestModel.setPassword(VALID_ADMIN_PASSWORD);
        // Set any other required fields here
        return adminRequestModel;
    }

    private AdminRequestModel buildInvalidAdminRequestModel(String nonExistentAdminId) {

        AdminRequestModel someAdmin = new AdminRequestModel(
                VALID_ADMIN_USERNAME,
                VALID_ADMIN_PASSWORD
        );
        return someAdmin;
    }

    @Test
    public void whenDeleteAdmin_thenDeleteAdminSuccessfully() {
        // Act
        WebTestClient.delete().uri(BASE_URI_ADMINS + "/" + VALID_ADMIN_ID)
                .exchange()
                .expectStatus()
                .isNoContent();
        //Assert

        assertFalse(adminRepository.existsByAdminId(new AdminId((VALID_ADMIN_ID))));
    }

    @Test
    public void whenRemoveNonExistentAdmin_thenThrowNotFoundException() {
        // Arrange
        String nonExistentAdminId = "nonExistentId";
        // Act & Assert
        WebTestClient.delete().uri(BASE_URI_ADMINS + "/" + nonExistentAdminId)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.httpStatus").isEqualTo("NOT_FOUND")
                .jsonPath("$.message").isEqualTo("Unknown adminId: " + nonExistentAdminId);
    }

    @Test
    public void whenGetAdminById_thenReturnAdmin() {
        // Act & Assert
        WebTestClient.get().uri(BASE_URI_ADMINS + "/" + VALID_ADMIN_ID)
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isOk()
            .expectBody(AdminResponseModel.class)
            .value((Admin) -> {
                assertNotNull(Admin);
                assertEquals(VALID_ADMIN_ID, Admin.getAdminId());
                assertEquals(VALID_ADMIN_USERNAME, Admin.getUsername());
        });
    }

    @Test
    public void whenValidAdmin_thenCreateAdmin(){
        //arrange
        long sizeDB = adminRepository.count();
        AdminRequestModel AdminToCreate = buildAdminRequestModel(VALID_ADMIN_USERNAME);
        WebTestClient.post()
                .uri(BASE_URI_ADMINS)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(AdminToCreate)
                .exchange()
                .expectStatus().isCreated()
                //.expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(AdminResponseModel.class)
                .value((adminResponseModel) -> {
                    assertNotNull(AdminToCreate);
                    assertEquals(AdminToCreate.getUsername(),adminResponseModel.getUsername());
                });
        long sizeDBAfter = adminRepository.count();
        assertEquals(sizeDB + 1, sizeDBAfter);
    }

    @Test
    public void whenUpdateNonExistentAdmin_thenThrowNotFoundException() {
        // Arrange
        AdminRequestModel updatedAdmin = buildInvalidAdminRequestModel(INVALID_ADMIN_ID);
        // Act & Assert
        WebTestClient.put()
                .uri(BASE_URI_ADMINS + "/" + INVALID_ADMIN_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(updatedAdmin)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.httpStatus").isEqualTo("NOT_FOUND")
                .jsonPath("$.message").isEqualTo("Unknown adminId: " + INVALID_ADMIN_ID);
    }

    @Test
    public void whenUpdateAdmin_thenReturnUpdatedAdmin() {
        // Arrange
        AdminRequestModel AdminToUpdate = buildAdminRequestModel(VALID_ADMIN_USERNAME);
        // Act & Assert
        WebTestClient.put()
                .uri(BASE_URI_ADMINS + "/" + VALID_ADMIN_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(AdminToUpdate)
                .exchange()
                .expectStatus().isOk()
                .expectBody(AdminResponseModel.class)
                .value((updatedAdmin) -> {
                    assertNotNull(updatedAdmin);
                    assertEquals(AdminToUpdate.getUsername(), updatedAdmin.getUsername());
                });
    }
}
