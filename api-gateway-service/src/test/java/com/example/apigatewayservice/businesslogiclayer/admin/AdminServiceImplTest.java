package com.example.apigatewayservice.businesslogiclayer.admin;

import com.example.apigatewayservice.DomainClientLayer.admin.AdminServiceClient;
import com.example.apigatewayservice.presentationlayer.admin.AdminRequestModel;
import com.example.apigatewayservice.presentationlayer.admin.AdminResponseModel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminServiceImplTest {

    @Mock
    private AdminServiceClient adminServiceClient;

    @InjectMocks
    private AdminServiceImpl adminService;

    private final String VALID_ADMIN_UUID = "admin-uuid-123";

    // Helper to build AdminResponseModel. Add @Builder to DTO.
    private AdminResponseModel buildAdminResponseModel(String adminId, String username) {
        return AdminResponseModel.builder().adminId(adminId).username(username).build();
    }

    // Helper to build AdminRequestModel. Add @Builder to DTO.
    private AdminRequestModel buildAdminRequestModel(String username, String password) {
        return AdminRequestModel.builder().username(username).password(password).build();
    }

    @Test
    void getAllAdmins_callsClient() {
        List<AdminResponseModel> expectedResponse = List.of(buildAdminResponseModel(VALID_ADMIN_UUID, "admin1"));
        when(adminServiceClient.getAllAdmins()).thenReturn(expectedResponse);

        List<AdminResponseModel> actualResponse = adminService.getAllAdmins();

        assertEquals(expectedResponse, actualResponse);
        verify(adminServiceClient, times(1)).getAllAdmins();
    }

    @Test
    void getAdminById_callsClient() {
        AdminResponseModel expectedResponse = buildAdminResponseModel(VALID_ADMIN_UUID, "admin1");
        when(adminServiceClient.getAdminById(VALID_ADMIN_UUID)).thenReturn(expectedResponse);

        AdminResponseModel actualResponse = adminService.getAdminById(VALID_ADMIN_UUID);

        assertEquals(expectedResponse, actualResponse);
        verify(adminServiceClient, times(1)).getAdminById(VALID_ADMIN_UUID);
    }

    @Test
    void addAdmin_callsClient() {
        AdminRequestModel requestModel = buildAdminRequestModel("admin1", "pass");
        AdminResponseModel expectedResponse = buildAdminResponseModel(VALID_ADMIN_UUID, "admin1");
        when(adminServiceClient.addAdmin(requestModel)).thenReturn(expectedResponse);

        AdminResponseModel actualResponse = adminService.addAdmin(requestModel);

        assertEquals(expectedResponse, actualResponse);
        verify(adminServiceClient, times(1)).addAdmin(requestModel);
    }

    @Test
    void updateAdmin_callsClient() {
        AdminRequestModel requestModel = buildAdminRequestModel("admin1", "pass");
        when(adminServiceClient.updateAdmin(any(AdminRequestModel.class), eq(VALID_ADMIN_UUID))).thenReturn(null);

        adminService.updateAdmin(requestModel, VALID_ADMIN_UUID);

        verify(adminServiceClient).updateAdmin(any(AdminRequestModel.class), eq(VALID_ADMIN_UUID));
    }

    @Test
    void deleteAdmin_callsClient() {
        doNothing().when(adminServiceClient).deleteAdmin(VALID_ADMIN_UUID);

        adminService.deleteAdmin(VALID_ADMIN_UUID);

        verify(adminServiceClient, times(1)).deleteAdmin(VALID_ADMIN_UUID);
    }
} 