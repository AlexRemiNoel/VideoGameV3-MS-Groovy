package com.example.videogamev3.Tests;

import com.example.videogamev3.DownloadManagement.BusinessLogic.DownloadService;
import com.example.videogamev3.DownloadManagement.DataAccess.Download;
import com.example.videogamev3.DownloadManagement.DataAccess.DownloadId;
import com.example.videogamev3.DownloadManagement.DataAccess.DownloadRepository;
import com.example.videogamev3.DownloadManagement.DataAccess.DownloadStatus;
import com.example.videogamev3.DownloadManagement.DataMapper.DownloadRequestMapper;
import com.example.videogamev3.DownloadManagement.DataMapper.DownloadResponseMapper;
import com.example.videogamev3.DownloadManagement.Presentation.DownloadRequestModel;
import com.example.videogamev3.DownloadManagement.Presentation.DownloadResponseModel;
import com.example.videogamev3.DownloadManagement.utils.exceptions.DownloadNotFoundException;
import com.example.videogamev3.DownloadManagement.utils.exceptions.DuplicateDownloadIDException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class) // Replaces @SpringBootTest for pure unit tests with Mockito
class DownloadServiceUnitTest {

    @Mock
    private DownloadRepository downloadRepository;

    @Mock
    private DownloadResponseMapper downloadResponseMapper;

    @Mock
    private DownloadRequestMapper downloadRequestMapper;

    @InjectMocks // Automatically injects mocked dependencies into DownloadService
    private DownloadService downloadService;

    private DownloadRequestModel requestModel;
    private Download downloadEntity;
    private DownloadResponseModel responseModel;
    private String testDownloadId;
    private String testUserId;

    @BeforeEach
    void setUp() {
        testDownloadId = UUID.randomUUID().toString();
        testUserId = "user-" + UUID.randomUUID().toString();

        requestModel = new DownloadRequestModel("http://example.com/file.zip", testUserId);

        downloadEntity = new Download(new DownloadId(testDownloadId), "http://example.com/file.zip", DownloadStatus.PENDING, testUserId);

        responseModel = new DownloadResponseModel(testDownloadId, "http://example.com/file.zip", DownloadStatus.PENDING.toString(), testUserId);
    }

    // --- createDownload ---
    @Test
    void whenCreateDownload_thenSaveAndReturnResponse() {
        // Arrange
        when(downloadRequestMapper.downloadRequestModelToDownload(any(DownloadRequestModel.class))).thenReturn(downloadEntity);
        // Ensure existsDownloadById_Uuid returns false for the new ID to avoid DuplicateDownloadIDException
        when(downloadRepository.existsDownloadById_Uuid(anyString())).thenReturn(false);
        when(downloadRepository.save(any(Download.class))).thenReturn(downloadEntity); // Mock save behavior
        when(downloadResponseMapper.downloadEntityToDownloadResponseModel(any(Download.class))).thenReturn(responseModel);

        // Act
        DownloadResponseModel created = downloadService.createDownload(requestModel);

        // Assert
        assertNotNull(created);
        assertEquals(responseModel.getSourceUrl(), created.getSourceUrl());
        assertEquals(responseModel.getUserId(), created.getUserId());
        assertEquals(DownloadStatus.PENDING.toString(), created.getStatus()); // Initial status should be PENDING

        verify(downloadRequestMapper, times(1)).downloadRequestModelToDownload(requestModel);
        verify(downloadRepository, times(1)).existsDownloadById_Uuid(anyString()); // Should check before saving
        verify(downloadRepository, times(1)).save(any(Download.class)); // Check that save was called
        verify(downloadResponseMapper, times(1)).downloadEntityToDownloadResponseModel(downloadEntity);
    }

    @Test
    void whenCreateDownload_andIdCollision_thenThrowDuplicateDownloadIDException() {
        // Arrange
        when(downloadRequestMapper.downloadRequestModelToDownload(any(DownloadRequestModel.class))).thenReturn(downloadEntity);
        when(downloadRepository.existsDownloadById_Uuid(anyString())).thenReturn(true); // Simulate ID collision

        // Act & Assert
        assertThrows(DuplicateDownloadIDException.class, () -> {
            downloadService.createDownload(requestModel);
        });

        verify(downloadRepository, times(1)).existsDownloadById_Uuid(anyString());
        verify(downloadRepository, never()).save(any(Download.class)); // Save should not be called
    }


