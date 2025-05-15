package com.champsoft.profiledashboardmanagement.Business;

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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AdminServiceUnitTest {

    @Mock
    private AdminRepository adminRepository;

    @Mock
    private AdminResponseMapper adminResponseMapper;

    @Mock
    private AdminRequestMapper adminRequestMapper;

    @InjectMocks
    private AdminService adminService;

    private Admin admin1, admin2;
    private AdminResponseModel responseModel1, responseModel2;
    private AdminRequestModel requestModel;
    private final String TEST_ADMIN_UUID_1 = "admin-uuid-1";
    private final String TEST_ADMIN_UUID_2 = "admin-uuid-2";
    private final String NON_EXISTENT_ADMIN_UUID = "admin-uuid-non-existent";

    @BeforeEach
    void setUp() {
        AdminId adminId1 = new AdminId(TEST_ADMIN_UUID_1);
        admin1 = new Admin(adminId1, "adminUser1", "pass1");
        responseModel1 = new AdminResponseModel(TEST_ADMIN_UUID_1, "adminUser1");

        AdminId adminId2 = new AdminId(TEST_ADMIN_UUID_2);
        admin2 = new Admin(adminId2, "adminUser2", "pass2");
        responseModel2 = new AdminResponseModel(TEST_ADMIN_UUID_2, "adminUser2");

        requestModel = new AdminRequestModel("newAdmin", "newPass");
    }

    @Test
    void getAllAdmins_whenAdminsExist_thenReturnListOfAdminResponseModels() {
        when(adminRepository.findAll()).thenReturn(Arrays.asList(admin1, admin2));
        when(adminResponseMapper.adminToAdminResponseModel(Arrays.asList(admin1, admin2)))
                .thenReturn(Arrays.asList(responseModel1, responseModel2));

        List<AdminResponseModel> result = adminService.getAllAdmins();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(responseModel1.getUsername(), result.get(0).getUsername());
        verify(adminRepository, times(1)).findAll();
        verify(adminResponseMapper, times(1)).adminToAdminResponseModel(Arrays.asList(admin1, admin2));
    }

    @Test
    void getAllAdmins_whenNoAdminsExist_thenReturnEmptyList() {
        when(adminRepository.findAll()).thenReturn(Collections.emptyList());
        when(adminResponseMapper.adminToAdminResponseModel(Collections.emptyList()))
                .thenReturn(Collections.emptyList());

        List<AdminResponseModel> result = adminService.getAllAdmins();

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(adminRepository, times(1)).findAll();
    }

    @Test
    void getAdminById_whenAdminExists_thenReturnAdminResponseModel() {
        when(adminRepository.findAdminByAdminId_uuid(TEST_ADMIN_UUID_1)).thenReturn(admin1);
        when(adminResponseMapper.adminToAdminResponseModel(admin1)).thenReturn(responseModel1);

        AdminResponseModel result = adminService.getAdminById(TEST_ADMIN_UUID_1);

        assertNotNull(result);
        assertEquals(responseModel1.getUsername(), result.getUsername());
        verify(adminRepository, times(1)).findAdminByAdminId_uuid(TEST_ADMIN_UUID_1);
        verify(adminResponseMapper, times(1)).adminToAdminResponseModel(admin1);
    }

    @Test
    void getAdminById_whenAdminDoesNotExist_thenThrowNotFoundException() {
        when(adminRepository.findAdminByAdminId_uuid(NON_EXISTENT_ADMIN_UUID)).thenReturn(null);

        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            adminService.getAdminById(NON_EXISTENT_ADMIN_UUID);
        });
        assertEquals("Unknown adminId: " + NON_EXISTENT_ADMIN_UUID, exception.getMessage());
        verify(adminRepository, times(1)).findAdminByAdminId_uuid(NON_EXISTENT_ADMIN_UUID);
        verify(adminResponseMapper, never()).adminToAdminResponseModel(any(Admin.class));
    }

    @Test
    void addAdmin_whenValidRequest_thenReturnAdminResponseModel() {
        Admin newAdmin = new Admin(new AdminId("new-admin-uuid"), "newAdmin", "newPass");
        AdminResponseModel newResponseModel = new AdminResponseModel("new-admin-uuid", "newAdmin");

        when(adminRequestMapper.adminRequestModelToAdmin(requestModel)).thenReturn(newAdmin);
        when(adminRepository.save(newAdmin)).thenReturn(newAdmin);
        when(adminResponseMapper.adminToAdminResponseModel(newAdmin)).thenReturn(newResponseModel);

        AdminResponseModel result = adminService.addAdmin(requestModel);

        assertNotNull(result);
        assertEquals(newResponseModel.getUsername(), result.getUsername());
        verify(adminRequestMapper, times(1)).adminRequestModelToAdmin(requestModel);
        verify(adminRepository, times(1)).save(newAdmin);
        verify(adminResponseMapper, times(1)).adminToAdminResponseModel(newAdmin);
    }


    @Test
    void updateAdmin_whenAdminExistsAndValidRequest_thenReturnUpdatedAdminResponseModel() {
        AdminRequestModel updateRequest = new AdminRequestModel("updatedAdmin", "updatedPass");
        Admin existingAdmin = admin1; // Uses TEST_ADMIN_UUID_1
        Admin updatedAdminEntity = new Admin(existingAdmin.getAdminId(), "updatedAdmin", "updatedPass");
        AdminResponseModel updatedResponse = new AdminResponseModel(TEST_ADMIN_UUID_1, "updatedAdmin");

        when(adminRepository.findAdminByAdminId_uuid(TEST_ADMIN_UUID_1)).thenReturn(existingAdmin);
        when(adminRepository.save(any(Admin.class))).thenReturn(updatedAdminEntity);
        when(adminResponseMapper.adminToAdminResponseModel(updatedAdminEntity)).thenReturn(updatedResponse);

        AdminResponseModel result = adminService.updateAdmin(updateRequest, TEST_ADMIN_UUID_1);

        assertNotNull(result);
        assertEquals("updatedAdmin", result.getUsername());
        verify(adminRepository, times(1)).findAdminByAdminId_uuid(TEST_ADMIN_UUID_1);
        verify(adminRepository, times(1)).save(argThat(savedAdmin ->
                savedAdmin.getUsername().equals("updatedAdmin") &&
                        savedAdmin.getPassword().equals("updatedPass") &&
                        savedAdmin.getAdminId().getUuid().equals(TEST_ADMIN_UUID_1)
        ));
        verify(adminResponseMapper, times(1)).adminToAdminResponseModel(updatedAdminEntity);
    }

    @Test
    void updateAdmin_whenAdminDoesNotExist_thenThrowNotFoundException() {
        AdminRequestModel updateRequest = new AdminRequestModel("any", "any");
        when(adminRepository.findAdminByAdminId_uuid(NON_EXISTENT_ADMIN_UUID)).thenReturn(null);

        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            adminService.updateAdmin(updateRequest, NON_EXISTENT_ADMIN_UUID);
        });
        assertEquals("Unknown adminId: " + NON_EXISTENT_ADMIN_UUID, exception.getMessage());
        verify(adminRepository, times(1)).findAdminByAdminId_uuid(NON_EXISTENT_ADMIN_UUID);
        verify(adminRepository, never()).save(any(Admin.class));
    }

    @Test
    void deleteAdmin_whenAdminExists_thenDeleteAdmin() {
        when(adminRepository.findAdminByAdminId_uuid(TEST_ADMIN_UUID_1)).thenReturn(admin1);
        doNothing().when(adminRepository).delete(admin1);

        adminService.deleteAdmin(TEST_ADMIN_UUID_1);

        verify(adminRepository, times(1)).findAdminByAdminId_uuid(TEST_ADMIN_UUID_1);
        verify(adminRepository, times(1)).delete(admin1);
    }

    @Test
    void deleteAdmin_whenAdminDoesNotExist_thenThrowNotFoundException() {
        when(adminRepository.findAdminByAdminId_uuid(NON_EXISTENT_ADMIN_UUID)).thenReturn(null);

        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            adminService.deleteAdmin(NON_EXISTENT_ADMIN_UUID);
        });
        assertEquals("Unknown adminId: " + NON_EXISTENT_ADMIN_UUID, exception.getMessage());
        verify(adminRepository, times(1)).findAdminByAdminId_uuid(NON_EXISTENT_ADMIN_UUID);
        verify(adminRepository, never()).delete(any(Admin.class));
    }
}