package com.example.apigatewayservice.presentationlayer.admin;

import com.example.apigatewayservice.businesslogiclayer.admin.AdminService;
import com.example.apigatewayservice.exception.HttpErrorInfo;
import com.example.apigatewayservice.exception.InvalidInputException;
import com.example.apigatewayservice.exception.NotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
public class AdminControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private AdminService adminService;

    private final String BASE_URI_ADMINS = "/api/v1/admin";
    private final String VALID_ADMIN_UUID = "admin-uuid-123";
    private final String NOT_FOUND_ADMIN_UUID = "admin-uuid-999";

    // Helper to create AdminResponseModel. Add @Builder to your DTO.
    private AdminResponseModel buildAdminResponseModel(String adminId, String username) {
        return AdminResponseModel.builder()
                .adminId(adminId)
                .username(username)
                .build();
    }

    // Helper to create AdminRequestModel. Add @Builder to your DTO.
    private AdminRequestModel buildAdminRequestModel(String username, String password) {
        return AdminRequestModel.builder()
                .username(username)
                .password(password)
                .build();
    }

    @Test
    void getAdmins_whenAdminsExist_thenReturnAdmins() {
        // Arrange
        AdminResponseModel admin1 = buildAdminResponseModel("uuid1", "adminUser1");
        AdminResponseModel admin2 = buildAdminResponseModel("uuid2", "adminUser2");
        List<AdminResponseModel> expectedAdmins = List.of(admin1, admin2);
        when(adminService.getAllAdmins()).thenReturn(expectedAdmins);

        // Act & Assert
        webTestClient.get().uri(BASE_URI_ADMINS)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBodyList(AdminResponseModel.class)
                .hasSize(2)
                .value(admins -> {
                    assertEquals(expectedAdmins.get(0).getAdminId(), admins.get(0).getAdminId());
                    assertEquals(expectedAdmins.get(1).getUsername(), admins.get(1).getUsername());
                });
        verify(adminService, times(1)).getAllAdmins();
    }

    @Test
    void getAdmins_whenNoAdminsExist_thenReturnEmptyList() {
        // Arrange
        when(adminService.getAllAdmins()).thenReturn(Collections.emptyList());

        // Act & Assert
        webTestClient.get().uri(BASE_URI_ADMINS)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBodyList(AdminResponseModel.class)
                .hasSize(0);
        verify(adminService, times(1)).getAllAdmins();
    }

    @Test
    void getAdminById_whenAdminExists_thenReturnAdmin() {
        // Arrange
        AdminResponseModel expectedAdmin = buildAdminResponseModel(VALID_ADMIN_UUID, "targetAdmin");
        when(adminService.getAdminById(VALID_ADMIN_UUID)).thenReturn(expectedAdmin);

        // Act & Assert
        webTestClient.get().uri(BASE_URI_ADMINS + "/" + VALID_ADMIN_UUID)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(AdminResponseModel.class)
                .value(admin -> {
                    assertNotNull(admin);
                    assertEquals(expectedAdmin.getAdminId(), admin.getAdminId());
                    assertEquals(expectedAdmin.getUsername(), admin.getUsername());
                });
        verify(adminService, times(1)).getAdminById(VALID_ADMIN_UUID);
    }

    @Test
    void getAdminById_whenAdminNotFound_thenReturnNotFound() {
        // Arrange
        String errorMessage = "Admin not found with uuid: " + NOT_FOUND_ADMIN_UUID;
        when(adminService.getAdminById(NOT_FOUND_ADMIN_UUID)).thenThrow(new NotFoundException(errorMessage));

        // Act & Assert
        webTestClient.get().uri(BASE_URI_ADMINS + "/" + NOT_FOUND_ADMIN_UUID)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(HttpErrorInfo.class)
                .value(errorInfo -> {
                    assertNotNull(errorInfo);
                    assertEquals(HttpStatus.NOT_FOUND, errorInfo.getHttpStatus());
                    assertTrue(errorInfo.getMessage().contains(errorMessage));
                    assertEquals(BASE_URI_ADMINS + "/" + NOT_FOUND_ADMIN_UUID, errorInfo.getPath());
                });
        verify(adminService, times(1)).getAdminById(NOT_FOUND_ADMIN_UUID);
    }

    @Test
    void addAdmin_whenValidRequest_thenReturnCreatedAdmin() {
        // Arrange
        AdminRequestModel requestModel = buildAdminRequestModel("newAdmin", "securePassword");
        AdminResponseModel expectedResponse = buildAdminResponseModel(VALID_ADMIN_UUID, "newAdmin");
        when(adminService.addAdmin(any(AdminRequestModel.class))).thenReturn(expectedResponse);

        // Act & Assert
        webTestClient.post().uri(BASE_URI_ADMINS)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestModel)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(AdminResponseModel.class)
                .value(response -> {
                    assertNotNull(response);
                    assertEquals(expectedResponse.getAdminId(), response.getAdminId());
                    assertEquals(requestModel.getUsername(), response.getUsername());
                });
        verify(adminService, times(1)).addAdmin(any(AdminRequestModel.class));
    }

    @Test
    void addAdmin_whenInvalidInput_thenReturnUnprocessableEntity() {
        // Arrange
        AdminRequestModel requestModel = buildAdminRequestModel("", ""); // Invalid data
        String errorMessage = "Invalid admin data provided";
        when(adminService.addAdmin(any(AdminRequestModel.class))).thenThrow(new InvalidInputException(errorMessage));

        // Act & Assert
        webTestClient.post().uri(BASE_URI_ADMINS)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestModel)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY)
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(HttpErrorInfo.class)
                .value(errorInfo -> {
                    assertNotNull(errorInfo);
                    assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, errorInfo.getHttpStatus());
                    assertTrue(errorInfo.getMessage().contains(errorMessage));
                });
        verify(adminService, times(1)).addAdmin(any(AdminRequestModel.class));
    }

    @Test
    void updateAdmin_whenAdminExistsAndValidRequest_thenReturnNoContent() {
        // Arrange
        AdminRequestModel requestModel = buildAdminRequestModel("updatedAdmin", "newSecurePass");
        doNothing().when(adminService).updateAdmin(any(AdminRequestModel.class), eq(VALID_ADMIN_UUID));

        // Act & Assert
        webTestClient.put().uri(BASE_URI_ADMINS + "/" + VALID_ADMIN_UUID)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestModel)
                .exchange()
                .expectStatus().isNoContent();
        verify(adminService, times(1)).updateAdmin(any(AdminRequestModel.class), eq(VALID_ADMIN_UUID));
    }

    @Test
    void updateAdmin_whenAdminNotFound_thenReturnNotFound() {
        // Arrange
        AdminRequestModel requestModel = buildAdminRequestModel("anyAdmin", "anyPass");
        String errorMessage = "Cannot update. Admin not found with uuid: " + NOT_FOUND_ADMIN_UUID;
        doThrow(new NotFoundException(errorMessage)).when(adminService).updateAdmin(any(AdminRequestModel.class), eq(NOT_FOUND_ADMIN_UUID));

        // Act & Assert
        webTestClient.put().uri(BASE_URI_ADMINS + "/" + NOT_FOUND_ADMIN_UUID)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestModel)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody(HttpErrorInfo.class)
                .value(errorInfo -> {
                    assertNotNull(errorInfo);
                    assertEquals(HttpStatus.NOT_FOUND, errorInfo.getHttpStatus());
                    assertTrue(errorInfo.getMessage().contains(errorMessage));
                });
        verify(adminService, times(1)).updateAdmin(any(AdminRequestModel.class), eq(NOT_FOUND_ADMIN_UUID));
    }

    @Test
    void deleteAdmin_whenAdminExists_thenReturnNoContent() {
        // Arrange
        doNothing().when(adminService).deleteAdmin(VALID_ADMIN_UUID);

        // Act & Assert
        webTestClient.delete().uri(BASE_URI_ADMINS + "/" + VALID_ADMIN_UUID)
                .exchange()
                .expectStatus().isNoContent();
        verify(adminService, times(1)).deleteAdmin(VALID_ADMIN_UUID);
    }

    @Test
    void deleteAdmin_whenAdminNotFound_thenReturnNotFound() {
        // Arrange
        String errorMessage = "Cannot delete. Admin not found with uuid: " + NOT_FOUND_ADMIN_UUID;
        doThrow(new NotFoundException(errorMessage)).when(adminService).deleteAdmin(NOT_FOUND_ADMIN_UUID);

        // Act & Assert
        webTestClient.delete().uri(BASE_URI_ADMINS + "/" + NOT_FOUND_ADMIN_UUID)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody(HttpErrorInfo.class)
                .value(errorInfo -> {
                    assertNotNull(errorInfo);
                    assertEquals(HttpStatus.NOT_FOUND, errorInfo.getHttpStatus());
                    assertTrue(errorInfo.getMessage().contains(errorMessage));
                });
        verify(adminService, times(1)).deleteAdmin(NOT_FOUND_ADMIN_UUID);
    }
} 