    // --- getDownload ---
    @Test
    void whenGetDownload_andExists_thenReturnDownload() {
        // Arrange
        when(downloadRepository.findDownloadById_Uuid(testDownloadId)).thenReturn(downloadEntity);
        when(downloadResponseMapper.downloadEntityToDownloadResponseModel(downloadEntity)).thenReturn(responseModel);

        // Act
        DownloadResponseModel found = downloadService.getDownload(testDownloadId);

        // Assert
        assertNotNull(found);
        assertEquals(testDownloadId, found.getId());
        verify(downloadRepository, times(1)).findDownloadById_Uuid(testDownloadId);
        verify(downloadResponseMapper, times(1)).downloadEntityToDownloadResponseModel(downloadEntity);
    }

    @Test
    void whenGetDownload_andNotExists_thenThrowDownloadNotFoundException() {
        // Arrange
        when(downloadRepository.findDownloadById_Uuid(testDownloadId)).thenReturn(null);

        // Act & Assert
        assertThrows(DownloadNotFoundException.class, () -> {
            downloadService.getDownload(testDownloadId);
        });
        verify(downloadRepository, times(1)).findDownloadById_Uuid(testDownloadId);
        verify(downloadResponseMapper, never()).downloadEntityToDownloadResponseModel((Download) any());
    }

    // --- startDownload ---
    @Test
    void whenStartDownload_andIsPending_thenSetStatusToDownloading() {
        // Arrange
        downloadEntity.setDownloadStatus(DownloadStatus.PENDING);
        when(downloadRepository.findDownloadById_Uuid(testDownloadId)).thenReturn(downloadEntity);
        when(downloadRepository.save(any(Download.class))).thenReturn(downloadEntity); // mock save
        when(downloadResponseMapper.downloadEntityToDownloadResponseModel(downloadEntity))
            .thenAnswer(invocation -> { // Simulate status change in response
                Download saved = invocation.getArgument(0);
                return new DownloadResponseModel(saved.getId().getUuid(), saved.getSourceUrl(), saved.getDownloadStatus().toString(), saved.getUserId());
            });


        // Act
        DownloadResponseModel updated = downloadService.startDownload(testDownloadId);

        // Assert
        assertNotNull(updated);
        assertEquals(DownloadStatus.DOWNLOADING.toString(), updated.getStatus());
        verify(downloadRepository, times(1)).save(downloadEntity);
        assertEquals(DownloadStatus.DOWNLOADING, downloadEntity.getDownloadStatus()); // Check entity state
    }

    @Test
    void whenStartDownload_andIsPaused_thenSetStatusToDownloading() {
        // Arrange
        downloadEntity.setDownloadStatus(DownloadStatus.PAUSED);
        when(downloadRepository.findDownloadById_Uuid(testDownloadId)).thenReturn(downloadEntity);
        when(downloadRepository.save(any(Download.class))).thenReturn(downloadEntity);
        when(downloadResponseMapper.downloadEntityToDownloadResponseModel(downloadEntity))
            .thenAnswer(invocation -> {
                Download saved = invocation.getArgument(0);
                return new DownloadResponseModel(saved.getId().getUuid(), saved.getSourceUrl(), saved.getDownloadStatus().toString(), saved.getUserId());
            });


        // Act
        DownloadResponseModel updated = downloadService.startDownload(testDownloadId);

        // Assert
        assertEquals(DownloadStatus.DOWNLOADING.toString(), updated.getStatus());
        verify(downloadRepository, times(1)).save(downloadEntity);
    }
    
