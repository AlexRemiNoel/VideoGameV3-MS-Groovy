package com.champsoft.profiledashboardmanagement.DataMapper;

import com.champsoft.profiledashboardmanagement.DataAccess.Admin;
import com.champsoft.profiledashboardmanagement.DataAccess.AdminId;
import com.champsoft.profiledashboardmanagement.Presentation.AdminResponseModel;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class AdminResponseMapperTest {

    private final AdminResponseMapper mapper = Mappers.getMapper(AdminResponseMapper.class);

    private Admin createTestAdmin() {
        Admin admin = new Admin();
        admin.setAdminId(new AdminId(UUID.randomUUID().toString()));
        admin.setUsername("adminUser");
        admin.setPassword("securePass123");
        return admin;
    }

    @Test
    void testAdminToAdminResponseModel() {
        Admin admin = createTestAdmin();

        AdminResponseModel responseModel = mapper.adminToAdminResponseModel(admin);

        assertNotNull(responseModel);
        assertEquals(admin.getAdminId().getUuid(), responseModel.getAdminId());
        assertEquals(admin.getUsername(), responseModel.getUsername());
    }

    @Test
    void testAdminListToAdminResponseModelList() {
        Admin admin1 = createTestAdmin();
        Admin admin2 = createTestAdmin();

        List<Admin> admins = Arrays.asList(admin1, admin2);
        List<AdminResponseModel> responseModels = mapper.adminToAdminResponseModel(admins);

        assertNotNull(responseModels);
        assertEquals(2, responseModels.size());
        assertEquals(admin1.getAdminId().getUuid(), responseModels.get(0).getAdminId());
        assertEquals(admin2.getAdminId().getUuid(), responseModels.get(1).getAdminId());
    }
}
