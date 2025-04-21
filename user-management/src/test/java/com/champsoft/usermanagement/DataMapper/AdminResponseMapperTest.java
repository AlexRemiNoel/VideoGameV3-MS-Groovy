package com.champsoft.usermanagement.DataMapper;

import com.champsoft.usermanagement.DataAccess.Admin;
import com.champsoft.usermanagement.DataAccess.AdminId;
import com.champsoft.usermanagement.Presentation.AdminResponseModel;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class AdminResponseMapperTest {

    private final AdminResponseMapper mapper = Mappers.getMapper(AdminResponseMapper.class);

    @Test
    public void testAdminToAdminResponseModel() {
        // Arrange
        String uuid = UUID.randomUUID().toString();
        AdminId adminId = new AdminId();
        adminId.setUuid(uuid);

        Admin admin = new Admin();
        admin.setAdminId(adminId);
        admin.setUsername("TestAdmin");

        // Act
        AdminResponseModel responseModel = mapper.adminToAdminResponseModel(admin);

        // Assert
        assertNotNull(responseModel);
        assertEquals(uuid, responseModel.getAdminId());
        assertEquals("TestAdmin", responseModel.getUsername());
    }
}