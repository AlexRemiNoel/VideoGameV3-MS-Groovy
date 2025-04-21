package com.champsoft.usermanagement.Presentation;


import com.champsoft.usermanagement.BusinessLogic.AdminService;
import com.champsoft.usermanagement.utils.exceptions.InvalidUserInputException;
import com.champsoft.usermanagement.utils.exceptions.NotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AdminControllerUnitTest {
    private final String VALID_ADMIN_ID = "c3540a89-cb47-4c96-888e-ff96708db4d8";
    private final String NOT_FOUND_ADMIN_ID = "c3540a89-cb47-4c96-888eff96708db4d0";
    private final String INVALID_ADMIN_ID = "c3540a89-cb47-4c96-888e-ff96708db4d";
    @Mock
    AdminService adminService;
    @InjectMocks
    AdminController adminController;
    @Test
    public void whenNoAdminsExist_ThenReturnEmptyList() {
        //arrange
        when(adminService.getAllAdmins()).thenReturn(Collections.emptyList());
        //act
        ResponseEntity<List<AdminResponseModel>> adminResponseEntity = adminController.getAdmins();
        //assert
        assertNotNull(adminResponseEntity);
        assertEquals(adminResponseEntity.getStatusCode(), HttpStatus.OK);
        assertArrayEquals(adminResponseEntity.getBody().toArray(),
                new ArrayList<AdminResponseModel>().toArray());
        verify(adminService, times(1)).getAllAdmins();
    }

    @Test
    public void whenAdminExists_ThenReturnAdmin()
    {
        //arrange
        AdminResponseModel adminResponseModel = buildAdminResponseModel();
        when(adminService.getAdminById(VALID_ADMIN_ID)).thenReturn(adminResponseModel);
        //act
        ResponseEntity<AdminResponseModel> adminResponseEntity =
                adminController.getAdminById(VALID_ADMIN_ID);

        //assert
        assertNotNull(adminResponseEntity);
        assertEquals(adminResponseEntity.getStatusCode(), HttpStatus.OK);
        assertNotNull(adminResponseEntity.getBody());
        assertEquals(adminResponseEntity.getBody(), adminResponseModel);
        verify(adminService, times(1)).getAdminById(VALID_ADMIN_ID);
    }

    @Test
    public void whenAdminIdInvalidOnGet_ThenThrowInvalidInputException() {
        //arrange
        when(adminService.getAdminById(INVALID_ADMIN_ID)).thenThrow(new InvalidUserInputException("Invalid admin id: " + INVALID_ADMIN_ID));
        //act and assert

        InvalidUserInputException exception = assertThrowsExactly(InvalidUserInputException.class, () -> {
            adminController.getAdminById(INVALID_ADMIN_ID);
        });
        // Assert exception message
        // the correct exception message should be : "Invalid admin id: " + INVALID_ADMIN_ID
        assertEquals("Invalid admin id: " + INVALID_ADMIN_ID, exception.getMessage());
        verify(adminService, times(0)).getAllAdmins();
    }

    @Test
    public void whenAdminNotFoundOnGet_ThenThrowNotFoundException() {
        //arrange
        when(adminService.getAdminById(NOT_FOUND_ADMIN_ID))
                .thenThrow(new NotFoundException("Unknown adminId: " + NOT_FOUND_ADMIN_ID));
        //act
        NotFoundException exception = assertThrowsExactly(NotFoundException.class, () -> {
            adminController.getAdminById(NOT_FOUND_ADMIN_ID);
        });
        //assert
        assertEquals( "Unknown adminId: " + NOT_FOUND_ADMIN_ID, exception.getMessage());
        verify(adminService, times( 1)).getAdminById(NOT_FOUND_ADMIN_ID);
    }

    @Test
    public void whenAdminValid_ThenReturnNewAdmin() {
        //arrange
        AdminRequestModel adminRequestModel = buildAdminRequestModel();
        AdminResponseModel adminResponseModel = buildAdminResponseModel();
        when(adminService.addAdmin(adminRequestModel)).thenReturn(adminResponseModel);
        //act
        ResponseEntity<AdminResponseModel> adminResponseEntity =
                adminController.addAdmin(adminRequestModel);

        //assert
        assertNotNull(adminResponseEntity);
        assertEquals(adminResponseEntity.getStatusCode(), HttpStatus.CREATED);
        assertNotNull(adminResponseEntity.getBody());
        assertEquals(adminResponseEntity.getBody(), adminResponseModel);
        verify(adminService, times(  1)).addAdmin(adminRequestModel);
    }

    @Test
    public void whenAdminExists_ThenReturnUpdatedAdmin() {
        //arrange
        AdminRequestModel adminRequestModel = buildAdminRequestModel( "Betty");
        AdminResponseModel adminResponseModel = buildAdminResponseModel("Betty");
        when(adminService.updateAdmin(adminRequestModel, VALID_ADMIN_ID)).thenReturn(adminResponseModel);
        //act
        ResponseEntity<AdminResponseModel> adminResponseEntity =
                adminController.updateAdmin(adminRequestModel, VALID_ADMIN_ID);
        //assert
        assertNotNull(adminResponseEntity);
        assertEquals(adminResponseEntity.getStatusCode(), HttpStatus.OK);
        assertNotNull(adminResponseEntity.getBody());
        assertEquals(adminResponseEntity.getBody(), adminResponseModel);
        verify(adminService, times( 1)).updateAdmin(adminRequestModel, VALID_ADMIN_ID);
    }

    @Test
    public void whenAdminDoesNotExistOnUpdate_ThenThrowNotFoundException() {
        //arrange
        AdminRequestModel adminRequestModel = buildAdminRequestModel( "Betty");
        when(adminService.updateAdmin(adminRequestModel, NOT_FOUND_ADMIN_ID)).thenThrow(new NotFoundException("Unknown adminId: " + NOT_FOUND_ADMIN_ID));
        //act
        NotFoundException exception = assertThrowsExactly(NotFoundException.class, () -> {
                adminController.updateAdmin(adminRequestModel, NOT_FOUND_ADMIN_ID);
        });
        //assert
        assertEquals("Unknown adminId: "+ NOT_FOUND_ADMIN_ID, exception.getMessage());
        verify(adminService, times( 1)).updateAdmin(adminRequestModel, NOT_FOUND_ADMIN_ID);
    }

    @Test
    public void whenAdminExists_ThenDeleteAdmin() {
        //arrange
        doNothing().when(adminService).deleteAdmin(VALID_ADMIN_ID);

        //act
        ResponseEntity<Void> responseEntity = adminController.deleteAdmin(VALID_ADMIN_ID);

        //assert
        assertNotNull(responseEntity);
        assertEquals(responseEntity.getStatusCode(), HttpStatus.NO_CONTENT);
        verify(adminService, times(1)).deleteAdmin(VALID_ADMIN_ID);
    }

    @Test
    public void whenAdminDoesNotExistOnDelete_ThenThrowNotFoundException() {
        //arrange
        doThrow(new NotFoundException("Unknown adminId: " + NOT_FOUND_ADMIN_ID))
            .when(adminService).deleteAdmin(NOT_FOUND_ADMIN_ID);

        //act
        NotFoundException exception = assertThrowsExactly(NotFoundException.class, () -> {
            adminController.deleteAdmin(NOT_FOUND_ADMIN_ID);
        });

        //assert
        assertEquals("Unknown adminId: "+ NOT_FOUND_ADMIN_ID, exception.getMessage());
        verify(adminService, times( 1)).deleteAdmin(NOT_FOUND_ADMIN_ID);
    }


    public AdminResponseModel buildAdminResponseModel() {
        return buildAdminResponseModel("John Doe");
    }

    public AdminResponseModel buildAdminResponseModel(String username) {
        AdminResponseModel adminResponseModel = new AdminResponseModel();
        adminResponseModel.setAdminId("valid-admin-id"); // Set the admin ID
        adminResponseModel.setUsername(username); // Set admin name

        // Set any other required fields here
        return adminResponseModel;
    }

    public AdminRequestModel buildAdminRequestModel(){
        return buildAdminRequestModel("John Doe");
    }

    public AdminRequestModel buildAdminRequestModel(String username){
        AdminRequestModel adminRequestModel = new AdminRequestModel();
        adminRequestModel.setUsername(username);
        adminRequestModel.setPassword("test123");
        return adminRequestModel;
    }
}
