package com.example.videogamev3.dataaccesslayer;


import com.example.videogamev3.DownloadManagement.DataAccess.Download;
import com.example.videogamev3.DownloadManagement.DataAccess.DownloadId;
import com.example.videogamev3.DownloadManagement.DataAccess.DownloadRepository;
import com.example.videogamev3.DownloadManagement.DataAccess.DownloadStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.transaction.annotation.Transactional; // Import Transactional for delete test

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*; // For assertTrue/False

@DataJpaTest // Configure H2, scans for @Entity classes, configures Spring Data JPA repositories
class DownloadIntegration {

    @Autowired
    private TestEntityManager entityManager; // Helper for JPA tests

    @Autowired
    private DownloadRepository downloadRepository;

    private Download download1;
    private Download download2;
    private String download1Id;
    private String download2Id;

    @BeforeEach
    void setUp() {
        // Clean up potentially persisted data from other tests if needed (though @DataJpaTest rolls back)
        // downloadRepository.deleteAll();

        download1Id = UUID.randomUUID().toString();
        download1 = new Download(new DownloadId(download1Id), "http://example.com/file1.zip", DownloadStatus.PENDING);

        download2Id = UUID.randomUUID().toString();
        download2 = new Download(new DownloadId(download2Id), "http://example.com/file2.iso", DownloadStatus.DOWNLOADING);

        // Persist using EntityManager for setup if needed before repository interaction
        entityManager.persist(download1);
        entityManager.persist(download2);
        entityManager.flush(); // Ensure data is written before tests run repository methods
    }

    @Test
    void whenFindDownloadById_Uuid_andExists_thenReturnDownload() {
        // Act
        Download found = downloadRepository.findDownloadById_Uuid(download1Id);

        // Assert
        assertThat(found).isNotNull();
        assertThat(found.getId().getUuid()).isEqualTo(download1Id);
        assertThat(found.getSourceUrl()).isEqualTo(download1.getSourceUrl());
        assertThat(found.getDownloadStatus()).isEqualTo(download1.getDownloadStatus());
    }

    @Test
    void whenFindDownloadById_Uuid_andNotExists_thenReturnNull() {
        // Act
        Download found = downloadRepository.findDownloadById_Uuid(UUID.randomUUID().toString());

        // Assert
        assertThat(found).isNull();
    }

    @Test
    void whenExistsDownloadById_Uuid_andExists_thenReturnTrue() {
        // Act
        boolean exists = downloadRepository.existsDownloadById_Uuid(download1Id);

        // Assert
        assertTrue(exists);
    }

    @Test
    void whenExistsDownloadById_Uuid_andNotExists_thenReturnFalse() {
        // Act
        boolean exists = downloadRepository.existsDownloadById_Uuid(UUID.randomUUID().toString());

        // Assert
        assertFalse(exists);
    }

    @Test
    @Transactional // Explicitly needed for delete operations within a test using JpaRepository custom methods
    void whenDeleteDownloadById_Uuid_andExists_thenShouldBeDeleted() {
        // Arrange
        assertTrue(downloadRepository.existsDownloadById_Uuid(download1Id), "Precondition failed: Download should exist before delete");

        // Act
        downloadRepository.deleteDownloadById_Uuid(download1Id);
        entityManager.flush(); // Ensure delete is processed
        entityManager.clear(); // Clear persistence context cache

        // Assert
        Download found = downloadRepository.findDownloadById_Uuid(download1Id);
        assertThat(found).isNull();
        assertFalse(downloadRepository.existsDownloadById_Uuid(download1Id));
    }

    @Test
    void whenFindAll_thenReturnAllDownloads() {
        // Act
        List<Download> downloads = downloadRepository.findAll();

        // Assert
        assertThat(downloads).hasSize(2);
        assertThat(downloads).extracting(d -> d.getId().getUuid())
                .containsExactlyInAnyOrder(download1Id, download2Id);
    }

    @Test
    void whenSave_thenDownloadIsPersisted() {
        // Arrange
        String newId = UUID.randomUUID().toString();
        Download newDownload = new Download(new DownloadId(newId), "http://new.com/new.rar", DownloadStatus.PENDING);

        // Act
        Download savedDownload = downloadRepository.save(newDownload);
        entityManager.flush();
        entityManager.clear();

        // Assert
        assertThat(savedDownload).isNotNull();
        assertThat(savedDownload.getId().getUuid()).isEqualTo(newId);

        Download found = downloadRepository.findDownloadById_Uuid(newId);
        assertThat(found).isNotNull();
        assertThat(found.getSourceUrl()).isEqualTo(newDownload.getSourceUrl());
    }
}