    @Test
    void whenStartDownload_andIsAlreadyDownloading_thenDoNotChangeStatus() {
        // Arrange
        downloadEntity.setDownloadStatus(DownloadStatus.DOWNLOADING);
        when(downloadRepository.findDownloadById_Uuid(testDownloadId)).thenReturn(downloadEntity);
        // NO save mock needed, as it shouldn't be called
        when(downloadResponseMapper.downloadEntityToDownloadResponseModel(downloadEntity))
            .thenReturn(new DownloadResponseModel(downloadEntity.getId().getUuid(), downloadEntity.getSourceUrl(), downloadEntity.getDownloadStatus().toString(), downloadEntity.getUserId()));

        // Act
        DownloadResponseModel updated = downloadService.startDownload(testDownloadId);

        // Assert
        assertEquals(DownloadStatus.DOWNLOADING.toString(), updated.getStatus()); // Status remains DOWNLOADING
        verify(downloadRepository, never()).save(any(Download.class)); // Save should not be called
    }


    // --- pauseDownload ---
    @Test
    void whenPauseDownload_andIsDownloading_thenSetStatusToPaused() {
        // Arrange
        downloadEntity.setDownloadStatus(DownloadStatus.DOWNLOADING);
        when(downloadRepository.findDownloadById_Uuid(testDownloadId)).thenReturn(downloadEntity);
        when(downloadRepository.save(any(Download.class))).thenReturn(downloadEntity);
        when(downloadResponseMapper.downloadEntityToDownloadResponseModel(downloadEntity))
            .thenAnswer(invocation -> {
                Download saved = invocation.getArgument(0);
                return new DownloadResponseModel(saved.getId().getUuid(), saved.getSourceUrl(), saved.getDownloadStatus().toString(), saved.getUserId());
            });

        // Act
        DownloadResponseModel updated = downloadService.pauseDownload(testDownloadId);

        // Assert
        assertEquals(DownloadStatus.PAUSED.toString(), updated.getStatus());
        verify(downloadRepository, times(1)).save(downloadEntity);
    }
    
    @Test
    void whenPauseDownload_andIsNotDownloading_thenDoNotChangeStatus() {
        // Arrange
        downloadEntity.setDownloadStatus(DownloadStatus.PENDING); // e.g., PENDING
        when(downloadRepository.findDownloadById_Uuid(testDownloadId)).thenReturn(downloadEntity);
        when(downloadResponseMapper.downloadEntityToDownloadResponseModel(downloadEntity))
             .thenReturn(new DownloadResponseModel(downloadEntity.getId().getUuid(), downloadEntity.getSourceUrl(), downloadEntity.getDownloadStatus().toString(), downloadEntity.getUserId()));

        // Act
        DownloadResponseModel updated = downloadService.pauseDownload(testDownloadId);

        // Assert
        assertEquals(DownloadStatus.PENDING.toString(), updated.getStatus());
        verify(downloadRepository, never()).save(any(Download.class));
    }


    // --- resumeDownload ---
    @Test
    void whenResumeDownload_andIsPaused_thenSetStatusToDownloading() {
        // Arrange
        downloadEntity.setDownloadStatus(DownloadStatus.PAUSED);
        when(downloadRepository.findDownloadById_Uuid(testDownloadId)).thenReturn(downloadEntity);
        when(downloadRepository.save(any(Download.class))).thenReturn(downloadEntity);
        when(downloadResponseMapper.downloadEntityToDownloadResponseModel(downloadEntity))
            .thenAnswer(invocation -> {
                Download saved = invocation.getArgument(0);
                return new DownloadResponseModel(saved.getId().getUuid(), saved.getSourceUrl(), saved.getDownloadStatus().toString(), saved.getUserId());
            });

        // Act
        DownloadResponseModel updated = downloadService.resumeDownload(testDownloadId);

        // Assert
        assertEquals(DownloadStatus.DOWNLOADING.toString(), updated.getStatus());
        verify(downloadRepository, times(1)).save(downloadEntity);
    }
    
