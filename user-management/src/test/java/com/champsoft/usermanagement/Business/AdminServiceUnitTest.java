package com.champsoft.usermanagement.Business;

import com.champsoft.usermanagement.BusinessLogic.AdminService;
import com.champsoft.usermanagement.DataAccess.Admin;
import com.champsoft.usermanagement.DataAccess.AdminId;
import com.champsoft.usermanagement.DataAccess.AdminRepository;
import com.champsoft.usermanagement.DataMapper.AdminRequestMapper;
import com.champsoft.usermanagement.DataMapper.AdminResponseMapper;
import com.champsoft.usermanagement.Presentation.AdminRequestModel;
import com.champsoft.usermanagement.Presentation.AdminResponseModel;
import com.champsoft.usermanagement.utils.exceptions.NotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

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

    private final String VALID_ADMIN_ID = "c3540a89-cb47-4c96-888e-ff96708db4d8";
    private final String VALID_ADMIN_USERNAME = "John Doe";
    private final String VALID_ADMIN_PASSWORD = "test123";

    private Admin createTestAdmin() {
        Admin admin = new Admin();
        admin.setAdminId(new AdminId(VALID_ADMIN_ID));
        admin.setUsername(VALID_ADMIN_USERNAME);
        admin.setPassword(VALID_ADMIN_PASSWORD);
        return admin;
    }

    private AdminResponseModel createTestAdminResponseModel() {
        AdminResponseModel admin = new AdminResponseModel();
        admin.setAdminId(VALID_ADMIN_ID);
        admin.setUsername(VALID_ADMIN_USERNAME);
        return admin;
    }

    private AdminRequestModel createTestAdminRequestModel() {
        AdminRequestModel admin = new AdminRequestModel();
        admin.setUsername(VALID_ADMIN_USERNAME);
        admin.setPassword(VALID_ADMIN_PASSWORD);
        return admin;
    }

    @Test
    public void whenGetAdminById_existingUuid_thenReturnAdminResponseModel() {
        // Arrange
        String uuid = UUID.randomUUID().toString();
        Admin admin = createTestAdmin();
        AdminResponseModel responseModel = createTestAdminResponseModel();
        when(adminRepository.findAdminByAdminId_uuid(uuid)).thenReturn(admin);
        when(adminResponseMapper.adminToAdminResponseModel(admin)).thenReturn(responseModel);
        //Act
        AdminResponseModel result = adminService.getAdminById(uuid);

        // Assert
        assertNotNull(result);
        assertEquals(responseModel, result);
        verify(adminRepository, times(1)).findAdminByAdminId_uuid(uuid);
        verify(adminResponseMapper, times(1)).adminToAdminResponseModel(admin);
    }

    @Test
    public void whenGetAdminById_nonExistingUuid_thenThrowNotFoundException() {
        // Arrange
        String uuid = UUID.randomUUID().toString();

        when(adminRepository.findAdminByAdminId_uuid(uuid)).thenReturn(null);

        // Act and Assert
        assertThrows(NotFoundException.class, () -> adminService.getAdminById(uuid));
        verify(adminRepository, times(1)).findAdminByAdminId_uuid(uuid);
        verify(adminResponseMapper, never()).adminToAdminResponseModel((Admin) any());
    }

    @Test
    public void whenGetAllAdmins_adminsExist_thenReturnListOfAdminResponseModels() {
        // Arrange
        List<Admin> admins = Arrays.asList(createTestAdmin(), createTestAdmin());
        List<AdminResponseModel> responseModels = Arrays.asList(createTestAdminResponseModel(), createTestAdminResponseModel());
        when(adminRepository.findAll()).thenReturn(admins);
        when(adminResponseMapper.adminToAdminResponseModel(admins)).thenReturn(responseModels);

        // Act
        List<AdminResponseModel> result = adminService.getAllAdmins();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(adminRepository, times(1)).findAll();
        verify(adminResponseMapper, times(1)).adminToAdminResponseModel(admins);
    }

    @Test
    public void whenGetAllAdmins_noAdminsExist_thenReturnEmptyList() {
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
    public void whenAddAdmin_validRequestModel_thenReturnAdminResponseModel() {
        // Arrange
        AdminRequestModel requestModel = createTestAdminRequestModel();
        Admin admin = createTestAdmin();
        AdminResponseModel responseModel = createTestAdminResponseModel();
        when(adminRequestMapper.adminRequestModelToAdmin(requestModel)).thenReturn(admin);
        when(adminRepository.save(admin)).thenReturn(admin);
        when(adminResponseMapper.adminToAdminResponseModel(admin)).thenReturn(responseModel);

        // Act
        AdminResponseModel result = adminService.addAdmin(requestModel);

        // Assert
        assertNotNull(result);
        assertEquals(responseModel.getUsername(), result.getUsername());
        verify(adminRequestMapper, times(1)).adminRequestModelToAdmin(requestModel);
        verify(adminRepository, times(1)).save(admin);
        verify(adminResponseMapper, times(1)).adminToAdminResponseModel(admin);
    }

    @Test
    public void whenUpdateAdmin_validRequestModel_thenReturnAdminResponseModel() {
        // Arrange
        AdminRequestModel requestModel = createTestAdminRequestModel();
        Admin admin = createTestAdmin();
        AdminResponseModel responseModel = createTestAdminResponseModel();
        String uuid = admin.getAdminId().getUuid();

        when(adminRepository.findAdminByAdminId_uuid(uuid)).thenReturn(admin);
        when(adminRepository.save(admin)).thenReturn(admin);
        when(adminResponseMapper.adminToAdminResponseModel(admin)).thenReturn(responseModel);

        // Act
        AdminResponseModel result = adminService.updateAdmin(requestModel, uuid);

        // Assert
        assertNotNull(result);
        assertEquals(responseModel.getUsername(), result.getUsername());
        verify(adminRepository, times(1)).save(admin);
        verify(adminResponseMapper, times(1)).adminToAdminResponseModel(admin);
    }


    @Test
    public void whenDeleteAdmin_existingUuid_thenAdminIsDeleted() {
        // Arrange
        String uuid = UUID.randomUUID().toString();
        Admin admin = createTestAdmin();
        when(adminRepository.findAdminByAdminId_uuid(uuid)).thenReturn(admin);

        // Act
        adminService.deleteAdmin(uuid);

        // Assert
        verify(adminRepository, times(1)).findAdminByAdminId_uuid(uuid);
        verify(adminRepository, times(1)).delete(admin);
    }
}
