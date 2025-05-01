package com.example.videogamev3.dataaccesslayer;

import com.example.videogamev3.DownloadManagement.DataAccess.Download;
import com.example.videogamev3.DownloadManagement.DataAccess.DownloadId;
import com.example.videogamev3.DownloadManagement.DataAccess.DownloadRepository;
import com.example.videogamev3.DownloadManagement.DataAccess.DownloadStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
public class DownloadRepositoryIntegrationTest {

    @Autowired
    private DownloadRepository downloadRepository;

    private Download download1;
    private Download download2;
    private DownloadId downloadId1;
    private DownloadId downloadId2;

    @BeforeEach
    void setUp() {
        downloadRepository.deleteAll();

        downloadId1 = new DownloadId(UUID.randomUUID().toString());
        download1 = new Download(
                downloadId1,
                "http://example.com/file1.zip",
                DownloadStatus.PENDING
        );

        downloadId2 = new DownloadId(UUID.randomUUID().toString());
        download2 = new Download(
                downloadId2,
                "http://example.com/file2.iso",
                DownloadStatus.DOWNLOADING
        );
    }

    @Test
    @DisplayName("Find Download By Existing UUID - Success")
    void whenDownloadExists_FindByUuid_ShouldReturnDownload() {
        downloadRepository.save(download1);
        String existingUuid = download1.getId().getUuid();

        Download foundDownload = downloadRepository.findDownloadById_Uuid(existingUuid);

        assertNotNull(foundDownload);
        assertEquals(download1.getId(), foundDownload.getId());
        assertEquals(download1.getSourceUrl(), foundDownload.getSourceUrl());
        assertEquals(download1.getDownloadStatus(), foundDownload.getDownloadStatus());
        assertEquals(existingUuid, foundDownload.getId().getUuid());
    }

    @Test
    @DisplayName("Find Download By Non-Existent UUID - Returns Null")
    void whenDownloadDoesNotExist_FindByUuid_ShouldReturnNull() {
        String nonExistentUuid = UUID.randomUUID().toString();

        Download foundDownload = downloadRepository.findDownloadById_Uuid(nonExistentUuid);

        assertNull(foundDownload);
    }

    @Test
    @DisplayName("Save New Download - Success")
    void whenSaveNewDownload_ShouldPersistDownload() {

                Download savedDownload = downloadRepository.save(download1);

                assertNotNull(savedDownload);
        assertNotNull(savedDownload.getId());
        assertEquals(download1.getId().getUuid(), savedDownload.getId().getUuid());

                Download retrievedDownload = downloadRepository.findDownloadById_Uuid(download1.getId().getUuid());         assertEquals(download1.getSourceUrl(), retrievedDownload.getSourceUrl());
    }

    @Test
    @DisplayName("Find All Downloads - Success")
    void whenMultipleDownloadsExist_FindAll_ShouldReturnAllDownloads() {
                downloadRepository.save(download1);
        downloadRepository.save(download2);
        long expectedCount = 2; 
                List<Download> downloads = downloadRepository.findAll();

                assertNotNull(downloads);
        assertEquals(expectedCount, downloads.size());

    }

    @Test
    @DisplayName("Find All Downloads When None Exist - Returns Empty List")
    void whenNoDownloadsExist_FindAll_ShouldReturnEmptyList() {
                long expectedCount = 0;

                List<Download> downloads = downloadRepository.findAll();

                assertNotNull(downloads);
        assertEquals(expectedCount, downloads.size());
        assertTrue(downloads.isEmpty());
    }


    @Test
    @DisplayName("Delete Download By ID - Success")
    void whenDownloadExists_DeleteById_ShouldRemoveDownload() {
                Download savedDownload = downloadRepository.save(download1);
        DownloadId idToDelete = savedDownload.getId();
        assertTrue(downloadRepository.existsDownloadById_Uuid(idToDelete.getUuid()), "Download should exist before deletion");

                downloadRepository.deleteDownloadById_Uuid(idToDelete.getUuid()); 
                assertFalse(downloadRepository.existsDownloadById_Uuid(idToDelete.getUuid()), "Download should not exist after deletion");
        assertNull(downloadRepository.findDownloadById_Uuid(idToDelete.getUuid()), "Finding by UUID should return null after deletion");
    }


    @Test
    @DisplayName("Update Existing Download - Success")
    void whenUpdateExistingDownload_ShouldReflectChanges() {
                Download savedDownload = downloadRepository.save(download1);
        DownloadId downloadId = savedDownload.getId();

                Download downloadToUpdate = downloadRepository.findDownloadById_Uuid(downloadId.getUuid());

        downloadToUpdate.setDownloadStatus(DownloadStatus.COMPLETED);
        downloadToUpdate.setSourceUrl("http://new.example.com/updated.zip");
        downloadRepository.save(downloadToUpdate); 
                Download updatedDownload = downloadRepository.findDownloadById_Uuid(downloadId.getUuid());

        assertEquals(downloadId, updatedDownload.getId());
        assertEquals(DownloadStatus.COMPLETED, updatedDownload.getDownloadStatus());
        assertEquals("http://new.example.com/updated.zip", updatedDownload.getSourceUrl());
    }

    @Test
    @DisplayName("Check Download Existence By ID - Exists")
    void whenDownloadExists_ExistsById_ShouldReturnTrue() {
                downloadRepository.save(download1);
        DownloadId existingId = download1.getId();

                boolean exists = downloadRepository.existsDownloadById_Uuid(existingId.getUuid());

                assertTrue(exists);
    }

    @Test
    @DisplayName("Check Download Existence By ID - Does Not Exist")
    void whenDownloadDoesNotExist_ExistsById_ShouldReturnFalse() {
                DownloadId nonExistentId = new DownloadId(UUID.randomUUID().toString());

                boolean exists = downloadRepository.existsDownloadById_Uuid(nonExistentId.getUuid());

                assertFalse(exists);
    }

    @Test
    @DisplayName("Count Downloads - Success")
    void whenMultipleDownloadsExist_Count_ShouldReturnCorrectNumber() {
                downloadRepository.save(download1);
        downloadRepository.save(download2);
        long expectedCount = 2;

                long actualCount = downloadRepository.count();

                assertEquals(expectedCount, actualCount);
    }
}