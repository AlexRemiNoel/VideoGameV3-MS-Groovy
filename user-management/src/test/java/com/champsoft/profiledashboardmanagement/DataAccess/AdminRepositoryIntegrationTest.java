package com.champsoft.profiledashboardmanagement.DataAccess;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("h2")
public class AdminRepositoryIntegrationTest {

    @Autowired
    private AdminRepository adminRepository;

    private Admin admin1;
    private final String TEST_ADMIN_UUID_1 = UUID.randomUUID().toString();
    private final String NON_EXISTENT_ADMIN_UUID = UUID.randomUUID().toString();

    @BeforeEach
    void setUp() {
        adminRepository.deleteAll();

        AdminId adminId1 = new AdminId(TEST_ADMIN_UUID_1);
        admin1 = new Admin(adminId1, "RepoAdmin1", "pass1");
        adminRepository.save(admin1);
    }

    @Test
    void findAdminByAdminId_uuid_whenAdminExists_thenReturnAdmin() {
        Admin foundAdmin = adminRepository.findAdminByAdminId_uuid(TEST_ADMIN_UUID_1);
        assertNotNull(foundAdmin);
        assertEquals(admin1.getAdminId().getUuid(), foundAdmin.getAdminId().getUuid());
    }

    @Test
    void findAdminByAdminId_uuid_whenAdminDoesNotExist_thenReturnNull() {
        Admin foundAdmin = adminRepository.findAdminByAdminId_uuid(NON_EXISTENT_ADMIN_UUID);
        assertNull(foundAdmin);
    }

    @Test
    void existsByAdminId_whenAdminExists_thenReturnTrue() {
        assertTrue(adminRepository.existsByAdminId(admin1.getAdminId()));
    }

    @Test
    void existsByAdminId_whenAdminDoesNotExist_thenReturnFalse() {
        assertFalse(adminRepository.existsByAdminId(new AdminId(NON_EXISTENT_ADMIN_UUID)));
    }

    @Test
    void saveAdmin_shouldPersistAdmin() {
        AdminId newAdminId = new AdminId(UUID.randomUUID().toString());
        Admin newAdmin = new Admin(newAdminId, "NewRepoAdmin", "newPass");
        Admin savedAdmin = adminRepository.save(newAdmin);

        assertNotNull(savedAdmin);
        Optional<Admin> retrieved = adminRepository.findById(newAdminId);
        assertTrue(retrieved.isPresent());
        assertEquals("NewRepoAdmin", retrieved.get().getUsername());
    }
}