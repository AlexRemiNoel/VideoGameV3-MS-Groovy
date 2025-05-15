package com.champsoft.profiledashboardmanagement;

import com.champsoft.DataAccess.UserProfileDashboardEntity;
import com.champsoft.DataAccess.UserProfileDashboardRepository;
import com.champsoft.Presentation.DownloadSummaryDto;
import com.champsoft.Presentation.GameSummaryDto;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.dao.DuplicateKeyException; // Make sure this is the correct Spring exception for unique constraint violation with Mongo

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;


@DataMongoTest
// To ensure this test runs with an embedded MongoDB without authentication issues,
// you might need to:
// 1. Ensure `de.flapdoodle.embed.mongo.spring3x` (or similar for your Spring Boot version) is a test dependency.
// 2. Ensure no `spring.data.mongodb.uri` in your main `application.properties` (or `yml`)
//    points to a secured MongoDB instance without providing credentials for tests.
// 3. If you have a main MongoDB URI, you can override it for tests in `src/test/resources/application.properties`
//    or `src/test/resources/application-test.properties` (if using a 'test' profile)
//    by either removing authentication details or pointing to a different test DB, e.g.,
//    `spring.data.mongodb.uri=mongodb://localhost/test_profile_dashboard`
//    For truly embedded, usually, no URI is needed if flapdoodle is present.
public class UserProfileDashboardRepositoryIntegrationTest {

    @Autowired
    private UserProfileDashboardRepository dashboardRepository;

    private UserProfileDashboardEntity entity1, entity2;
    private final String USER_ID_1 = "userRepo1";
    private final String USER_ID_2 = "userRepo2";

    @BeforeEach
    void setUp() {
        // It's crucial that this deleteAll() operation can execute without authentication errors.
        dashboardRepository.deleteAll();

        GameSummaryDto game1 = new GameSummaryDto("g1", "Game One", "Action");
        DownloadSummaryDto download1 = new DownloadSummaryDto("d1", "url1", "COMPLETED", "Game One");

        entity1 = new UserProfileDashboardEntity();
        entity1.setUserId(USER_ID_1);
        entity1.setUsername("Repo User One");
        entity1.setEmail("repo1@example.com");
        entity1.setBalance(10.0);
        entity1.setGames(Collections.singletonList(game1));
        entity1.setDownloads(Collections.singletonList(download1));
        entity1.setLastUpdatedAt(LocalDateTime.now());

        entity2 = new UserProfileDashboardEntity();
        entity2.setUserId(USER_ID_2);
        entity2.setUsername("Repo User Two");
        entity2.setEmail("repo2@example.com");
        entity2.setBalance(20.0);
        entity2.setGames(Collections.emptyList());
        entity2.setDownloads(Collections.emptyList());
        entity2.setLastUpdatedAt(LocalDateTime.now().minusHours(1));
    }

    @AfterEach
    void tearDown() {
        dashboardRepository.deleteAll();
    }


    @Test
    void whenFindByUserId_andDashboardExists_thenReturnDashboard() {
        // Arrange
        dashboardRepository.save(entity1);

        // Act
        Optional<UserProfileDashboardEntity> foundEntityOpt = dashboardRepository.findByUserId(USER_ID_1);

        // Assert
        assertThat(foundEntityOpt).isPresent();
        UserProfileDashboardEntity foundEntity = foundEntityOpt.get();
        assertThat(foundEntity.getUserId()).isEqualTo(USER_ID_1);
        assertThat(foundEntity.getUsername()).isEqualTo("Repo User One");
        assertThat(foundEntity.getGames()).hasSize(1);
        assertThat(foundEntity.getGames().get(0).getTitle()).isEqualTo("Game One");
    }

    @Test
    void whenFindByUserId_andDashboardDoesNotExist_thenReturnEmptyOptional() {
        // Act
        Optional<UserProfileDashboardEntity> foundEntityOpt = dashboardRepository.findByUserId("nonExistentUserId");

        // Assert
        assertThat(foundEntityOpt).isNotPresent();
    }

    @Test
    void whenSaveNewDashboard_thenDashboardIsPersisted() {
        // Act
        UserProfileDashboardEntity savedEntity = dashboardRepository.save(entity1);

        // Assert
        assertThat(savedEntity).isNotNull();
        assertThat(savedEntity.getId()).isNotNull();
        assertThat(savedEntity.getUserId()).isEqualTo(USER_ID_1);

        Optional<UserProfileDashboardEntity> retrievedEntity = dashboardRepository.findById(savedEntity.getId());
        assertThat(retrievedEntity).isPresent();
        assertThat(retrievedEntity.get().getUsername()).isEqualTo("Repo User One");
    }

