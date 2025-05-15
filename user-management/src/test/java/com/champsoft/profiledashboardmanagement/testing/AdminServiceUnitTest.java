package com.champsoft.profiledashboardmanagement.testing;

import com.champsoft.profiledashboardmanagement.BusinessLogic.AdminService;
import com.champsoft.profiledashboardmanagement.DataAccess.Admin;
import com.champsoft.profiledashboardmanagement.DataAccess.AdminId;
import com.champsoft.profiledashboardmanagement.DataAccess.AdminRepository;
import com.champsoft.profiledashboardmanagement.DataMapper.AdminRequestMapper;
import com.champsoft.profiledashboardmanagement.DataMapper.AdminResponseMapper;
import com.champsoft.profiledashboardmanagement.Presentation.AdminRequestModel;
import com.champsoft.profiledashboardmanagement.Presentation.AdminResponseModel;
import com.champsoft.profiledashboardmanagement.utils.exceptions.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class) // Use MockitoExtension for pure Mockito tests
class AdminServiceUnitTest {

    @Mock
    private AdminRepository adminRepository;

    @Mock
    private AdminResponseMapper adminResponseMapper;

    @Mock
    private AdminRequestMapper adminRequestMapper;

    @InjectMocks // Inject mocks into this instance
    private AdminService adminService;

    private Admin admin1;
    private Admin admin2;
    private AdminRequestModel adminRequestModel;
    private AdminResponseModel adminResponseModel1;
    private AdminResponseModel adminResponseModel2;
    private String adminId1;
    private String adminId2;
    private String nonExistentAdminId;

    @BeforeEach
    void setUp() {
        adminId1 = UUID.randomUUID().toString();
        adminId2 = UUID.randomUUID().toString();
        nonExistentAdminId = UUID.randomUUID().toString();

        admin1 = new Admin(new AdminId(adminId1), "adminUser1", "pass1");
        admin2 = new Admin(new AdminId(adminId2), "adminUser2", "pass2");
        adminRequestModel = new AdminRequestModel("newAdmin", "newPass");
        adminResponseModel1 = new AdminResponseModel(adminId1, "adminUser1");
        adminResponseModel2 = new AdminResponseModel(adminId2, "adminUser2");
    }

    @Test
    void getAllAdmins_whenAdminsExist_thenReturnAdminList() {
        // Arrange
        List<Admin> admins = Arrays.asList(admin1, admin2);
        List<AdminResponseModel> responseModels = Arrays.asList(adminResponseModel1, adminResponseModel2);
        when(adminRepository.findAll()).thenReturn(admins);
        when(adminResponseMapper.adminToAdminResponseModel(admins)).thenReturn(responseModels);

        // Act
        List<AdminResponseModel> result = adminService.getAllAdmins();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(responseModels, result);
        verify(adminRepository, times(1)).findAll();
        verify(adminResponseMapper, times(1)).adminToAdminResponseModel(admins);
    }

    @Test
    void getAllAdmins_whenNoAdminsExist_thenReturnEmptyList() {
        // Arrange
        when(adminRepository.findAll()).thenReturn(Collections.emptyList());
        when(adminResponseMapper.adminToAdminResponseModel(Collections.emptyList())).thenReturn(Collections.emptyList());

        // Act
        List<AdminResponseModel> result = adminService.getAllAdmins();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(adminRepository, times(1)).findAll();
        verify(adminResponseMapper, times(1)).adminToAdminResponseModel(Collections.emptyList());
    }

    @Test
    void getAdminById_whenAdminExists_thenReturnAdmin() {
        // Arrange
        when(adminRepository.findAdminByAdminId_uuid(adminId1)).thenReturn(admin1);
        when(adminResponseMapper.adminToAdminResponseModel(admin1)).thenReturn(adminResponseModel1);

        // Act
        AdminResponseModel result = adminService.getAdminById(adminId1);

        // Assert
        assertNotNull(result);
        assertEquals(adminResponseModel1, result);
        verify(adminRepository, times(1)).findAdminByAdminId_uuid(adminId1);
        verify(adminResponseMapper, times(1)).adminToAdminResponseModel(admin1);
    }

