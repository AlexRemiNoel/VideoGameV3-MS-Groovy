package com.champsoft.usermanagement.DataAccess;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
public class AdminRepositoryIntegrationTest {
    private final String VALID_ADMIN_ID = "c3540a89-cb47-4c96-888e-ff96708db4d8";
    private final String INVALID_ADMIN_ID = "nonExistentId";
    private final String VALID_ADMIN_USERNAME = "John Doe";
    private final String VALID_ADMIN_PASSWORD = "test123";

    @Autowired
    private AdminRepository adminRepository;

    @BeforeEach
    void setup() {
        Admin admin = new Admin();
        admin.setAdminId(new AdminId(VALID_ADMIN_ID));
        admin.setUsername(VALID_ADMIN_USERNAME);
        admin.setPassword(VALID_ADMIN_PASSWORD);
        adminRepository.save(admin);
    }

    @Test
    public void whenAdminDoesNotExist_ReturnNull() {
        //arrange
        //act
        Admin admin = adminRepository.findAdminByAdminId_uuid(INVALID_ADMIN_ID);
        //assert
        assertNull(admin);
    }

    @Test
    public void whenAdminExist_ReturnAdminById() {
        //arrange

        Admin admin1 = new Admin(
                new AdminId(VALID_ADMIN_ID),
                VALID_ADMIN_USERNAME,
                VALID_ADMIN_PASSWORD
                );
        //act
        Admin admin = adminRepository.findAdminByAdminId_uuid( VALID_ADMIN_ID );

        //assert
        assertNotNull(admin);
        assertEquals(admin1.getAdminId(), admin.getAdminId());
        assertEquals(admin1.getUsername(), admin.getUsername());
        assertEquals(admin1.getPassword(), admin.getPassword());
    }

    @Test
    public void whenAdminsExist_ReturnAllAdmins() {
        //arrange
        Admin admin1 = new Admin(
                new AdminId(VALID_ADMIN_ID),
                VALID_ADMIN_USERNAME,
                VALID_ADMIN_PASSWORD);
        adminRepository.save(admin1);
        Admin admin2 = new Admin(
                new AdminId("e3abc06e-e677-4121-841c-c059e91b8ed2"),
                "Jane Doe",
                "test123");
        adminRepository.save(admin2);
        Long sizeDB = adminRepository.count();
        //act
        List<Admin> admins = adminRepository.findAll();
        //assert
        assertEquals(sizeDB, admins.size());
    }
}
