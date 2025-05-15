package com.champsoft.profiledashboardmanagement.Presentation;

import com.champsoft.profiledashboardmanagement.BusinessLogic.AdminService;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = AdminController.class)
public class AdminControllerUnitTest {

    @Mock
    private AdminService adminService;

    @Autowired
    private AdminController adminController;

    private AdminResponseModel responseModel1, responseModel2;
    private AdminRequestModel requestModel;
    private final String TEST_ADMIN_UUID_1 = "admin-uuid-1";

    @BeforeEach
    void setUp() {
        responseModel1 = new AdminResponseModel(TEST_ADMIN_UUID_1, "adminUser1");
        responseModel2 = new AdminResponseModel("admin-uuid-2", "adminUser2");
        requestModel = new AdminRequestModel("newAdmin", "newPass");
    }



}