    @Test
    void whenSaveDashboard_withExistingUserId_thenThrowsDuplicateKeyException() {
        // Arrange
        // Ensure the UserProfileDashboardEntity has @Indexed(unique=true) on userId field for this to work
        dashboardRepository.save(entity1);

        UserProfileDashboardEntity duplicateEntity = new UserProfileDashboardEntity();
        duplicateEntity.setUserId(USER_ID_1);
        duplicateEntity.setUsername("Another Name");
        duplicateEntity.setLastUpdatedAt(LocalDateTime.now());
        duplicateEntity.setEmail("another@example.com"); // Ensure all required fields are set if any

        // Act & Assert
        assertThrows(DuplicateKeyException.class, () -> {
            dashboardRepository.save(duplicateEntity);
        });
    }

    @Test
    void whenUpdateExistingDashboard_byFetchingAndSaving_thenUpdatesSuccessfully() {
        // Arrange
        UserProfileDashboardEntity savedOriginal = dashboardRepository.save(entity1);

        // Act
        Optional<UserProfileDashboardEntity> toUpdateOpt = dashboardRepository.findByUserId(USER_ID_1);
        assertThat(toUpdateOpt).isPresent();
        UserProfileDashboardEntity toUpdate = toUpdateOpt.get();
        toUpdate.setUsername("Updated Repo User One");
        toUpdate.setBalance(150.0);
        LocalDateTime newTime = LocalDateTime.now().plusHours(1).withNano(0); // Truncate nanos for comparison
        toUpdate.setLastUpdatedAt(newTime);

        UserProfileDashboardEntity updatedEntity = dashboardRepository.save(toUpdate);

        // Assert
        assertThat(updatedEntity.getId()).isEqualTo(savedOriginal.getId());
        assertThat(updatedEntity.getUsername()).isEqualTo("Updated Repo User One");
        assertThat(updatedEntity.getBalance()).isEqualTo(150.0);
        assertThat(updatedEntity.getLastUpdatedAt().withNano(0)).isEqualTo(newTime); // Compare with nano truncation

        Optional<UserProfileDashboardEntity> foundAfterUpdate = dashboardRepository.findByUserId(USER_ID_1);
        assertThat(foundAfterUpdate).isPresent();
        assertThat(foundAfterUpdate.get().getUsername()).isEqualTo("Updated Repo User One");
    }


    @Test
    void whenFindAll_andDashboardsExist_thenReturnAllDashboards() {
        // Arrange
        dashboardRepository.save(entity1);
        dashboardRepository.save(entity2);

        // Act
        List<UserProfileDashboardEntity> dashboards = dashboardRepository.findAll();

        // Assert
        assertThat(dashboards).hasSize(2);
        assertThat(dashboards).extracting(UserProfileDashboardEntity::getUserId).containsExactlyInAnyOrder(USER_ID_1, USER_ID_2);
    }

    @Test
    void whenFindAll_andNoDashboardsExist_thenReturnEmptyList() {
        // Act
        List<UserProfileDashboardEntity> dashboards = dashboardRepository.findAll();

        // Assert
        assertThat(dashboards).isEmpty();
    }

    @Test
    void whenDeleteByUserId_andDashboardExists_thenDashboardIsRemoved() {
        // Arrange
        dashboardRepository.save(entity1);
        assertThat(dashboardRepository.findByUserId(USER_ID_1)).isPresent();

        // Act
        dashboardRepository.deleteByUserId(USER_ID_1);

        // Assert
        assertThat(dashboardRepository.findByUserId(USER_ID_1)).isNotPresent();
        assertThat(dashboardRepository.count()).isEqualTo(0);
    }

    @Test
    void whenDeleteByUserId_andDashboardDoesNotExist_thenNoChangeAndNoException() {
        // Arrange
        dashboardRepository.save(entity1);
        long initialCount = dashboardRepository.count();

        // Act
        dashboardRepository.deleteByUserId("nonExistentUserIdToDelete");

        // Assert
        assertThat(dashboardRepository.count()).isEqualTo(initialCount);
    }
}