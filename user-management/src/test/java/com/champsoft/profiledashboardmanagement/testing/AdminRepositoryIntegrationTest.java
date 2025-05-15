package com.champsoft.profiledashboardmanagement.testing; // Make sure package name is correct

import com.champsoft.profiledashboardmanagement.DataAccess.Admin;
import com.champsoft.profiledashboardmanagement.DataAccess.AdminId;
import com.champsoft.profiledashboardmanagement.DataAccess.AdminRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat; // Using AssertJ for fluent assertions
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest // Configures H2, focuses on JPA components, transactional rollbacks
// Optional: Add ActiveProfiles if your main config might interfere
// @ActiveProfiles("h2")
class AdminRepositoryIntegrationTest {

    @Autowired
    private AdminRepository adminRepository;

    private Admin admin1;
    private Admin admin2;
    private AdminId adminIdObject1; // Store the AdminId object
    private AdminId adminIdObject2;

    @BeforeEach
    void setUp() {
        adminRepository.deleteAll(); // Ensure clean state (though @DataJpaTest rolls back)

        String uuid1 = UUID.randomUUID().toString();
        String uuid2 = UUID.randomUUID().toString();

        adminIdObject1 = new AdminId(uuid1); // Create the AdminId object
        adminIdObject2 = new AdminId(uuid2); // Create the second AdminId object

        admin1 = new Admin(adminIdObject1, "repoTestAdmin1", "pass1");
        admin2 = new Admin(adminIdObject2, "repoTestAdmin2", "pass2");
    }

    @Test
    void findAdminByAdminId_uuid_whenAdminExists_thenReturnAdmin() {
        // Arrange
        adminRepository.save(admin1);

        // Act
        // Use the custom query method which expects a String uuid
        Admin foundAdmin = adminRepository.findAdminByAdminId_uuid(admin1.getAdminId().getUuid());

        // Assert
        assertNotNull(foundAdmin);
        assertEquals(admin1.getAdminId(), foundAdmin.getAdminId());
        assertEquals(admin1.getUsername(), foundAdmin.getUsername());
        assertEquals(admin1.getPassword(), foundAdmin.getPassword());
    }

    @Test
    void findAdminByAdminId_uuid_whenAdminDoesNotExist_thenReturnNull() {
        // Arrange
        String nonExistentUuid = UUID.randomUUID().toString();

        // Act
        Admin foundAdmin = adminRepository.findAdminByAdminId_uuid(nonExistentUuid);

        // Assert
        assertNull(foundAdmin);
    }

    @Test
    void existsByAdminId_whenAdminExists_thenReturnTrue() {
        // Arrange
        adminRepository.save(admin1);

        // Act
        // Use the custom query method which expects an AdminId object
        boolean exists = adminRepository.existsByAdminId(admin1.getAdminId());

        // Assert
        assertTrue(exists);
    }

    @Test
    void existsByAdminId_whenAdminDoesNotExist_thenReturnFalse() {
        // Arrange
        AdminId nonExistentIdObject = new AdminId(UUID.randomUUID().toString());

        // Act
        boolean exists = adminRepository.existsByAdminId(nonExistentIdObject);

        // Assert
        assertFalse(exists);
    }

    @Test
    void save_whenNewAdmin_thenPersistAdmin() {
        // Act
        Admin savedAdmin = adminRepository.save(admin1);
        // *** FIX: Use findById with the correct ID type (AdminId) ***
        Admin foundAdmin = adminRepository.findById(admin1.getAdminId()).orElse(null);

        // Assert
        assertNotNull(savedAdmin);
        assertNotNull(savedAdmin.getAdminId());
        assertEquals(admin1.getAdminId().getUuid(), savedAdmin.getAdminId().getUuid()); // Compare UUID strings if needed

        assertNotNull(foundAdmin);
        assertEquals(admin1.getUsername(), foundAdmin.getUsername());
        assertEquals(admin1.getAdminId(), foundAdmin.getAdminId()); // Compare the full AdminId object
    }

    @Test
    void save_whenUpdateAdmin_thenUpdateAdmin() {
        // Arrange
        adminRepository.save(admin1); // Save initial state
        String updatedUsername = "updatedRepoAdmin";
        admin1.setUsername(updatedUsername); // Modify existing managed entity

        // Act
        Admin savedAdmin = adminRepository.save(admin1); // Save the updated entity
        // Find using the custom method by String UUID for verification
        Admin foundAdmin = adminRepository.findAdminByAdminId_uuid(admin1.getAdminId().getUuid());

        // Assert
        assertNotNull(savedAdmin);
        assertEquals(updatedUsername, savedAdmin.getUsername());
        assertEquals(admin1.getAdminId(), savedAdmin.getAdminId());

        assertNotNull(foundAdmin);
        assertEquals(updatedUsername, foundAdmin.getUsername());
        assertEquals(admin1.getAdminId(), foundAdmin.getAdminId());
    }

    @Test
    void findAll_whenMultipleAdminsExist_thenReturnAllAdmins() {
        // Arrange
        adminRepository.save(admin1);
        adminRepository.save(admin2);

        // Act
        List<Admin> admins = adminRepository.findAll();

        // Assert
        assertNotNull(admins);
        assertEquals(2, admins.size());
        // Using AssertJ for better list/object comparison
        assertThat(admins).containsExactlyInAnyOrder(admin1, admin2);
        // Or check specific fields if preferred:
        assertThat(admins).extracting(Admin::getUsername)
                .containsExactlyInAnyOrder(admin1.getUsername(), admin2.getUsername());
        assertThat(admins).extracting(Admin::getAdminId)
                .containsExactlyInAnyOrder(admin1.getAdminId(), admin2.getAdminId());
    }

    @Test
    void findAll_whenNoAdminsExist_thenReturnEmptyList() {
        // Act
        List<Admin> admins = adminRepository.findAll();

        // Assert
        assertNotNull(admins);
        assertTrue(admins.isEmpty());
    }

    @Test
    void delete_whenAdminExists_thenRemoveAdmin() {
        // Arrange
        adminRepository.save(admin1);
        // Use existsById from JpaRepository, expects AdminId
        assertTrue(adminRepository.existsById(admin1.getAdminId()));

        // Act
        adminRepository.delete(admin1);
        adminRepository.flush(); // Ensure delete is executed before checking

        // Assert
        // Use existsById from JpaRepository, expects AdminId
        assertFalse(adminRepository.existsById(admin1.getAdminId()));
        assertNull(adminRepository.findAdminByAdminId_uuid(admin1.getAdminId().getUuid()));
    }
}