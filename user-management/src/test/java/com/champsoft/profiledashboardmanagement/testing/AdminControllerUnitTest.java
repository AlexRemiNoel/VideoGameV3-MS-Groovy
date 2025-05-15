package com.champsoft.profiledashboardmanagement.testing;

import com.champsoft.profiledashboardmanagement.BusinessLogic.AdminService;
import com.champsoft.profiledashboardmanagement.Presentation.AdminController;
import com.champsoft.profiledashboardmanagement.Presentation.AdminRequestModel;
import com.champsoft.profiledashboardmanagement.Presentation.AdminResponseModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

// Load only the controller context and mock the service
@SpringBootTest(classes = AdminController.class)
class AdminControllerUnitTest {

    @Autowired
    private AdminController adminController;

    @MockitoBean // Creates a Mockito mock managed by Spring
    private AdminService adminService;

    private AdminResponseModel adminResponseModel1;
    private AdminResponseModel adminResponseModel2;
    private AdminRequestModel adminRequestModel;
    private String adminId1;
    private String nonExistentAdminId;

    @BeforeEach
    void setUp() {
        adminId1 = UUID.randomUUID().toString();
        String adminId2 = UUID.randomUUID().toString();
        nonExistentAdminId = UUID.randomUUID().toString();

        adminResponseModel1 = new AdminResponseModel(adminId1, "adminUser1");
        adminResponseModel2 = new AdminResponseModel(adminId2, "adminUser2");
        adminRequestModel = new AdminRequestModel("newAdmin", "newPass");
    }

    @Test
    void getAdmins_whenAdminsExist_thenReturnAdmins() {
        // Arrange
        List<AdminResponseModel> admins = Arrays.asList(adminResponseModel1, adminResponseModel2);
        when(adminService.getAllAdmins()).thenReturn(admins);

        // Act
        ResponseEntity<List<AdminResponseModel>> response = adminController.getAdmins();

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(admins, response.getBody());
        verify(adminService, times(1)).getAllAdmins();
    }

    @Test
    void getAdmins_whenNoAdminsExist_thenReturnEmptyList() {
        // Arrange
        when(adminService.getAllAdmins()).thenReturn(Collections.emptyList());

        // Act
        ResponseEntity<List<AdminResponseModel>> response = adminController.getAdmins();

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isEmpty());
        verify(adminService, times(1)).getAllAdmins();
    }

    @Test
    void getAdminById_whenAdminExists_thenReturnAdmin() {
        // Arrange
        when(adminService.getAdminById(adminId1)).thenReturn(adminResponseModel1);

        // Act
        ResponseEntity<AdminResponseModel> response = adminController.getAdminById(adminId1);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(adminResponseModel1, response.getBody());
        verify(adminService, times(1)).getAdminById(adminId1);
    }

    @Test
    void getAdminById_whenAdminDoesNotExist_thenReturnNotFound() {
        // Arrange
        // Controller checks for null after service call in this specific implementation
        when(adminService.getAdminById(nonExistentAdminId)).thenReturn(null);

        // Act
        ResponseEntity<AdminResponseModel> response = adminController.getAdminById(nonExistentAdminId);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
        verify(adminService, times(1)).getAdminById(nonExistentAdminId);
    }

    @Test
    void addAdmin_whenValidRequest_thenReturnCreatedAdmin() {
        // Arrange
        AdminResponseModel addedAdmin = new AdminResponseModel(UUID.randomUUID().toString(), "newAdmin");
        when(adminService.addAdmin(adminRequestModel)).thenReturn(addedAdmin);

        // Act
        ResponseEntity<AdminResponseModel> response = adminController.addAdmin(adminRequestModel);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(addedAdmin, response.getBody());
        verify(adminService, times(1)).addAdmin(adminRequestModel);
    }

    @Test
    void updateAdmin_whenAdminExists_thenReturnUpdatedAdmin() {
        // Arrange
        AdminResponseModel updatedAdmin = new AdminResponseModel(adminId1, "updatedAdmin");
        when(adminService.updateAdmin(adminRequestModel, adminId1)).thenReturn(updatedAdmin);

        // Act
        ResponseEntity<AdminResponseModel> response = adminController.updateAdmin(adminRequestModel, adminId1);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(updatedAdmin, response.getBody());
        verify(adminService, times(1)).updateAdmin(adminRequestModel, adminId1);
    }

    @Test
    void updateAdmin_whenAdminDoesNotExist_thenReturnNotFound() {
        // Arrange
        // Controller checks for null after service call in this specific implementation
        when(adminService.updateAdmin(adminRequestModel, nonExistentAdminId)).thenReturn(null);

        // Act
        ResponseEntity<AdminResponseModel> response = adminController.updateAdmin(adminRequestModel, nonExistentAdminId);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
        verify(adminService, times(1)).updateAdmin(adminRequestModel, nonExistentAdminId);
    }

    @Test
    void deleteAdmin_whenAdminExists_thenReturnNoContent() {
        // Arrange
        doNothing().when(adminService).deleteAdmin(adminId1); // Mock void method

        // Act
        ResponseEntity<Void> response = adminController.deleteAdmin(adminId1);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertNull(response.getBody());
        verify(adminService, times(1)).deleteAdmin(adminId1);
    }

    // Note: Testing the NotFoundException scenario for delete requires either
    // @WebMvcTest or an integration test to verify the exception handler correctly
    // translates the exception to a 404 response.
    // This unit test primarily verifies the service method is called.
    @Test
    void deleteAdmin_whenServiceCalled_thenVerifyInteraction() {
        // Arrange
        doNothing().when(adminService).deleteAdmin(adminId1);

        // Act
        adminController.deleteAdmin(adminId1);

        // Assert
        verify(adminService, times(1)).deleteAdmin(adminId1);
    }
}