    @Test
    void getAdminById_whenAdminDoesNotExist_thenThrowNotFoundException() {
        // Arrange
        when(adminRepository.findAdminByAdminId_uuid(nonExistentAdminId)).thenReturn(null);

        // Act & Assert
        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            adminService.getAdminById(nonExistentAdminId);
        });
        assertEquals("Unknown adminId: " + nonExistentAdminId, exception.getMessage());
        verify(adminRepository, times(1)).findAdminByAdminId_uuid(nonExistentAdminId);
        verify(adminResponseMapper, never()).adminToAdminResponseModel(any(Admin.class));
    }

    @Test
    void addAdmin_whenValidRequest_thenReturnAddedAdmin() {
        // Arrange
        Admin newAdmin = new Admin(new AdminId(UUID.randomUUID().toString()), "newAdmin", "newPass");
        AdminResponseModel newAdminResponse = new AdminResponseModel(newAdmin.getAdminId().getUuid(), newAdmin.getUsername());

        when(adminRequestMapper.adminRequestModelToAdmin(adminRequestModel)).thenReturn(newAdmin);
        when(adminRepository.save(newAdmin)).thenReturn(newAdmin); // Mock save behavior
        when(adminResponseMapper.adminToAdminResponseModel(newAdmin)).thenReturn(newAdminResponse);

        // Act
        AdminResponseModel result = adminService.addAdmin(adminRequestModel);

        // Assert
        assertNotNull(result);
        assertEquals(newAdminResponse, result);
        verify(adminRequestMapper, times(1)).adminRequestModelToAdmin(adminRequestModel);
        verify(adminRepository, times(1)).save(newAdmin);
        verify(adminResponseMapper, times(1)).adminToAdminResponseModel(newAdmin);
    }


    @Test
    void updateAdmin_whenAdminExists_thenReturnUpdatedAdmin() {
        // Arrange
        AdminRequestModel updateRequest = new AdminRequestModel("updatedUser", "updatedPass");
        Admin updatedAdmin = new Admin(new AdminId(adminId1), "updatedUser", "updatedPass");
        AdminResponseModel updatedResponse = new AdminResponseModel(adminId1, "updatedUser");

        when(adminRepository.findAdminByAdminId_uuid(adminId1)).thenReturn(admin1); // Return existing admin
        when(adminRepository.save(any(Admin.class))).thenAnswer(invocation -> invocation.getArgument(0)); // Return the saved admin
        when(adminResponseMapper.adminToAdminResponseModel(any(Admin.class))).thenReturn(updatedResponse);


        // Act
        AdminResponseModel result = adminService.updateAdmin(updateRequest, adminId1);

        // Assert
        assertNotNull(result);
        assertEquals(updatedResponse, result);
        assertEquals("updatedUser", admin1.getUsername()); // Verify the original object was modified before save
        assertEquals("updatedPass", admin1.getPassword());
        verify(adminRepository, times(1)).findAdminByAdminId_uuid(adminId1);
        verify(adminRepository, times(1)).save(admin1); // Verify save was called with the modified admin1
        verify(adminResponseMapper, times(1)).adminToAdminResponseModel(admin1);
    }

    @Test
    void updateAdmin_whenAdminDoesNotExist_thenThrowNotFoundException() {
        // Arrange
        AdminRequestModel updateRequest = new AdminRequestModel("updatedUser", "updatedPass");
        when(adminRepository.findAdminByAdminId_uuid(nonExistentAdminId)).thenReturn(null);

        // Act & Assert
        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            adminService.updateAdmin(updateRequest, nonExistentAdminId);
        });
        assertEquals("Unknown adminId: " + nonExistentAdminId, exception.getMessage());
        verify(adminRepository, times(1)).findAdminByAdminId_uuid(nonExistentAdminId);
        verify(adminRepository, never()).save(any(Admin.class));
        verify(adminResponseMapper, never()).adminToAdminResponseModel(any(Admin.class));
    }

    @Test
    void deleteAdmin_whenAdminExists_thenDeleteSuccessfully() {
        // Arrange
        when(adminRepository.findAdminByAdminId_uuid(adminId1)).thenReturn(admin1);
        doNothing().when(adminRepository).delete(admin1); // Mock void method

        // Act
        assertDoesNotThrow(() -> {
            adminService.deleteAdmin(adminId1);
        });

        // Assert
        verify(adminRepository, times(1)).findAdminByAdminId_uuid(adminId1);
        verify(adminRepository, times(1)).delete(admin1);
    }

    @Test
    void deleteAdmin_whenAdminDoesNotExist_thenThrowNotFoundException() {
        // Arrange
        when(adminRepository.findAdminByAdminId_uuid(nonExistentAdminId)).thenReturn(null);

        // Act & Assert
        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            adminService.deleteAdmin(nonExistentAdminId);
        });
        assertEquals("Unknown adminId: " + nonExistentAdminId, exception.getMessage());
        verify(adminRepository, times(1)).findAdminByAdminId_uuid(nonExistentAdminId);
        verify(adminRepository, never()).delete(any(Admin.class));
    }
}