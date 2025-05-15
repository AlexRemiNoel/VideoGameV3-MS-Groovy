package com.champsoft.profiledashboardmanagement.DataMapper;

import com.champsoft.profiledashboardmanagement.DataAccess.Admin;
import com.champsoft.profiledashboardmanagement.Presentation.AdminRequestModel;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
public class AdminRequestMapperTest {
    @Autowired
    private AdminRequestMapper adminRequestMapper;


    @Test
    public void testAdminRequestModelToAdmin() {
        // Arrange
        AdminRequestModel requestModel = new AdminRequestModel(
                "TestAdmin",
                "securePassword"
        );

        // Act
        Admin user = adminRequestMapper.adminRequestModelToAdmin(requestModel);

        // Assert
        assertNotNull(user);
        assertNotNull(user.getAdminId());
        assertNotNull(user.getAdminId().getUuid());
        assertEquals("TestAdmin", user.getUsername());
        assertEquals("securePassword", user.getPassword());
    }

    @Test
    public void testAdminRequestModelToAdmin_ListMapping() {
        // Arrange
        AdminRequestModel admin1 = new AdminRequestModel("Admin1", "pass1");
        AdminRequestModel admin2 = new AdminRequestModel("Admin2", "pass2");
        List<AdminRequestModel> requestModels = Arrays.asList(admin1, admin2);

        // Act
        List<AdminRequestModel> admins = adminRequestMapper.adminRequestModelToAdmin(requestModels);

        // Assert
        assertNotNull(admins);
        assertEquals(2, admins.size());

        assertEquals("Admin1", admins.get(0).getUsername());
        assertEquals("pass1", admins.get(0).getPassword());

        assertEquals("Admin2", admins.get(1).getUsername());
        assertEquals("pass2", admins.get(1).getPassword());
    }
}
