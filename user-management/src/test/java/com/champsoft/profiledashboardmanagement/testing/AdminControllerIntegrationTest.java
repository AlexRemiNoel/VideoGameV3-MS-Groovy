package com.champsoft.profiledashboardmanagement.testing;


import com.champsoft.profiledashboardmanagement.DataAccess.Admin;
import com.champsoft.profiledashboardmanagement.DataAccess.AdminId;
import com.champsoft.profiledashboardmanagement.DataAccess.AdminRepository;
import com.champsoft.profiledashboardmanagement.Presentation.AdminRequestModel;
import com.champsoft.profiledashboardmanagement.Presentation.AdminResponseModel;
import com.champsoft.profiledashboardmanagement.utils.HttpErrorInfo;
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
@ActiveProfiles("h2")
public class AdminControllerIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private AdminRepository adminRepository;

    private Admin admin1;
    private final String BASE_URI_ADMINS = "/api/v1/admin";

    @BeforeEach
    void setUp() {
        adminRepository.deleteAll();

        AdminId adminId1 = new AdminId(UUID.randomUUID().toString());
        admin1 = new Admin(adminId1, "IntegrationAdmin1", "pass1");
        adminRepository.save(admin1);
    }

    @AfterEach
    void tearDown() {
        adminRepository.deleteAll();
    }

    @Test
    void getAdmins_shouldReturnAllAdmins() {
        webTestClient.get().uri(BASE_URI_ADMINS)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(AdminResponseModel.class)
                .hasSize(1)
                .value(admins -> assertEquals(admin1.getAdminId().getUuid(), admins.get(0).getAdminId()));
    }

    @Test
    void getAdminById_whenAdminExists_shouldReturnAdmin() {
        String existingAdminId = admin1.getAdminId().getUuid();
        webTestClient.get().uri(BASE_URI_ADMINS + "/" + existingAdminId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(AdminResponseModel.class)
                .value(adminResponse -> {
                    assertEquals(existingAdminId, adminResponse.getAdminId());
                    assertEquals(admin1.getUsername(), adminResponse.getUsername());
                });
    }

    @Test
    void getAdminById_whenAdminDoesNotExist_shouldReturnNotFound() {
        String nonExistentAdminId = UUID.randomUUID().toString();
        webTestClient.get().uri(BASE_URI_ADMINS + "/" + nonExistentAdminId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody(HttpErrorInfo.class)
                .value(errorInfo -> assertTrue(errorInfo.getMessage().contains("Unknown adminId: " + nonExistentAdminId)));
    }

    @Test
    void addAdmin_whenValidRequest_shouldCreateAdmin() {
        AdminRequestModel newAdminRequest = new AdminRequestModel("NewIntAdmin", "newPass");
        long countBefore = adminRepository.count();

        webTestClient.post().uri(BASE_URI_ADMINS)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(newAdminRequest)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(AdminResponseModel.class)
                .value(createdAdmin -> {
                    assertNotNull(createdAdmin.getAdminId());
                    assertEquals(newAdminRequest.getUsername(), createdAdmin.getUsername());
                });
        assertEquals(countBefore + 1, adminRepository.count());
    }

    @Test
    void updateAdmin_whenAdminExistsAndValidRequest_shouldUpdateAdmin() {
        String existingAdminId = admin1.getAdminId().getUuid();
        AdminRequestModel updateAdminRequest = new AdminRequestModel("UpdatedAdminName", "updatedPass");

        webTestClient.put().uri(BASE_URI_ADMINS + "/" + existingAdminId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(updateAdminRequest)
                .exchange()
                .expectStatus().isOk()
                .expectBody(AdminResponseModel.class)
                .value(updatedAdmin -> {
                    assertEquals(existingAdminId, updatedAdmin.getAdminId());
                    assertEquals(updateAdminRequest.getUsername(), updatedAdmin.getUsername());
                });
        Admin dbAdmin = adminRepository.findAdminByAdminId_uuid(existingAdminId);
        assertNotNull(dbAdmin);
        assertEquals("UpdatedAdminName", dbAdmin.getUsername());
    }

    @Test
    void updateAdmin_whenAdminDoesNotExist_shouldReturnNotFound() {
        String nonExistentAdminId = UUID.randomUUID().toString();
        AdminRequestModel updateAdminRequest = new AdminRequestModel("AnyName", "anyPass");

        webTestClient.put().uri(BASE_URI_ADMINS + "/" + nonExistentAdminId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(updateAdminRequest)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody(HttpErrorInfo.class)
                .value(errorInfo -> assertTrue(errorInfo.getMessage().contains("Unknown adminId: " + nonExistentAdminId)));
    }


    @Test
    void deleteAdmin_whenAdminExists_shouldDeleteAdmin() {
        String existingAdminId = admin1.getAdminId().getUuid();
        assertTrue(adminRepository.existsByAdminId(admin1.getAdminId()));

        webTestClient.delete().uri(BASE_URI_ADMINS + "/" + existingAdminId)
                .exchange()
                .expectStatus().isNoContent();

        assertFalse(adminRepository.existsByAdminId(admin1.getAdminId()));
    }

    @Test
    void deleteAdmin_whenAdminDoesNotExist_shouldReturnNotFound() {
        String nonExistentAdminId = UUID.randomUUID().toString();
        webTestClient.delete().uri(BASE_URI_ADMINS + "/" + nonExistentAdminId)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody(HttpErrorInfo.class)
                .value(errorInfo -> assertTrue(errorInfo.getMessage().contains("Unknown adminId: " + nonExistentAdminId)));
    }
}