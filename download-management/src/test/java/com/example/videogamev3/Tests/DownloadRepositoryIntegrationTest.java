package com.example.videogamev3.Tests;

import com.example.videogamev3.DownloadManagement.DataAccess.Download;
import com.example.videogamev3.DownloadManagement.DataAccess.DownloadId;
import com.example.videogamev3.DownloadManagement.DataAccess.DownloadRepository;
import com.example.videogamev3.DownloadManagement.DataAccess.DownloadStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest; // For MongoDB

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataMongoTest // Configures an embedded MongoDB, scans for @Document, configures repositories
class DownloadRepositoryIntegrationTest {

    @Autowired
    private DownloadRepository downloadRepository;

    private Download download1, download2, downloadUser1_1, downloadUser1_2, downloadUser2_1;
    private String userId1 = "user-" + UUID.randomUUID().toString();
    private String userId2 = "user-" + UUID.randomUUID().toString();


    @BeforeEach
    void setUp() {
        downloadRepository.deleteAll(); // Clean slate before each test

        download1 = new Download(new DownloadId(), "http://example.com/file1.zip", DownloadStatus.PENDING, userId1);
        download2 = new Download(new DownloadId(), "http://example.com/file2.zip", DownloadStatus.DOWNLOADING, userId2);
        
        downloadUser1_1 = new Download(new DownloadId(), "http://example.com/user1_file1.zip", DownloadStatus.COMPLETED, userId1);
        downloadUser1_2 = new Download(new DownloadId(), "http://example.com/user1_file2.zip", DownloadStatus.PAUSED, userId1);
        downloadUser2_1 = new Download(new DownloadId(), "http://example.com/user2_file1.zip", DownloadStatus.CANCELLED, userId2);

        downloadRepository.saveAll(List.of(download1, download2, downloadUser1_1, downloadUser1_2, downloadUser2_1));
    }

    @AfterEach
    void tearDown() {
        downloadRepository.deleteAll();
    }

    @Test
    void whenFindDownloadById_Uuid_andExists_thenReturnDownload() {
        // Act
        Download found = downloadRepository.findDownloadById_Uuid(download1.getId().getUuid());

        // Assert
        assertNotNull(found);
        assertEquals(download1.getId().getUuid(), found.getId().getUuid());
        assertEquals(download1.getSourceUrl(), found.getSourceUrl());
        assertEquals(DownloadStatus.PENDING, found.getDownloadStatus());
    }

    @Test
    void whenFindDownloadById_Uuid_andNotExists_thenReturnNull() {
        // Act
        Download found = downloadRepository.findDownloadById_Uuid(UUID.randomUUID().toString());

        // Assert
        assertNull(found);
    }

    @Test
    void whenExistsDownloadById_Uuid_andExists_thenReturnTrue() {
        // Act
        boolean exists = downloadRepository.existsDownloadById_Uuid(download2.getId().getUuid());

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
    void whenDeleteDownloadById_Uuid_thenRemoveFromDb() {
        // Arrange
        String idToDelete = download1.getId().getUuid();
        assertTrue(downloadRepository.existsDownloadById_Uuid(idToDelete));

        // Act
        downloadRepository.deleteDownloadById_Uuid(idToDelete); // Assuming this method exists or use deleteById
                                                                // Your interface has deleteDownloadById_Uuid, let's assume it works.
                                                                // If not, use downloadRepository.deleteById(idToDelete) if ID is just string
                                                                // Or downloadRepository.delete(download1)

        // Assert
        assertFalse(downloadRepository.existsDownloadById_Uuid(idToDelete));
    }
    
    @Test
    void whenGetDownloadsByUserId_andUserHasDownloads_thenReturnList() {
        // Act
        List<Download> user1Downloads = downloadRepository.getDownloadsByUserId(userId1);
        
        // Assert
        assertNotNull(user1Downloads);
        assertEquals(3, user1Downloads.size()); // download1, downloadUser1_1, downloadUser1_2
        assertTrue(user1Downloads.stream().allMatch(d -> d.getUserId().equals(userId1)));
    }

    @Test
    void whenGetDownloadsByUserId_andUserHasNoDownloads_thenReturnEmptyList() {
        // Act
        List<Download> nonExistentUserDownloads = downloadRepository.getDownloadsByUserId("non-existent-user");

        // Assert
        assertNotNull(nonExistentUserDownloads);
        assertTrue(nonExistentUserDownloads.isEmpty());
    }
    
    @Test
    void whenSaveDownload_thenPersistAndCanBeFound() {
        // Arrange
        Download newDownload = new Download(new DownloadId(), "http://new.com/new.zip", DownloadStatus.FAILED, "new-user");
        
        // Act
        Download saved = downloadRepository.save(newDownload);
        Download found = downloadRepository.findDownloadById_Uuid(saved.getId().getUuid());

        // Assert
        assertNotNull(found);
        assertEquals(newDownload.getSourceUrl(), found.getSourceUrl());
        assertEquals(DownloadStatus.FAILED, found.getDownloadStatus());
        assertEquals("new-user", found.getUserId());
    }
    
    @Test
    void whenFindAll_thenReturnAllDownloads() {
        // Act
        List<Download> allDownloads = downloadRepository.findAll();
        
        // Assert
        assertEquals(5, allDownloads.size()); // As per setup
    }
}