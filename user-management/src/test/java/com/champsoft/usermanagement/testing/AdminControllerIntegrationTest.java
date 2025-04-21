package com.champsoft.usermanagement.testing;

import com.champsoft.usermanagement.DataAccess.Admin;
import com.champsoft.usermanagement.DataAccess.AdminId;
import com.champsoft.usermanagement.DataAccess.AdminRepository;
import com.champsoft.usermanagement.Presentation.AdminRequestModel;
import com.champsoft.usermanagement.Presentation.AdminResponseModel;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("h2") // Activate the H2 profile from application-h2.yml
class AdminControllerIntegrationTest {

    @Autowired
    private WebTestClient webTestClient; // Use reactive client for testing

    @Autowired
    private AdminRepository adminRepository;

    private final String BASE_URI_ADMIN = "/api/v1/admin";
    private Admin admin1, admin2;
    private String adminId1, adminId2;

    @BeforeEach
    void setUp() {
        adminRepository.deleteAll(); // Clean slate before each test

        adminId1 = UUID.randomUUID().toString();
        adminId2 = UUID.randomUUID().toString();

        admin1 = new Admin(new AdminId(adminId1), "adminInteg1", "passInteg1");
        admin2 = new Admin(new AdminId(adminId2), "adminInteg2", "passInteg2");

    }

    @AfterEach
    void tearDown() {
        adminRepository.deleteAll();
    }

    private Admin saveAdmin(Admin admin) {
        return adminRepository.save(admin);
    }

    @Test
    void getAdmins_whenAdminsExist_thenReturnAdmins() {
        // Arrange
        saveAdmin(admin1);
        saveAdmin(admin2);

        // Act & Assert
        webTestClient.get().uri(BASE_URI_ADMIN)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.length()").isEqualTo(2)
                .jsonPath("$[0].adminId").isEqualTo(admin1.getAdminId().getUuid())
                .jsonPath("$[0].username").isEqualTo(admin1.getUsername())
                .jsonPath("$[1].adminId").isEqualTo(admin2.getAdminId().getUuid())
                .jsonPath("$[1].username").isEqualTo(admin2.getUsername());
    }

    @Test
    void getAdmins_whenNoAdminsExist_thenReturnEmptyArray() {
        // Act & Assert
        webTestClient.get().uri(BASE_URI_ADMIN)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.length()").isEqualTo(0);
    }

    @Test
    void getAdminById_whenAdminExists_thenReturnAdmin() {
        // Arrange
        saveAdmin(admin1);

        // Act & Assert
        webTestClient.get().uri(BASE_URI_ADMIN + "/" + adminId1)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(AdminResponseModel.class) // Can map directly to Response Model
                .value((response) -> {
                    assertNotNull(response);
                    assertEquals(adminId1, response.getAdminId());
                    assertEquals(admin1.getUsername(), response.getUsername());
                });
    }

    @Test
    void getAdminById_whenAdminDoesNotExist_thenReturnNotFound() {
        // Arrange
        String nonExistentId = UUID.randomUUID().toString();

        // Act & Assert
        webTestClient.get().uri(BASE_URI_ADMIN + "/" + nonExistentId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.httpStatus").isEqualTo("NOT_FOUND") // Assuming Exception Handler setup
                .jsonPath("$.message").isEqualTo("Unknown adminId: " + nonExistentId);
    }

    @Test
    void addAdmin_whenValidRequest_thenCreateAdmin() {
        // Arrange
        AdminRequestModel requestModel = new AdminRequestModel("newAdmin", "newPass");
        long countBefore = adminRepository.count();

        // Act & Assert
        webTestClient.post().uri(BASE_URI_ADMIN)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestModel)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(AdminResponseModel.class)
                .value((response) -> {
                    assertNotNull(response);
                    assertNotNull(response.getAdminId()); // ID should be generated
                    assertEquals(requestModel.getUsername(), response.getUsername());
                });

        // Verify DB state
        long countAfter = adminRepository.count();
        assertEquals(countBefore + 1, countAfter);
        // Optional: Find the newly created admin by username/criteria if ID is unknown
    }


    @Test
    void updateAdmin_whenAdminExists_thenUpdateAdmin() {
        // Arrange
        saveAdmin(admin1);
        AdminRequestModel updateRequest = new AdminRequestModel("updatedAdminName", "updatedPass");

        // Act & Assert
        webTestClient.put().uri(BASE_URI_ADMIN + "/" + adminId1)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(updateRequest)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(AdminResponseModel.class)
                .value((response) -> {
                    assertNotNull(response);
                    assertEquals(adminId1, response.getAdminId());
                    assertEquals(updateRequest.getUsername(), response.getUsername());
                });

        // Verify DB state
        Admin updatedAdmin = adminRepository.findAdminByAdminId_uuid(adminId1);
        assertNotNull(updatedAdmin);
        assertEquals(updateRequest.getUsername(), updatedAdmin.getUsername());
        assertEquals(updateRequest.getPassword(), updatedAdmin.getPassword());
    }

    @Test
    void updateAdmin_whenAdminDoesNotExist_thenReturnNotFound() {
        // Arrange
        String nonExistentId = UUID.randomUUID().toString();
        AdminRequestModel updateRequest = new AdminRequestModel("updatedAdminName", "updatedPass");

        // Act & Assert
        webTestClient.put().uri(BASE_URI_ADMIN + "/" + nonExistentId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(updateRequest)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.httpStatus").isEqualTo("NOT_FOUND")
                .jsonPath("$.message").isEqualTo("Unknown adminId: " + nonExistentId);
    }

    @Test
    void deleteAdmin_whenAdminExists_thenDeleteAdmin() {
        // Arrange
        saveAdmin(admin1);
        assertTrue(adminRepository.existsByAdminId(admin1.getAdminId()));

        // Act & Assert
        webTestClient.delete().uri(BASE_URI_ADMIN + "/" + adminId1)
                .exchange()
                .expectStatus().isNoContent();

        // Verify DB state
        assertFalse(adminRepository.existsByAdminId(admin1.getAdminId()));
    }

    @Test
    void deleteAdmin_whenAdminDoesNotExist_thenReturnNotFound() {
        // Arrange
        String nonExistentId = UUID.randomUUID().toString();
        assertFalse(adminRepository.existsByAdminId(new AdminId(nonExistentId)));

        // Act & Assert
        webTestClient.delete().uri(BASE_URI_ADMIN + "/" + nonExistentId)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.httpStatus").isEqualTo("NOT_FOUND")
                .jsonPath("$.message").isEqualTo("Unknown adminId: " + nonExistentId);
    }
}