    @Test
    void whenResumeDownload_andIsNotPaused_thenDoNotChangeStatus() {
        // Arrange
        downloadEntity.setDownloadStatus(DownloadStatus.DOWNLOADING); // e.g., already DOWNLOADING
        when(downloadRepository.findDownloadById_Uuid(testDownloadId)).thenReturn(downloadEntity);
        when(downloadResponseMapper.downloadEntityToDownloadResponseModel(downloadEntity))
             .thenReturn(new DownloadResponseModel(downloadEntity.getId().getUuid(), downloadEntity.getSourceUrl(), downloadEntity.getDownloadStatus().toString(), downloadEntity.getUserId()));

        // Act
        DownloadResponseModel updated = downloadService.resumeDownload(testDownloadId);

        // Assert
        assertEquals(DownloadStatus.DOWNLOADING.toString(), updated.getStatus());
        verify(downloadRepository, never()).save(any(Download.class));
    }

    // --- cancelDownload ---
    @Test
    void whenCancelDownload_andIsDownloading_thenSetStatusToCancelled() {
        // Arrange
        downloadEntity.setDownloadStatus(DownloadStatus.DOWNLOADING);
        when(downloadRepository.findDownloadById_Uuid(testDownloadId)).thenReturn(downloadEntity);
        when(downloadRepository.save(any(Download.class))).thenReturn(downloadEntity);
        when(downloadResponseMapper.downloadEntityToDownloadResponseModel(downloadEntity))
            .thenAnswer(invocation -> {
                Download saved = invocation.getArgument(0);
                return new DownloadResponseModel(saved.getId().getUuid(), saved.getSourceUrl(), saved.getDownloadStatus().toString(), saved.getUserId());
            });

        // Act
        DownloadResponseModel updated = downloadService.cancelDownload(testDownloadId);

        // Assert
        assertEquals(DownloadStatus.CANCELLED.toString(), updated.getStatus());
        verify(downloadRepository, times(1)).save(downloadEntity);
    }
    
    @Test
    void whenCancelDownload_andIsAlreadyCompleted_thenDoNotChangeStatus() {
        // Arrange
        downloadEntity.setDownloadStatus(DownloadStatus.COMPLETED);
        when(downloadRepository.findDownloadById_Uuid(testDownloadId)).thenReturn(downloadEntity);
        when(downloadResponseMapper.downloadEntityToDownloadResponseModel(downloadEntity))
             .thenReturn(new DownloadResponseModel(downloadEntity.getId().getUuid(), downloadEntity.getSourceUrl(), downloadEntity.getDownloadStatus().toString(), downloadEntity.getUserId()));

        // Act
        DownloadResponseModel updated = downloadService.cancelDownload(testDownloadId);

        // Assert
        assertEquals(DownloadStatus.COMPLETED.toString(), updated.getStatus());
        verify(downloadRepository, never()).save(any(Download.class));
    }

    // --- getAllDownloads ---
    @Test
    void whenGetAllDownloads_andDownloadsExist_thenReturnList() {
        // Arrange
        List<Download> downloads = Collections.singletonList(downloadEntity);
        List<DownloadResponseModel> responseModels = Collections.singletonList(responseModel);
        when(downloadRepository.findAll()).thenReturn(downloads);
        when(downloadResponseMapper.downloadEntityToDownloadResponseModel(downloads)).thenReturn(responseModels);

        // Act
        List<DownloadResponseModel> foundList = downloadService.getAllDownloads();

        // Assert
        assertNotNull(foundList);
        assertFalse(foundList.isEmpty());
        assertEquals(1, foundList.size());
        assertEquals(responseModel.getId(), foundList.get(0).getId());
        verify(downloadRepository, times(1)).findAll();
        verify(downloadResponseMapper, times(1)).downloadEntityToDownloadResponseModel(downloads);
    }

    @Test
    void whenGetAllDownloads_andNoDownloads_thenReturnEmptyList() {
        // Arrange
        when(downloadRepository.findAll()).thenReturn(Collections.emptyList());
        when(downloadResponseMapper.downloadEntityToDownloadResponseModel(Collections.emptyList())).thenReturn(Collections.emptyList());

        // Act
        List<DownloadResponseModel> foundList = downloadService.getAllDownloads();

        // Assert
        assertNotNull(foundList);
        assertTrue(foundList.isEmpty());
        verify(downloadRepository, times(1)).findAll();
    }

    // --- deleteDownload ---
    @Test
    void whenDeleteDownload_andExists_thenCallRepositoryDelete() {
        // Arrange
        when(downloadRepository.findDownloadById_Uuid(testDownloadId)).thenReturn(downloadEntity);
        doNothing().when(downloadRepository).delete(downloadEntity); // For void methods

        // Act
        downloadService.deleteDownload(testDownloadId);

        // Assert
        verify(downloadRepository, times(1)).findDownloadById_Uuid(testDownloadId);
        verify(downloadRepository, times(1)).delete(downloadEntity);
    }

    @Test
    void whenDeleteDownload_andNotExists_thenThrowDownloadNotFoundException() {
        // Arrange
        when(downloadRepository.findDownloadById_Uuid(testDownloadId)).thenReturn(null);

        // Act & Assert
        assertThrows(DownloadNotFoundException.class, () -> {
            downloadService.deleteDownload(testDownloadId);
        });
        verify(downloadRepository, times(1)).findDownloadById_Uuid(testDownloadId);
        verify(downloadRepository, never()).delete(any());
    }

    // --- getAllDownloadsByUserId ---
    @Test
    void whenGetAllDownloadsByUserId_andDownloadsExist_thenReturnList() {
        // Arrange
        List<Download> userDownloads = Collections.singletonList(downloadEntity);
        List<DownloadResponseModel> userResponseModels = Collections.singletonList(responseModel);
        when(downloadRepository.getDownloadsByUserId(testUserId)).thenReturn(userDownloads);
        when(downloadResponseMapper.downloadEntityToDownloadResponseModel(userDownloads)).thenReturn(userResponseModels);

        // Act
        List<DownloadResponseModel> result = downloadService.getAllDownloadsByUserId(testUserId);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testUserId, result.get(0).getUserId());
        verify(downloadRepository, times(1)).getDownloadsByUserId(testUserId);
    }

    // --- updateDownload ---
    @Test
    void whenUpdateDownload_andExists_thenUpdateAndSave() {
        // Arrange
        DownloadRequestModel updateRequest = new DownloadRequestModel("http://newurl.com/file.dat", testUserId);
        Download updatedEntity = new Download(new DownloadId(testDownloadId), "http://newurl.com/file.dat", downloadEntity.getDownloadStatus(), testUserId);
        DownloadResponseModel updatedResponse = new DownloadResponseModel(testDownloadId, "http://newurl.com/file.dat", downloadEntity.getDownloadStatus().toString(), testUserId);

        when(downloadRepository.findDownloadById_Uuid(testDownloadId)).thenReturn(downloadEntity);
        when(downloadRepository.save(any(Download.class))).thenReturn(updatedEntity); // Simulate save returning updated entity
        when(downloadResponseMapper.downloadEntityToDownloadResponseModel(updatedEntity)).thenReturn(updatedResponse);

        // Act
        DownloadResponseModel result = downloadService.updateDownload(testDownloadId, updateRequest);

        // Assert
        assertNotNull(result);
        assertEquals("http://newurl.com/file.dat", result.getSourceUrl());
        assertEquals(downloadEntity.getDownloadStatus().toString(), result.getStatus()); // Status shouldn't change with this update
        verify(downloadRepository, times(1)).findDownloadById_Uuid(testDownloadId);
        verify(downloadRepository, times(1)).save(argThat(savedDownload ->
                savedDownload.getId().getUuid().equals(testDownloadId) &&
                savedDownload.getSourceUrl().equals("http://newurl.com/file.dat")
        ));
        verify(downloadResponseMapper, times(1)).downloadEntityToDownloadResponseModel(updatedEntity);
    }

    @Test
    void whenUpdateDownload_andNotExists_thenThrowDownloadNotFoundException() {
        // Arrange
        when(downloadRepository.findDownloadById_Uuid(testDownloadId)).thenReturn(null);
        DownloadRequestModel updateRequest = new DownloadRequestModel("http://newurl.com/file.dat", testUserId);

        // Act & Assert
        assertThrows(DownloadNotFoundException.class, () -> {
            downloadService.updateDownload(testDownloadId, updateRequest);
        });
        verify(downloadRepository, times(1)).findDownloadById_Uuid(testDownloadId);
        verify(downloadRepository, never()).save(any());
    }
}